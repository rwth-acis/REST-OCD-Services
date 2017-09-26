package i5.las2peer.services.ocd.cd.simulation.dynamic;

import i5.las2peer.services.ocd.cd.simulation.Agent;
import i5.las2peer.services.ocd.cd.simulation.Simulation;

public class WinStayLoseShift extends Dynamic {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean getNewStrategy(Agent agent, Simulation simulation) {

		if (simulation.getRound() < 2)
			return agent.getStrategy();

		int round = simulation.getRound() - 1;
		boolean myStrategy = agent.getStrategy(round);

		double myPayoff = agent.getPayoff(round);
		double myLastPayoff = agent.getPayoff(round - 1);

		return getNewStrategy(myStrategy, myPayoff, myLastPayoff);
	}

	protected boolean getNewStrategy(boolean myStrategy, double myPayoff, double myLastPayoff) {

		if (myLastPayoff >= myPayoff) {
			if (myStrategy == true) {
				return false;
			}
			return true;
		}
		return myStrategy;
	}
}
