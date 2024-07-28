package summative;

import java.awt.Color;

import becker.robots.*;

import java.util.Random;

/**
 * Attack: Attack the opponent with the best health/distance ratio when health & energy is high enough, considers stats of opponents and goes for one with highest defense and lowest attack
 * Defend: Run away when too much health lost, changes depending on energy as well
 * Movement: Attacks the robot with the best health/distance ratio when has enough health, and runs away when it does not
 * @author Siodhachan Morehead
 * @version June 14, 2024
 */
public class MoreheadFighterRobotV5 extends FighterRobot {
	private int health;
	private int energy;
	private int turnCount;
	private boolean attacking;
	
	private MoreheadOppData [] opponents;
	/**
	 * Constructor method
	 * @param c - the City of the FighterRobot
	 * @param a - the avenue of the FighterRobot
	 * @param s - the street of the FighterRobot
	 * @param d - the Direction of the FighterRobot
	 * @param id - the ID of the FighterRobot
	 * @param health - the health of the FighterRobot
	 */
	public MoreheadFighterRobotV5(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, 5, 4, 1);
		this.health = health;
		this.energy = 100;
		this.attacking = false;
		this.setColor(Color.MAGENTA);
		this.setLabel();
		
		this.opponents = new MoreheadOppData[BattleManager.NUM_PLAYERS];
		
		// Initializing MoreheadOppData array
		for(int i = 0; i < this.opponents.length; i++) {
			this.opponents[i] = new MoreheadOppData(i, 0, 0, 0, 0, 0, new int [] {0, 0, 0});
		}
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
	 * Considers the result of the previous battle when making decisions
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		// Updates health
		this.health -= healthLost;
		
