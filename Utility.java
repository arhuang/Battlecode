package team016;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Random;

public class Utility {

	static RobotController rc = RobotPlayer.rc;
	static Random rnd = new Random(rc.getID());
	
	/**
	 * *********************************GENERAL****************************************
	 */
	
	public static void tryToMove(MapLocation g) throws GameActionException{
		if(rc.isCoreReady()){
			Direction forward = rc.getLocation().directionTo(g);
			if(rc.canMove(forward)){
				rc.move(forward);
			}else{
				if(rc.getType().canClearRubble()){
					MapLocation travel = rc.getLocation().add(forward);
					if(rc.senseRubble(travel)>=GameConstants.RUBBLE_OBSTRUCTION_THRESH){
						rc.clearRubble(forward);
						return;
					}
				}
				Direction[] directions = {forward.rotateLeft(),forward.rotateRight(),forward.rotateLeft().rotateLeft(),forward.rotateRight().rotateRight()};
				for(Direction dir:directions){
					if(rc.canMove(dir)){
						rc.move(dir);
						break;
					}
				}
			}
		}
	}
	
	public static MapLocation toAttack(RobotInfo[] visibleEnemyArray, Signal[] incomingSignals) {
		double mostDangerous = 0;
		MapLocation attackLocation = null;
		for (RobotInfo r : visibleEnemyArray) {
			if (rc.canAttackLocation(r.location)){
				double danger = (r.attackPower/r.weaponDelay)/r.health;
				if (danger>=mostDangerous) {
					attackLocation = r.location;
					mostDangerous = danger;
				}
			}
		}
		for (Signal s: incomingSignals) {
			int[] message = s.getMessage();
			if (message[0]%10==1) {
				rc.setIndicatorString(0, "got enemy");
				MapLocation loc = new MapLocation(message[1]/100000, message[1]%100000);
				if (rc.canAttackLocation(loc)){
					rc.setIndicatorString(0, "can attack: " + rc.getRoundNum());
					double danger = (dps(message[0]/100000))/(message[0]/10%10000);
					if (danger>=mostDangerous) {
						attackLocation = loc;
						mostDangerous = danger;
						
					}
				}
			}
		}
		return attackLocation;
	}
	
	/**
	 * Message Form
	 * 1. Distance, Direction, Message Type: 000,0,0 3/1/1
	 */
	/*private static void allMove(Signal[] incoming) {
		for (Signal s: incoming) {
			int m = s.getMessage()[0];
			if (m%10==2) {
				MapLocation loc = rc.getLocation();
				Direction dir;
				if (m[0])
				tryToMove(rc.getLocation().)
			}
		}
	}*/
	
	public static int castType(RobotInfo r) {
		int type = 0;
		if (r.team==rc.getTeam().opponent()) {
			if (r.type==RobotType.ARCHON) { //Archon
				type = 0;
			} else if (r.type==RobotType.ARCHON) { //Scout
				type = 1;
			} else if (r.type==RobotType.ARCHON) { //Soldier
				type = 2;
			} else if (r.type==RobotType.ARCHON) { //Guard
				type = 3;
			} else if (r.type==RobotType.ARCHON) { //Viper
				type = 4;
			} else if (r.type==RobotType.ARCHON) { //Turret
				type = 5;
			}  else if (r.type==RobotType.ARCHON) { //TTM
				type = 6;
			} 
		} else {
			if (r.type==RobotType.STANDARDZOMBIE) { //Standard
				type = 10;
			} else if (r.type==RobotType.STANDARDZOMBIE) { //Ranged
				type = 11;
			} else if (r.type==RobotType.STANDARDZOMBIE) { //Fast
				type = 12;
			} else if (r.type==RobotType.STANDARDZOMBIE) { //Big
				type = 13;
			}
		}
		return type;
	}
	
	public static double dps(int type) {

		double dmg = 0;
		int unit = type%10;
		if (type/10==0) { //enemy
			if (unit==0) { //Archon
				dmg = 0;
			} else if (unit==1) { //Scout
				dmg = 0;
			} else if (unit==2) { //Soldier
				dmg = 4./2;
			} else if (unit==3) { //Guard
				dmg = 1.5;
			} else if (unit==4) { //Viper
				dmg = 2./3+2;
			} else if (unit==5) { //Turret
				dmg = 14/3.;
			}  else if (unit==6) { //TTM
				dmg = 0;
			}
		} else if (type/10==1) { //zombie
			if (unit==0) { //Standard
				dmg = 2.5/2;
			} else if (unit==1) { //Ranged
				dmg = 3.;
			} else if (unit==2) { //Fast
				dmg = 3.;
			} else if (unit==3) { //Big
				dmg = 25./3;
			}
		}
		return dmg;
	}
	
	public static Direction randomDirection() {
		return Direction.values()[(int) (rnd.nextDouble() * 8)];
	}
	
}
