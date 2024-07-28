package summative;

import java.awt.Color;

import becker.robots.*;

/**
 * Dummy robot for testing
 * @author Siodhachan Morehead
 * @version June 12, 2024
 */
public class MoreheadFighterRobotV0Attacking extends FighterRobot{
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
	public MoreheadFighterRobotV0Attacking(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, 6, 3, 1);
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
		int fightingID = -1;
		
		for(int i = 0; i < data.length; i++) {
			if(data[i].getStreet() == this.getStreet() && data[i].getAvenue() == this.getAvenue()) {
				if(data[i].getID() != this.getID()) {
					fightingID = data[i].getID();
				}
			}
		}
		
		return new TurnRequest(this.getAvenue(), this.getStreet(), fightingID, this.getAttack());
	}

	/**
	 * Dummy robot cannot move
	 * @param a - the requested avenue
	 * @param s - the requested street
	 */
	public void goToLocation(int a, int s) {
		// Stub
	}
}
