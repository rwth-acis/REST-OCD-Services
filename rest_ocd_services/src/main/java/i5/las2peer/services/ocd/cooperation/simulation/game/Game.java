package i5.las2peer.services.ocd.cooperation.simulation.game;

import java.io.Serializable;

import i5.las2peer.services.ocd.cooperation.simulation.Agent;
import sim.util.Bag;

/**
 * Provides the functions to determine the new payoff values for a agent.
 *
 */
public class Game implements Serializable {

	private static final long serialVersionUID = 1L;

	/////////////// Attributes ///////////////

	/**
	 * the payoff a agent get if he plays strategy A against strategy A
	 */
	private double payoffAA;
	/**
	 * the payoff a agent get if he plays strategy A against strategy B
	 */
	private double payoffAB;
	/**
	 * the payoff a agent get if he plays strategy B against strategy A
	 */
	private double payoffBA;
	/**
	 * the payoff a agent get if he plays strategy B against strategy B
	 */
	private double payoffBB;

	private final boolean STRATEGY_A = true; // cooperate
	private final boolean STRATEGY_B = false; // defect

	/////////////// Constructor ///////////////////

	/**
	 * Initialize the Game with default payoff parameters
	 */
	protected Game() {
		this(1.0, 0.0, 1.0, 0.0);
	}
	
	/**
	 * Initialize with the specified payoff parameters
	 */
	protected Game(double aa, double ab, double ba, double bb) {

		this.payoffAA = aa;
		this.payoffAB = ab;
		this.payoffBA = ba;
		this.payoffBB = bb;
	}

	/////////////// Methods ///////////////////

	/**
	 * Determine the total Payoff for a agent of all neighbour games
	 * 
	 * @param agent
	 * @param neighbours
	 * @return payoff
	 */
	public double getPayoff(Agent agent) {

		Bag neighbours = agent.getNeighbourhood();
		double payoff = 0.0;
		for (int i = 0, si = neighbours.size(); i < si; i++) {
			Agent neighbour = (Agent) neighbours.get(i);
			payoff += getPayoff(agent.getStrategy(), neighbour.getStrategy());
		}

		return payoff;
	}

	/**
	 * Determine the Payoff between two Strategies
	 * 
	 * @param myStrategy
	 * @param otherStrategy
	 * @return payoff
	 */
	public double getPayoff(boolean myStrategy, boolean otherStrategy) {

		if (myStrategy == STRATEGY_A) {
			if (otherStrategy == STRATEGY_A) {
				return payoffAA;
			} else {
				return payoffAB;
			}
		}
		if (otherStrategy == STRATEGY_A) {
			return payoffBA;
		}
		return payoffBB;
	}

	///// Getter /////

	/**
	 * @return GameType of this game
	 */
	public GameType getGameType() {
		return GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
	}

	public double getPayoffAA() {
		return payoffAA;
	}

	public double getPayoffAB() {
		return payoffAB;
	}

	public double getPayoffBA() {
		return payoffBA;
	}

	public double getPayoffBB() {
		return payoffBB;
	}

	///// Setter /////

	public void setPayoffAA(double payoffAA) {
		this.payoffAA = payoffAA;
	}

	public void setPayoffAB(double payoffAB) {
		this.payoffAB = payoffAB;
	}

	public void setPayoffBA(double payoffBA) {
		this.payoffBA = payoffBA;
	}

	public void setPayoffBB(double payoffBB) {
		this.payoffBB = payoffBB;
	}

}
