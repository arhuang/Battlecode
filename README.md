# Battlecode
MIT Battlecode 2016 Competition
## Overview
The point of the competition was to code an AI player to control specific units in order to fight against an enemy team as well as neutral zombies. Each team got a few archons, which served as mobile bases that were able to spawn units. The game was won when all the opposing team's archons were destroyed, or if time ran out, a tiebreaker based on how efficient each team was, i.e. the worth of units left.

## Units
### Archon
The main unit of the game, able to spawn new units, pick up resources, and mine through walls. Able to send signals to units in a small radius. Unable to attack.
### Turret
Long range, High damage unit of the game, needs to set up before able to attack, cannot attack closer than a certain range.
### Soldier
Mobile, versatile unit of the game. Medium range, Medium damage.
### Scout
Fast unit of the game, can send signals to units within a large radius, cannot attack

## Strategy
Our strategy was to move all archons together, and then create turrets in a formation, with scouts interspersed in the formation, and soldiers circling the formation. Every time the archons built a new turret, the formation would expand out. The scouts were included in order to give range to the turrets i.e. spot enemy units. The soldiers circling the formation were meant to give the turrets some buffer room, as well as kite the enemies around the formation so that the turrets could continuously fire on the enemies.
