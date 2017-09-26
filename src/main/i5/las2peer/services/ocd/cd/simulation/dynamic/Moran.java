package i5.las2peer.services.ocd.cd.simulation.dynamic;

import ec.util.MersenneTwisterFast;
import i5.las2peer.services.ocd.cd.simulation.Agent;
import i5.las2peer.services.ocd.cd.simulation.Simulation;
import sim.util.Bag;

/**
 *
 * Moran-Like
 * 
 */
public class Moran extends Dynamic {
	
	private static final long serialVersionUID = 1L;
	
	/////////////// Constructor ////////////

	protected Moran() {
		super();
	}

	/////////////// Methods /////////////////

	@Override
	/// Dependencies
	public boolean getNewStrategy(Agent agent, Simulation simulation) {

		int round = simulation.getRound()-1;
		MersenneTwisterFast random = simulation.random;
		
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
		
		return getNewStrategy(size, strategies, payoff, random);
	}
	
	/// Algorithm
	protected boolean getNewStrategy(int size, boolean[] strategies, double[] payoff, MersenneTwisterFast random) {

		double totalPayoff = 0.0;
		for (int i = 0; i < size; i++) {			
			totalPayoff += payoff[i];
		}

		double[] probability = new double[size];
		for (int i = 0; i < size; i++) {
			probability[i] = payoff[i] / totalPayoff;
		}

		double randomDouble = random.nextDouble(true, true);
		for (int i = 0; i < size; i++) {
			double value = 0.0;
			for (int j = 0; j <= i; j++) {
				value += probability[j];
				if (randomDouble <= value) {					
					return strategies[i];
				}
			}
		}
		return strategies[0];
	}
}
