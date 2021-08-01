package i5.las2peer.services.ocd.cooperation.simulation.dynamic;

import i5.las2peer.services.ocd.cooperation.simulation.Agent;
import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import sim.util.Bag;

/**
 *
 * Unconditional Imitation
 * 
 * Search Neighbor with best Payoff and adopt his strategy
 * 
 */
public class UnconditionalImitation extends Dynamic {

	private static final long serialVersionUID = 1L;

	/////////////// Constructor ////////////

	protected UnconditionalImitation() {
		super();
	}

	/////////////// Methods ///////////////

	@Override
	/// Dependencies
	public boolean getNewStrategy(Agent agent, Simulation simulation) {

		int round = simulation.getRound() - 1;
		Bag neighbours = agent.getNeighbourhood();
		int size = neighbours.size() + 1;

		boolean[] strategies = new boolean[size];
		double[] payoff = new double[size];
		strategies[size - 1] = agent.getStrategy(round);
		payoff[size - 1] = agent.getPayoff(round);
		
		for (int neighbourId = 0; neighbourId < size - 1; neighbourId++) {
			Agent neighbour = (Agent) neighbours.get(neighbourId);
			strategies[neighbourId] = neighbour.getStrategy(round);
			payoff[neighbourId] = neighbour.getPayoff(round);
		}

		return getNewStrategy(size, strategies, payoff);
	}

	/// Algorithm
	/**
	 * determine the best strategy. parameters are not checked.
	 * 
	 * @param size length of the parameter arrays
	 * @param strategies array of strategies
	 * @param payoff array of payoff values
	 * @return new strategy
	 */
	protected boolean getNewStrategy(int size, boolean[] strategies, double[] payoff) {

		double bestPayoff = payoff[0];
		boolean bestStrategy = strategies[0];
		for (int i = 1; i < size; i++) {
			if (payoff[i] > bestPayoff) {
				bestPayoff = payoff[i];
				bestStrategy = strategies[i];
			}
		}
		return bestStrategy;
	}
	
}
