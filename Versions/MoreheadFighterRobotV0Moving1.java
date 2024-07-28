package summative;

import java.awt.Color;
import java.util.Random;

import becker.robots.*;

/**
 * Dummy robot for testing
 * @author Siodhachan Morehead
 * @version June 12, 2024
 */
public class MoreheadFighterRobotV0Moving1 extends FighterRobot{
	private int health;
	private int energy;

	/**
	 * Constructor method
	 * @param c - the City of the FighterRobot
	 * @param a - the avenue of the FighterRobot
	 * @param s - the street of the FighterRobot
	 * @param d - the Direction of the FighterRobot
	 * @param id - the ID of the FighterRobot
	 * @param health - the health of the FighterRobot
	 */
	public MoreheadFighterRobotV0Moving1(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, 4, 5, 1);
		this.health = health;
		super.setColor(Color.GRAY);
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
	 * Does not do anything yet, but will consider the results of a battle when taking actions later
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
	}

	/**
	 * Responsible for the AI of the robot
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) {
		this.energy = energy;
		
		int fightingID = -1;
		
		int aveRequest = this.getAvenue(), strRequest = this.getStreet();
		
		if(this.getDirection() == Direction.NORTH || this.getDirection() == Direction.SOUTH) {
			if(this.getDirection() == Direction.NORTH) {
				strRequest--;
			} else {
				strRequest++;
			}
		}
		
		if(this.getDirection() == Direction.EAST || this.getDirection() == Direction.WEST) {
			if(this.getDirection() == Direction.WEST) {
				aveRequest--;
			} else {
				aveRequest++;
			}
		}
		
		for(int i = 0; i < data.length; i++) {
			if(data[i].getStreet() == strRequest && data[i].getAvenue() == aveRequest) {
				if(data[i].getID() != this.getID()) {
					fightingID = data[i].getID();
				}
			}
		}
		
		return new TurnRequest(aveRequest, strRequest, fightingID, this.getAttack());
	}
	
	/**
	 * Run away to an arbitrary location if health is low
	 */
	private int [] randomMove() {
		Random generator = new Random();
		int [] requestInfo = new int[3];
		requestInfo[2] = -1;
		
		int moveAmount = this.energy/BattleManager.MOVES_ENERGY_COST;
		
		// Maximum number of moves cannot be greater than the numMoves attribute
		if(moveAmount > this.getNumMoves()) {
			moveAmount = this.getNumMoves();
		}
		
		// Randomly chooses a direction to go in 
		switch(generator.nextInt(4)) {
		case 0: // Right
			requestInfo[0] = this.getAvenue() + moveAmount;
			requestInfo[1] = this.getStreet();
			
			if(requestInfo[0] > BattleManager.WIDTH - 1) {
				requestInfo[0] = BattleManager.WIDTH-1;
			}
			break;
		case 1: //  Left
			requestInfo[0] = this.getAvenue() - moveAmount;
			requestInfo[1] = this.getStreet();
			
			if(requestInfo[0] < 0) {
				requestInfo[0] = 0;
			}
			break;
		case 2: // Up
			requestInfo[0] = this.getAvenue();
			requestInfo[1] = this.getStreet() - moveAmount;
			
			if(requestInfo[1] < 0) {
				requestInfo[1] = 0;
			}
			break;
		default: // Down
			requestInfo[0] = this.getAvenue();
			requestInfo[1] = this.getStreet() + moveAmount;
			
			if(requestInfo[1] > BattleManager.HEIGHT - 1) {
				requestInfo[1] = BattleManager.HEIGHT - 1;
			}
			break;
		}
		
		return requestInfo;
	}

	/**
	 * Moves the robot to a specified location
	 * @param a - the avenue to go to
	 * @param s - the street to go to
	 */
	public void goToLocation(int a, int s) {
		System.out.println("\nTRYING TO MOVE TO: STREET: " + s + " AVENUE: " + a);

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

		if(this.frontIsClear()) {
			this.move(Math.abs(this.getStreet() - s));
		}
	}

	/**
	 * Faces the robot in a certain direction with the least amount of turns
	 * @param dir - the direction to be faced 
	 */
	private void turnDirection(Direction dir) {
		// If the robot is not already facing the requested direction
		if(this.getDirection() != dir) {
			// If robot is to face east, eORwest, OR north, OR south
			switch (dir) {
			case EAST:
				// If the robot is facing west, or north, or south
				if(this.getDirection() == Direction.WEST) {
					this.turnAround();
				} else if(this.getDirection() == Direction.NORTH) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
				break;

			case WEST:
				// If the robot is facing east, or south, or west
				if(this.getDirection() == Direction.EAST) {
					this.turnAround();
				} else if(this.getDirection() == Direction.SOUTH) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
				break;

			case NORTH:
				// If the robot is facing south, or west, or east
				if(this.getDirection() == Direction.SOUTH) {
					this.turnAround();
				} else if(this.getDirection() == Direction.WEST) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
				break;

			default:
				// If the robot is facing north, or east, or west
				if(this.getDirection() == Direction.NORTH) {
					this.turnAround();
				} else if(this.getDirection() == Direction.EAST) {
					this.turnRight();
				} else {
					this.turnLeft();
				}
				break;
			}
		}
	}
}