package i5.las2peer.services.ocd.cd.simulation.dynamic;

import ec.util.MersenneTwisterFast;
import i5.las2peer.services.ocd.cd.simulation.Agent;
import i5.las2peer.services.ocd.cd.simulation.Simulation;

/**
 * Implementation of the spatial Replicator update dynamic
 * 
 */
public class Replicator extends Dynamic {

	private static final long serialVersionUID = 1L;

	/////////////// Constructor ////////////
	
	public Replicator() {

	}
	
	protected Replicator(double value) {
		super(value);
	}

	/////////////// Methods /////////////////

	/**
	 * 
	 * Determine the new strategy of a agent by calling
	 * {@link #getNewStrategy(boolean, boolean, double, double, MersenneTwisterFast, int, int, double)}
	 * 
	 * @param agent
	 * @param simulation
	 *
	 * @return new strategy
	 */
	@Override
	public boolean getNewStrategy(Agent agent, Simulation simulation) {

		int round = simulation.getRound() - 1;
		MersenneTwisterFast random = simulation.random;

		Agent neighbour = agent.getRandomNeighbour(random);
		if (neighbour == null)
			return agent.getStrategy(round);

		boolean myStrategy = agent.getStrategy(round);
		boolean otherStrategy = neighbour.getStrategy(round);
		double myPayoff = agent.getPayoff(round);
		double otherPayoff = neighbour.getPayoff(round);
		int myNeighSize = agent.getNeighbourhood().size();
		int otherNeighSize = neighbour.getNeighbourhood().size();
		double value = getValues()[0];

		return getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize, otherNeighSize,
				value);
	}

	/**
	 * compare a agent with his neighbour to determine his new strategy
	 * 
	 * @param myStrategy
	 *            agent strategy
	 * @param otherStrategy
	 *            neighbour strategy
	 * @param myPayoff
	 *            agent payoff
	 * @param otherPayoff
	 *            neighbour payoff
	 * @param random
	 *            random number generator
	 * @param myNeighSize
	 *            agent neighbourhood size
	 * @param otherNeighSize
	 *            neighbour neighbourhood size
	 * @param value
	 *            dynamic parameter value
	 * 
	 * @return new strategy
	 */
	protected boolean getNewStrategy(boolean myStrategy, boolean otherStrategy, double myPayoff, double otherPayoff,
			MersenneTwisterFast random, int myNeighSize, int otherNeighSize, double value) {

		if (otherPayoff > myPayoff) {
			double probability = (otherPayoff - myPayoff) / (value * Math.max(otherNeighSize, myNeighSize));
			if (random.nextDouble(true, true) < probability) {
				return otherStrategy;
			}
		}
		return myStrategy;
	}

}
