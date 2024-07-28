package summative;

/**
 * Extends OppData to store more data about opponents
 * @author Siodhachan Morehead
 * @version June 14, 2024
 */
public class MoreheadOppData extends OppData{
	private int healthLostAgainst;
	private int oppHealthLost;
	private int attack;
	private int defense;
	private int agility;
	
	/**
	 * Constructor method
	 * @param id - the ID of this opponent
	 * @param a - the avenue of this opponent
	 * @param s - the street of this opponent
	 * @param health - the health of this opponent
	 * @param healthLostAgainst - health lost against this opponent (only used for V4)
	 * @param oppHealthLost - opponent health lost (only used for V4)
	 * @param stats - array storing all the stats of the opponent
	 */
	public MoreheadOppData(int id, int a, int s, int health, int healthLostAgainst, int oppHealthLost, int [] stats) {
		super(id, a, s, health);
		this.healthLostAgainst = healthLostAgainst;
		this.oppHealthLost = oppHealthLost;
		
		this.attack = stats[0];
		this.defense = stats[1];
		this.agility = stats[2];
		
	} 
	
	/**
	 * Constructor method that takes in an OppData object and retrieves its data
	 * @param data - the OppData object
	 * @param healthLostAgainst - health lost against this opponent (V4 only)
	 * @param oppHealthLost - how much health this opponent has lost (V4 only)
	 * @param stats - the stat array of this opponent
	 */
	public MoreheadOppData(OppData data, int healthLostAgainst, int oppHealthLost, int [] stats) {
		super(data.getID(), data.getAvenue(), data.getStreet(), data.getHealth());
		
		// These two variables are only needed for V5 which uses an advantage factor
		this.healthLostAgainst = healthLostAgainst;
		this.oppHealthLost = oppHealthLost;
		
		// Getting previously stored stats
		this.attack = stats[0];
		this.defense = stats[1];
		this.agility = stats[2];
	}

	/**
	 * Retrieves a double that represents the player/opponent's advantage
	 * Only used for testing in V4, have to keep this here in order for V4 to work
	 * @return - the advantage factor, which is a ratio between the amount of health this opponent has lost vs how much the other opponent has lost
	 */
	public double getAdvantage() {
		// If this opponent or the other opponent has lost any health
		if(this.oppHealthLost + this.healthLostAgainst > 0) {
			return 100.0 * (((double)this.oppHealthLost / (double)this.healthLostAgainst) / ((double)this.oppHealthLost + (double)this.healthLostAgainst));
		} else {
			return 5;
		}
	}

	/**
	 * Adds a fight between another opponent to this opponent's data
	 * @param healthLost - health lost against this opponent (V4 only)
	 * @param oppHealthLost - how much health this opponent lost (V4 only)
	 * @param numRounds - the number of rounds the fight between opponents lasted
	 * @param defending - whether or not this opponent is defending
	 */
	public void addFight(int healthLost, int oppHealthLost, int numRounds, boolean defending) {
		this.healthLostAgainst += healthLost;
		this.oppHealthLost += oppHealthLost;
		
		// If this opponent is not defending
		if(!defending) {
			// If the number of rounds engaged by this oopponent is greater than its current attack, then update it
			if(numRounds > this.attack) {
				this.attack = numRounds;
			}
		}
	}
	
	/**
	 * Updates this oppponents agility stat
	 * @param prevAvenue - the previoius avenue of this opponent
	 * @param prevStreet - the previous street of this opponent
	 */
	public void updateAgility(int prevAvenue, int prevStreet) {
		int distance = Math.abs(prevAvenue - this.getAvenue()) + Math.abs(prevStreet - this.getStreet());

		// If the distance travelled is greater than this opponent's current agility, update it
		if(distance > this.agility) {
			this.agility = distance;
		}
		
		// If the robot's agility is outside of the accepted range, set it to 0
		if(this.agility > 6 || this.agility < 0) {
			this.agility = 0;
		}
		
		// If the the sum of this opponent's attack and agility is greater or equal to four, the defence can be found
		if(this.attack + this.agility >= 4) {
			this.defense = 10 - this.attack - this.agility;
		}
	}
	
	/**
	 * Gets the stats of this an opponent
	 * @return an integer array of this opponent's stats in the order (attack, defence, agility)
	 */
	public int [] getStats() {
		int [] stats = {this.attack, this.defense, this.agility};
		return stats;
	}
	
	/**
	 * Gets the health lost against this opponent
	 * @return how much health has been lost against this opponent
	 */
	public int getHealthLost() {
		return this.healthLostAgainst;
	}
	
	/**
	 * Gets the health that this opponent has lost
	 * @return how much health this opponent lost
	 */
	public int getOppHealthLost() {
		return this.oppHealthLost;
	}
}
