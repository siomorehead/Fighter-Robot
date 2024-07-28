package summative;

import java.awt.Color;

import becker.robots.*;

/**
 * Attack: Fight the robot with the best health/distance factor, never run away
 * Defend: No defence techniques
 * Movement: Go to the robot with the best health/distance factor
 * @author Siodhachan Morehead
 * @version June 2, 2024
 */
public class MoreheadFighterRobotV2 extends FighterRobot{
	private int health;

	/**
	 * Constructor method
	 * @param c - the City of the FighterRobot
	 * @param a - the avenue of the FighterRobot
	 * @param s - the street of the FighterRobot
	 * @param d - the Direction of the FighterRobot
	 * @param id - the ID of the FighterRobot
	 * @param health - the health of the FighterRobot
	 */
	public MoreheadFighterRobotV2(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, 4, 5, 1);
		this.health = health;
		super.setColor(Color.CYAN);
		super.setLabel(this.getID() + ": " + this.health);
	}

	/**
	 * Displays the health and id of the robot and turns it black if its health is less than 0
	 */
	public void setLabel() {
		if(this.health > 0) {
			super.setLabel(this.getID() + ": " + this.health);
		} else {
			this.setColor(Color.BLACK);
		}
	}

	/**
	 * Retrieves results from a battle with an opponent
	 * @param healthLost - amount of health this robot has lost
	 * @param oppID - the ID of the opponent
	 * @param oppHealthLost - amount of health opponent has lost
	 * @param numRoundsFought - the number of rounds fought with the opponent
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
	}

	/**
	 * Responsible for the AI of the robot
	 * @param energy - the updated energy of this robot
	 * @param data - the OppData array of opponents
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) {
		// Misc. Constants
		final double LOW_HEALTH = 20;

		// Constants for the modes of the robot
		final int ATTACK = 0;
		final int HEALTH_LOW = 1;

		// Determines which actions to take
		int currentMode = 0;

		// Variables for factors
		double health_factor = 0.0;
		double distance_factor = 0.0;
		
		// Choosing factors based on mode
		if(this.health < LOW_HEALTH) {
			health_factor = 0.3;
			distance_factor = 0.7;
			currentMode = HEALTH_LOW;
		} else {
			health_factor = 0.6;
			distance_factor = 0.4;
			this.sortByHealthDist(data, health_factor, distance_factor);
		}

		// Information about opponent location and id
		int [] opponentInfo= new int[3];

		// State system which toggles between different modes
		switch(currentMode) {
		case ATTACK:
			opponentInfo = this.attackMode(data);
			break;

		case HEALTH_LOW:
			this.healthLowMode();
			break;

		default:
			//System.out.println("No valid modes selected.");
			break;
		}

		// Information about movement
		// System.out.format("Current Location:\nStreet: %d\tAvenue: %d%n", currentStr, currentAve);

		return new TurnRequest(opponentInfo[0], opponentInfo[1], opponentInfo[2], this.getAttack());
	}

	/**
	 * Sorts an array of OppData objects based on a health/distance ratio depending on weighing factors
	 * @param data - the array of OppData objects
	 * @param health_factor - the weighing factor (out of 1) for the opponents health
	 * @param dist_factor - the weighing factor (out of 1) for the opponents distance
	 */
	private void sortByHealthDist(OppData[] data, double health_factor, double dist_factor) {
		// Health/distance ratio of selected and new robot
		double healthDist1 = 0;
		double healthDist2 = 0;

		// Distance of the robot
		double distance = 0;

		// Temp OppData object
		OppData temp;

		// For every opponent
		for(int i = 0; i < data.length; i++) {
			// For every opponent after the selected opponent
			for(int n = i; n < data.length; n++) {
				// Getting distance and health/distance ratio of selected opponent
				distance = Math.abs(data[i].getStreet() - this.getStreet()) + Math.abs(data[i].getAvenue() - this.getAvenue());
				distance = 100 - ((distance / 30.0) * 100);
				healthDist1 = (health_factor * (100 - data[i].getHealth())) + (dist_factor * distance);

				// Getting distance and health/distance ratio of new opponent
				distance = Math.abs(data[n].getStreet() - this.getStreet()) + Math.abs(data[n].getAvenue() - this.getAvenue());
				distance = 100 - ((distance / 30.0) * 100);
				healthDist2 = (health_factor * (100 - data[n].getHealth())) + (dist_factor * distance);

				// If the health/distance ratio of the new opponent is better than the currently selected one
				if(healthDist2 > healthDist1) {
					temp = data[i];
					data[i] = data[n];
					data[n] = temp;
				}
			}
		}

	}

	/**
	 * Mode for when robot has low health
	 */
	private void healthLowMode() {

	}

	/**
	 * ATTACK MODE: When health & energy is high enough, attack the robot with the best health/distance ratio
	 * @param data - data of all opponents
	 * @return - the location and ID of the robot to be attacked
	 */
	private int[] attackMode(OppData[] data) {
		int fightingID = -1;

		// Finds the lowest health opponent and sets it to the fighting id
		for(int i = 0; i < data.length; i++) {
			// Finds the lowest health opponent and sets it to the fighting id
			if(data[i].getID() != this.getID() && data[i].getHealth() > 0) {
				fightingID = data[i].getID();
				break;
			}
		}
		
		// Array for the location of the opponent
		int [] opponentLocation = new int[2];
		
		// Gets the location of the chosen opponent
		for(int i = 0; i < data.length; i++) {
			if(fightingID == data[i].getID()) {
				opponentLocation[0] = data[i].getAvenue();
				opponentLocation[1] = data[i].getStreet();
			}
		}

		// Requested information (opponent location, id)
		int [] opponentInfo = this.moveTo(opponentLocation);

		// Opponent ID = fightingID
		opponentInfo[2] = fightingID;
		
		// Only fighting the opponent if this robot and the opponent are at the same location
		if(opponentLocation[0] != opponentInfo[0] || opponentLocation[1] != opponentInfo[1]) {
			opponentInfo[2] = -1;
		}
		
		return opponentInfo;
	}

	/**
	 * Determines the closest the robot can get to an opponents location
	 * @param currentLocation
	 * @param opponentLocation
	 * @return - a location closest to the opponent location that can be reached 
	 */
	private int [] moveTo(int [] opponentLocation) {
		// Current location
		int currentAve = this.getAvenue();
		int currentStr = this.getStreet();

		// Opponent location
		int opponentAve = opponentLocation[0];
		int opponentStr = opponentLocation[1];

		// Requested location
		int aveRequest = currentAve;
		int strRequest = currentStr;

		// Max moves, moves done, and distance
		int numMoves = this.getNumMoves();
		int moves = numMoves;
		int distance = 0;

		// If the opponent is to the right, else left
		if(currentAve < opponentAve) {
			distance = opponentAve - currentAve;

			// If the distance can be reached within the number of available moves
			if(distance <= moves) {
				aveRequest += distance;
				moves -= distance;
			} else {
				aveRequest += moves;
				moves = 0;
			}

		} else {
			distance = currentAve - opponentAve;

			// If the distance can be reached within the number of available moves
			if(distance <= moves) {
				aveRequest -= distance;
				moves -= distance;
			} else {
				aveRequest -= moves;
				moves = 0;
			}
		}
		
		// If the opponent is below
		if(currentStr < opponentStr) {
			distance = opponentStr - currentStr;

			// If the distance can be reached within the number of available moves
			if(distance <= moves) {
				strRequest += distance;
				moves -= distance;
			} else {
				strRequest += moves;
				moves = 0;
			}
		} else {
			distance = currentStr - opponentStr;

			// If the distance can be reached within the number of available moves
			if(distance <= moves) {
				strRequest -= distance;
				moves -= distance;
			} else {
				strRequest -= moves;
				moves = 0;
			}
		}
		
		// Used to debug when the robot is not moving
//		if(aveRequest == currentAve && strRequest == currentStr) {
//			System.out.println("\nROBOT NOT MOVING. HERE ARE THE FOLLOWING REASONS");
//			System.out.println("-------------------------------------------------");
//			System.out.println("Current Avenue: " + currentAve + "\tOther Current Avenue: " + this.getAvenue());
//			System.out.println("Current Street: " + currentStr + "\tOther Current Street: " + this.getStreet());
//			System.out.println("moves: " + moves + "\tnumMoves: " + numMoves);
//			System.out.println("Opponent Avenue: " + opponentAve + "\tOpponent Street: " + opponentStr + "\n");
//		}

		// System.out.format("Requesting %d moves... (max is %d)%n", Math.abs(strRequest - currentStr) + Math.abs(aveRequest - currentAve), this.getNumMoves());

		return new int [] {aveRequest, strRequest, 0};
	}

	/**
	 * Moves the robot to a specified location
	 * @param a - the avenue to go to
	 * @param s - the street to go to
	 */
	public void goToLocation(int a, int s) {
		// System.out.println("\nTRYING TO MOVE TO: STREET: " + s + " AVENUE: " + a);

		// If the robot is to the left of the specified location
		if(this.getAvenue() - a < 0) {
			this.turnDirection(Direction.EAST);
		} else if(this.getAvenue() - a > 0){
			this.turnDirection(Direction.WEST);
		}

		this.move(Math.abs(this.getAvenue() - a));

		// If the robot above the specified location
		if(this.getStreet() - s < 0) {
			this.turnDirection(Direction.SOUTH);
		} else if (this.getStreet() - s > 0){
			this.turnDirection(Direction.NORTH);
		}

		this.move(Math.abs(this.getStreet() - s));
	}

	/**
	 * Faces the robot in a certain direction with the least amount of turns
	 * @param dir - the direction to be faced 
	 */
	private void turnDirection(Direction dir) {
		// If robot is to face east, eORwest, OR north, OR south
		if(dir == Direction.EAST ) {
			// If the robot is not already facing east
			if(this.getDirection() != Direction.EAST) {
				// If the robot is facing west, or north, or south
				if(this.getDirection() == Direction.WEST) {
					this.turnAround();
				} else if(this.getDirection() == Direction.NORTH) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
			}
		} else if(dir == Direction.WEST) {
			// If the robot is not already facing west
			if(this.getDirection() != Direction.WEST) {
				// If the robot is facing east, or south, or west
				if(this.getDirection() == Direction.EAST) {
					this.turnAround();
				} else if(this.getDirection() == Direction.SOUTH) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
			}
		} else if(dir == Direction.NORTH) {
			// If the robot is not already facing north
			if(this.getDirection() != Direction.NORTH) {
				// If the robot is facing south, or west, or east
				if(this.getDirection() == Direction.SOUTH) {
					this.turnAround();
				} else if(this.getDirection() == Direction.WEST) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
			}
		} else {
			// if the robot is not already facing south
			if(this.getDirection() != Direction.SOUTH) {
				// If the robot is facing north, or east, or west
				if(this.getDirection() == Direction.NORTH) {
					this.turnAround();
				} else if(this.getDirection() == Direction.EAST) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
			}
		}
	}
}