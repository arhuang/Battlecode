package team016;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * *********************************RETURN WIN;****************************************
 * SIGNALS
 * 0: Archon Radius + Center
 * 1: Enemy RobotInfo
 * 2: All Move
 * 
 * @author aaronhuang
 *
 */
public class RobotPlayer {
	static int radius;
	static RobotController rc;
	static Direction position;
	static MapLocation goal;
	static MapLocation center;
	static int patience=20;
	static Random rnd;
	
	public static void run(RobotController rcIn) throws GameActionException{
		rc=rcIn;
		rnd = new Random(rc.getID());
		
		//Set Up
		if(rc.getType()==RobotType.ARCHON && rc.getRoundNum()==0){
			patience=0;
			Signal[] incomingMessages = rc.emptySignalQueue();
			int total=0;
			for(Signal s: incomingMessages){
				if(s.getTeam()==rc.getTeam()){
					int[] message = s.getMessage();
					if(message!=null){
						center = new MapLocation(message[0],message[1]);
					}
					total+=1;
				}
			}
			if(total==0){
				position = Direction.NONE;
				center =rc.getLocation();
				rc.broadcastMessageSignal(rc.getLocation().x,rc.getLocation().y,10000);
			}else if(total==1){
				position=Direction.EAST;
				rc.broadcastSignal(10000);
			}else if(total==2){
				position=Direction.SOUTH;
				rc.broadcastSignal(10000);
			}else if(total==3){
				position=Direction.NORTH_WEST;
				rc.broadcastSignal(10000);
			}
		}
		
		while(true){
			try{
				if(rc.getType()==RobotType.ARCHON){
					archonCode();
				} else if(rc.getType()==RobotType.SCOUT) {
					scoutCode();
				} else if(rc.getType()==RobotType.SOLDIER) {
					soldierCode();	
				} else if(rc.getType()==RobotType.TURRET) {
					turretCode();
				}else if(rc.getType()==RobotType.TTM){
					ttmCode();
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			Clock.yield();
		}
	}
	
	/**
	 * *********************************ARCHON****************************************
	 */
	public static void archonCode() throws GameActionException{
		if(position==Direction.NONE){
			int army = 4;
			for (RobotInfo r : rc.senseNearbyRobots(35, rc.getTeam())) {
				if(r.type==RobotType.TURRET || r.type==RobotType.SCOUT){
					army++;
				}
			}
			
			if(rc.getRoundNum()<90){
				if (rc.getTeamParts() > 100) {
					if (build(RobotType.SOLDIER)){ 
						return;
					}
				}
			}else if(army%8==0){
				if (rc.getTeamParts() > 100) {
					if (build(RobotType.SCOUT)){ 
						return;
					}
				}
			}else{
				if (rc.getTeamParts() > 100) {
					if (build(RobotType.TURRET)){ 
						return;
					}
				}
			}
			int p;
			if(army<9){
				p=2;
			}else if(army<21){
				p=8;
			}else if(army<25){
				p=9;
			}else if(army<29){
				p=10;
			}else if(army<37){
				p=13;
			}else if(army<45){
				p=16;
			}else if(army<49){
				p=17;
			}else if(army<57){
				p=18;
			}else if(army<61){
				p=20;
			}else{
				p=25;
			}
			rc.broadcastMessageSignal(p*10, center.x*100000+center.y, 35);
		}else{
			goal = center.add(position);
			if(rc.getLocation().distanceSquaredTo(goal)==0){
				position=Direction.NONE;
			}else{
				archonMove(goal);
			}
		}
	}
	
	public static void archonMove(MapLocation g) throws GameActionException{
		if(rc.isCoreReady()){
			Direction forward = rc.getLocation().directionTo(g);
			if(rc.canMove(forward)){
				rc.move(forward);
				patience=Math.max(patience+1, 31);
			}else{
				if(patience<5){
					if(rc.getType().canClearRubble()){
						//failed to move, look to clear rubble
						MapLocation ahead = rc.getLocation().add(forward);
						if(rc.senseRubble(ahead)>=GameConstants.RUBBLE_OBSTRUCTION_THRESH){
							rc.clearRubble(forward);
							return;
						}
					}
				}
				for(int i=patience;i<=31;i=i+10){
					Direction try1 = forward;
					Direction try2 = forward;
					for(int j=patience;j<=i;j=j+10){
						try1=try1.rotateLeft();
						try2=try2.rotateRight();
					}
					if(rc.canMove(try1)){
						rc.move(try1);
						break;
					}else if(rc.canMove(try2)){
						rc.move(try2);
						break;
					}
				}
				patience=Math.max(patience-10, 1);
			}
		}
	}
	
	private static boolean build(RobotType type) throws GameActionException{

		Direction randomDir = Utility.randomDirection();
		if(rc.getRoundNum()%2==0){
			randomDir=Direction.SOUTH_EAST;
		}
		int count= 0;
		while (!rc.canBuild(randomDir, type) && count<8) {
			randomDir = randomDir.rotateLeft();
			count++;
		}
		if(count<8){
			rc.build(randomDir, type);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * *********************************SCOUT****************************************
	 * **ENEMY MESSAGE FORMAT**
	 * 1. Type,Health,Threat: 00,0000,0 2/4/1
	 * 2. Location: 00000,00000 5/5
	 * @throws GameActionException
	 */
	private static void scoutCode() throws GameActionException {
		Signal[] incomingSignals = rc.emptySignalQueue();
		Signal[] signals;
		if (incomingSignals.length>125) {
			signals = new Signal[125];
			for(int i=0; i<125; i++) {
				signals[i]=incomingSignals[i];
			}
		} else {
			signals = incomingSignals;
		}
		for(Signal s: signals){	
			int[] message = s.getMessage();
			if(s.getTeam()==rc.getTeam()){
				if(message!=null){
					if(message[0]%10==0){
						center= s.getLocation();
						int r = message[0]/10;
						r = (int) Math.pow(r,.5);
						radius=(int) Math.pow(r,2);
					}
				}
			}
		}
		//Broadcast Enemies
		RobotInfo[] enemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
		for(RobotInfo r: enemyArray) {
			MapLocation l = r.location;
			int m2=l.x*100000+l.y;
			int m1=(int)(r.health)*10+1;
			m1 += Utility.castType(r)*100000;
			rc.broadcastMessageSignal(m1, m2, 53);
		}
		MapLocation travel=scoutFind();
		if(rc.getLocation()!=travel){
			turretMove(travel);
		}
	}
	
	private static MapLocation scoutFind() throws GameActionException {
		MapLocation loc = rc.getLocation();
		MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(loc, 12);
		double max = scoutDistance(loc);
		MapLocation travel = loc;
		
		for(MapLocation l: locs) {
			double r = archonDistance(l);
			double dist = scoutDistance(l);
			if (r<radius && dist>max && rc.senseRobotAtLocation(l)==null && rc.onTheMap(l)  ) {
				travel = l;
				max = dist;
			}
		}
		return travel;
	}
	
	private static double scoutDistance(MapLocation loc) throws GameActionException {
		ArrayList<MapLocation> scouts = new ArrayList<MapLocation>();
		for (RobotInfo r: rc.senseNearbyRobots(25, rc.getTeam())) {
			if (r.type==RobotType.SCOUT) {
				scouts.add(r.location);
			}
		}
		return loc.distanceSquaredTo(avg(scouts));
	}
	
	private static MapLocation avg(ArrayList<MapLocation> locs) {
		int len = locs.size();
		int x=0,y=0;
		if (len==0) {
			return center;
		}
		for(MapLocation l: locs) {
			x+=l.x;
			y+=l.y;
		}
		return new MapLocation(x/len,y/len);
	}
	
	/**
	 * *********************************TURRET****************************************
	 * **BEHAVIOR 0 - TURTLE**
	 * for each location 2 squares away within radius and movable
	 *      if archonDistance is greater: move
	 * @throws GameActionException 
	 */
	private static void turretCode() throws GameActionException {
		//Attack Enemy
		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
		Signal[] incomingSignals = rc.emptySignalQueue();
		Signal[] signals;
		if (incomingSignals.length>125) {
			signals = new Signal[125];
			for(int i=0; i<125; i++) {
				signals[i]=incomingSignals[i];
			}
		} else {
			signals = incomingSignals;
		}
		boolean unit = false;
		for(Signal s:signals){
			int[] message = s.getMessage();
			if(s.getTeam()==rc.getTeam()){
				if(message!=null){
					if(message[0]%10==0){
						center = s.getLocation();
						int r = message[0]/10;
						radius=r;
					}else{
						unit=true;
					}
				}
			}
		}
		
		if (visibleEnemyArray.length > 0 || unit) {
			if (rc.isWeaponReady()) {
				rc.attackLocation(Utility.toAttack(visibleEnemyArray, incomingSignals));
			}
		}else{
			MapLocation travel=turretFind();
			if(travel!=rc.getLocation()){
				goal = travel;
				rc.pack();
			}
		}			
	}
	
	private static void ttmCode() throws GameActionException{
		goal=turretFind();
		if(rc.getLocation().equals(goal)){
			rc.unpack();
		}else{
			turretMove(goal);
		}
	}
	
	private static double archonDistance(MapLocation loc) {
		return loc.distanceSquaredTo(center);
	}
	
	public static void turretMove(MapLocation g) throws GameActionException {
		rc.setIndicatorString(0, g.toString());
		if(rc.isCoreReady()){
			MapLocation loc = rc.getLocation();
			Direction forward = loc.directionTo(g);
			if(rc.canMove(forward)){
				rc.move(forward);
				position=forward;
			}else{
				forward=position;
				if(rc.canMove(forward)){
					rc.move(forward);
				}else{
					for(int i=0;i<=3;i++){
						Direction try1 = forward;
						Direction try2 = forward;
						for(int j=0;j<=i;j++){
							try1=try1.rotateLeft();
							try2=try2.rotateRight();
						}
						if(rc.canMove(try1)){
							rc.move(try1);
							position=try1;
							return;
						}else if(rc.canMove(try2)){
							rc.move(try2);
							position=try2;
							return;
						}
					}
				}
			}
		}
	}
	
	private static MapLocation turretFind() throws GameActionException{
		MapLocation loc = rc.getLocation();
		MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(loc, 12);
		double max = archonDistance(loc);
		MapLocation travel = loc;
		for(MapLocation l: locs) {
			double dist = archonDistance(l);
			if (dist<radius && dist>max && rc.senseRobotAtLocation(l)==null && rc.senseRubble(l)<GameConstants.RUBBLE_OBSTRUCTION_THRESH && rc.onTheMap(l)) {
				travel = l;
				max = dist;
			}
		}
		return travel;
	}
	
	/**
	 * *********************************SOLDIER****************************************
	 * **BEHAVIOR 0 - TURTLE**
	 * PRIORITY
	 * 1. Kite
	 * 2. Clear Rubble
	 * 3. Move back
	 * 4. Explore
	 * @throws GameActionException 
	 */
	private static void soldierCode() throws GameActionException {
		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
		Signal[] incomingSignals = rc.emptySignalQueue();
		for(Signal s:incomingSignals){
			int[] message = s.getMessage();
			if(s.getTeam()==rc.getTeam()){
				if(message!=null){
					if(message[0]%10==0){
						int x = message[1]/100000;
						int y = message[1]%100000;
						center= new MapLocation(x,y);
						int r = message[0]/10;
						radius=r;
					}
				}
			}
		}
		int dist = rc.getLocation().distanceSquaredTo(center);
		if (visibleEnemyArray.length==0) {
			if (goal!=null&&(rc.senseRubble(goal)<100 || rc.senseRobotAtLocation(goal)!=null)) {
				goal = null;
				rc.setIndicatorString(0, "done clearing");
			}	
			if(goal==null) {
				MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 8);
				ArrayList<MapLocation> inside = new ArrayList<MapLocation>();
				for(MapLocation loc: locs) {
					if (loc.distanceSquaredTo(center)<=1.5*radius) {
						inside.add(loc);
					}
				}
				for(MapLocation l: inside) {
					if (rc.senseRubble(l)>99 && rc.senseRobotAtLocation(l)==null) {
						goal = l;
						return;
					}
				}
			} else {
				Utility.tryToMove(goal);
			}
			
			if (dist>radius && dist< 4*radius) { //Explore
				MapLocation travel = soldierFind();
				Utility.tryToMove(travel);
			}else{ //Move back
				MapLocation loc = center.add(center.directionTo(rc.getLocation()), (int)Math.pow(radius, 0.5)+1);
				Utility.tryToMove(loc);
			}
		} else { //Kite
			kite(visibleEnemyArray);
		}
	}
	
	private static void kite(RobotInfo[] enemy) throws GameActionException {
		RobotInfo toKite = enemy[0];
		int closest = 1000000;
		
		for (RobotInfo r: enemy) {
			if (r.type.attackRadiusSquared >= rc.getType().attackRadiusSquared) {
				rc.attackLocation(r.location);
				rc.setIndicatorString(0, "Go Rambo");
				return;
			}
			int dist = rc.getLocation().distanceSquaredTo(r.location) - r.type.attackRadiusSquared;
			if( dist < closest) {
				toKite = r;
				closest = dist;
			}
		}
		
		if (rc.getLocation().distanceSquaredTo(toKite.location) > toKite.type.attackRadiusSquared) { //outside range
			if (rc.isWeaponReady()) {
				rc.attackLocation(Utility.toAttack(enemy, new Signal[0]));
			}
		} else {
			MapLocation loc = rc.getLocation(); 
			MapLocation travel = loc.subtract(loc.directionTo(toKite.location));
			Utility.tryToMove(travel);
		}
	}
	
	private static MapLocation soldierFind() throws GameActionException {
		MapLocation loc = rc.getLocation();
		MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(loc, 12);
		double max = scoutDistance(loc);
		MapLocation travel = loc;
		
		for(MapLocation l: locs) {
			double r = archonDistance(l);
			double dist = soldierDistance(l);
			if (r<radius && dist>max && rc.senseRobotAtLocation(l)==null && rc.onTheMap(l)  ) {
				travel = l;
				max = dist;
			}
		}
		return travel;
	}
	
	private static double soldierDistance(MapLocation loc) throws GameActionException {
		ArrayList<MapLocation> soldiers = new ArrayList<MapLocation>();
		for (RobotInfo r: rc.senseNearbyRobots(25, rc.getTeam())) {
			if (r.type==RobotType.SOLDIER) {
				soldiers.add(r.location);
			}
		}
		return loc.distanceSquaredTo(avg(soldiers));
	}
}