		// Adding information from battleResult to opponent data
		for(int i = 0; i < this.opponents.length; i++) {
			if(this.opponents[i].getID() == oppID) {
				this.opponents[i].addFight(healthLost, oppHealthLost, numRoundsFought, this.attacking);
			}
		}
	}

	/**
	 * Responsible for the AI of the robot
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) {
		// Variables to store data from the previous rounds for a single robot
		int prevHealthLost = 0;
		int prevOppHealthLost = 0;
		int prevNumRounds = 0;
		int prevAvenue = 0;
		int prevStreet = 0;

		// Retrieving data from previous round such that MoreheadOppData can take in the new data as well as keep the old data
		for(int i = 0; i < data.length; i++) {
			prevHealthLost = this.opponents[i].getHealthLost();
			prevOppHealthLost = this.opponents[i].getOppHealthLost();
			
			prevAvenue = this.opponents[i].getAvenue();
			prevStreet = this.opponents[i].getStreet();

			int [] prevStats = this.opponents[i].getStats();
			
			this.opponents[i] = new MoreheadOppData(data[this.opponents[i].getID()], prevHealthLost, prevOppHealthLost, prevStats);
			
			// Used for the starting round, will cause slight logic error in certain situations so fix later
			if(prevAvenue != 0 && prevStreet != 0) {
				this.opponents[i].updateAgility(prevAvenue, prevStreet);
			}
		}

		// Updating energy
		this.energy = energy;

		// Misc. Constants
		final double LOW_HEALTH = 15;
		final double LOW_ENERGY = 25;

		// Constants for the modes of the robot
		final int ATTACK = 0;
		final int HEALTH_LOW = 1;
		final int ENERGY_LOW = 2;

		// Determines which actions to take
		int currentMode = 0;

		// Variables for factors
		double health_factor = 0.0;
		double distance_factor = 0.0;

		// Attack, Defense, Agility
		double [] stat_factor = new double[3];

		// Resetting turnCount if the robot has not ran in a while
		if(this.turnCount > 10) {
			this.turnCount = 0;
		}

		// Choosing factors based on mode
		if(this.health < LOW_HEALTH && this.turnCount < 3) {
			this.turnCount++;
			currentMode = HEALTH_LOW;
		} else if(this.energy <= LOW_ENERGY) {
			health_factor = 0.1;
			distance_factor = 0.3;

			stat_factor[0] = 0.3;
			stat_factor[1] = 0.2;
			stat_factor[2] = 0.1;
			turnCount++;
			this.sortByHealthDistStats(health_factor, distance_factor, stat_factor);
			currentMode = ENERGY_LOW;
		} else {
			health_factor = 0.2;
			distance_factor = 0.1;

			stat_factor[0] = 0.2;
			stat_factor[1] = 0.4;
			stat_factor[2] = 0.1;
			turnCount++;
			this.sortByHealthDistStats(health_factor, distance_factor, stat_factor);
			currentMode = ATTACK;
		}

		int [] requestInfo= new int[3];

		//		for(int i = 0; i < data.length; i++) {
		//			int distance = (int) Math.round(((Math.abs(this.getStreet() - data[i].getStreet()) + Math.abs(this.getAvenue() - data[i].getAvenue())) / 30.0) * 100);
		//			int healthDist = (int)Math.round((health_factor * (100 - data[i].getHealth())) + (distance_factor * (100 - distance)));
		//			System.out.println(data[i].getID() + ":\tFACTOR: " + healthDist + "\tHEALTH: " + data[i].getHealth() + "\tDISTANCE: " + distance);
		//		}

		// State system which toggles between different modes
		switch(currentMode) {
		case ATTACK:
			requestInfo = this.attackMode(this.opponents);
			break;

		case HEALTH_LOW:
			requestInfo = this.healthLowMode();
			break;

		case ENERGY_LOW:
			requestInfo = this.energyLowMode(this.opponents);
			break;

		default:
			System.out.println("No valid modes selected.");
			break;
		}
		
		if(requestInfo[2] != -1) {
			this.attacking = true;
		} else {
			this.attacking = false;
		}

		return new TurnRequest(requestInfo[0], requestInfo[1], requestInfo[2], this.getAttack());
	}

	/**
	 * Sorts an array of OppData objects based on a health/distance ratio depending on weighing factors
	 * @param data - the array of OppData objects
	 * @param health_factor - the weighing factor (out of 1) for the opponents health
	 * @param dist_factor - the weighing factor (out of 1) for the opponents distance
	 */
	private void sortByHealthDistStats(double health_factor, double dist_factor, double [] stat_factors) {
		// Health and distance ratios
		double opponentFactor1 = 0;
		double opponentFactor2 = 0;

		// Health and distance values in percent
		double health = 0;
		double dist = 0;

		// Stats related variables 
		double stats = 0;
		int [] oppStats = new int[3];

		// Distance
		double distance = 0;

		MoreheadOppData temp;

		// For every opponent
		for(int i = 0; i < this.opponents.length; i++) {
			// For every opponent after the selected opponent
			for(int n = i; n < this.opponents.length; n++) {
				// Selected opponent in the array
				distance = Math.abs(this.opponents[i].getStreet() - this.getStreet()) + Math.abs(this.opponents[i].getAvenue() - this.getAvenue());
				distance = 100 - ((distance / 30.0) * 100);
				
				health = health_factor * (100 - this.opponents[i].getHealth());
				dist = dist_factor * distance;
				
				oppStats = this.opponents[i].getStats();
				
				stats = 100 * (stat_factors[0] * ((6 - oppStats[0]) / 6.0) + stat_factors[1] * ((6 - oppStats[1]) / 6.0) + stat_factors[2] * ((oppStats[2]) / 6.0));
				
				opponentFactor1 = health + dist + stats;

				// New opponent in the array
				distance = Math.abs(this.opponents[n].getStreet() - this.getStreet()) + Math.abs(this.opponents[n].getAvenue() - this.getAvenue());
				distance = 100 - ((distance / 30.0) * 100);
				
				health = health_factor * (100 - this.opponents[n].getHealth());
				dist = dist_factor * distance;
				
				oppStats = this.opponents[n].getStats();
				
				stats = 100 * (stat_factors[0] * ((6 - oppStats[0]) / 6.0) + stat_factors[1] * ((6 - oppStats[1]) / 6.0) + stat_factors[2] * ((oppStats[2]) / 6.0));
				
				opponentFactor2 = health + dist + stats;

				// If the health/distance ratio of the new opponent is better than the currently selected one
				if(opponentFactor2 > opponentFactor1) {
					temp = this.opponents[i];
					this.opponents[i] = this.opponents[n];
					this.opponents[n] = temp;
				}
			}
		}
	}

	/**
	 * Run away to an arbitrary location if health is low
	 */
	private int [] healthLowMode() {
		Random generator = new Random();
		int [] requestInfo = new int[3];
		requestInfo[2] = -1;
		
		int maxMoves = this.energy/BattleManager.MOVES_ENERGY_COST;
		
		// Maximum number of moves cannot be greater than the numMoves attribute
		if(maxMoves > this.getNumMoves()) {
			maxMoves = this.getNumMoves();
		}
		
		int moveAmount = generator.nextInt(maxMoves + 1);
		
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

	private int [] energyLowMode(MoreheadOppData [] opponents) {
		int [] requestInfo = new int[3];
		requestInfo = this.attackMode(opponents);

		return requestInfo;
	}

	/**
	 * ATTACK MODE: When health & energy is high enough, attack the robot with the best health/distance ratio
	 * @param data - data of all opponents
	 * @return - the location and ID of the robot to be attacked
	 */
	private int[] attackMode(MoreheadOppData [] opponents) {
		int fightingID = -1;

		// Finds the lowest health opponent and sets it to the fighting id
		for(int i = 0; i < opponents.length; i++) {
			// Finds the lowest health opponent and sets it to the fighting id
			if(opponents[i].getID() != this.getID() && opponents[i].getHealth() > 0) {
				fightingID = opponents[i].getID();
				break;
			}
		}

		int [] opponentLocation = new int[2];

		for(int i = 0; i < opponents.length; i++) {
			if(fightingID == opponents[i].getID()) {
				opponentLocation[0] = opponents[i].getAvenue();
				opponentLocation[1] = opponents[i].getStreet();
			}
		}

		int [] opponentInfo = this.moveTo(opponentLocation);

		opponentInfo[2] = fightingID;

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

		int moves = this.energy/BattleManager.MOVES_ENERGY_COST;
		
		// Maximum number of moves cannot be greater than the numMoves attribute
		if(moves > this.getNumMoves()) {
			moves = this.getNumMoves();
		}
		
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

		System.out.format("Requesting %d moves... (max is %d)%n", Math.abs(strRequest - currentStr) + Math.abs(aveRequest - currentAve), this.getNumMoves());
		

		return new int [] {aveRequest, strRequest, 0};
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