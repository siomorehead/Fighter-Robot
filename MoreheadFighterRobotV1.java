package summative;

import java.awt.Color;

import becker.robots.*;

/**
 * Attack: Fight the robot with the least health
 * Defend: Defend every time
 * Movement: Go straight to the robot with the least health
 * @author Siodhachan Morehead
 * @version May 27, 2024
 */
public class MoreheadFighterRobotV1 extends FighterRobot{
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
	public MoreheadFighterRobotV1(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, 4, 5, 1);
		this.health = health;
		this.setColor(Color.WHITE);
		this.setLabel(this.getID() + ": " + this.health);
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
	 * Does not do anything yet, but will consider the results of a battle when taking actions later
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
	}

	/**
	 * Responsible for the AI of the robot
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) {
		// Arrays for opponent info
		int [] healths = new int[data.length];
		int [] ids = new int[data.length];
		
		// Fight and movement information
		int fightingID = -1;
		int numMoves = this.getNumMoves();
		int moves = 0;

		// Location of the opponent and self
		int oppStr = 0, oppAve = 0;
		int currentStr = this.getStreet(), currentAve = this.getAvenue();
		
		// The street to be requested
		int strRequest = currentStr, aveRequest = currentAve;

		// Gets opponent data
		for(int i = 0; i < data.length; i++) {
			healths[i] = data[i].getHealth();
			ids[i] = data[i].getID();
		}

		// Uses selection sort to find the lowest health, while keeping track of the id for that health
		int temp1 =  0;
		int temp2 = 0;

		// For every index
		for(int i = 0; i < healths.length; i++) {
			// For every index after the previous index
			for(int n = i+1; n < healths.length; n++) {
				// If the index after the original index is smaller, switch their positions
				if(healths[n] < healths[i]) {
					temp1 = healths[i];
					temp2 = ids[i];
					healths[i] = healths[n];
					ids[i] = ids[n];
					healths[n] = temp1;
					ids[n] = temp2;
				}
			}
		}
		
		// Gets the opponent to fight
		for(int i = 0; i < ids.length; i++) {
			// Finds the lowest health opponent and sets it to the fighting id
			if(ids[i] != this.getID() && healths[i] > 0) {
				fightingID = ids[i];
				break;
			}
		}
		
		// Setting opponent location to the location of the desired opponent to fight 
		oppAve = data[fightingID].getAvenue();
		oppStr = data[fightingID].getStreet();
		
		// If opponent is to the right
		if(currentAve <= oppAve) {
			// If the opponent's avenue can be reached in the number of moves
			if(oppAve - currentAve <= numMoves) {
				aveRequest = oppAve;
				moves += oppAve - currentAve;
			} else {
				aveRequest += numMoves;
				moves += numMoves;
			}
		} else {
			// If the opponent's avenue can be reached in the number of moves
			if(currentAve - oppAve <= numMoves) {
				aveRequest = oppAve;
				moves += currentAve - oppAve;
			} else {
				aveRequest -= numMoves;
				moves += numMoves;
			}
		}
		
		// Handles moving up and down
		if(numMoves - moves > 0 ) {
			numMoves = numMoves - moves;
			// If the opponent is below 
			if(currentStr <= oppStr) {
				// If the opponent's street can be reached in the number of moves
				if(oppStr - currentStr<= numMoves) {
					strRequest = oppStr;
					moves += oppStr - currentStr;
				} else {
					strRequest += numMoves;
					moves += numMoves;
				}
			} else {
				// If the opponent's street can be reached in the number of moves
				if(currentStr - oppStr <= (numMoves - moves)) {
					strRequest = oppStr;
					moves += currentStr - oppStr;
				} else {
					strRequest -= numMoves;
					moves += numMoves;
				}
			}
		}
		
		if(aveRequest != oppAve || strRequest != oppStr) {
			fightingID = -1;
		}
		
		// Information about movement
		//System.out.format("Requesting %d moves... (max is %d)%n", moves, this.getNumMoves());
		//System.out.format("Current Location:\nStreet: %d\tAvenue: %d%n", currentStr, currentAve);

		return new TurnRequest(aveRequest, strRequest, fightingID, this.getAttack());
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
