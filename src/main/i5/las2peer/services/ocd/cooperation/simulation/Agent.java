
package i5.las2peer.services.ocd.cooperation.simulation;

import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

/**
 * Every Agent is a node of the network of the simulation.
 *
 */
public class Agent implements Steppable {

	private static final long serialVersionUID = 1;

	/**
	 * the nodeId is used to identify the agent for further evaluation after the simulation.
	 */
	private int nodeId;

	/**
	 * The neighborhood of a agent is stored for faster computation. This is
	 * possible because the network do not change during the simulation.
	 */
	private Bag neighbours;

	/**
	 * This list records the strategy of the agent in every round. A {@link Dynamic} may use this
	 * information to determine the new strategy. Further This list can
	 * be used for further evaluation after the simulation.
	 */
	private List<Boolean> strategies;
	
	/**
	 * This list records the payoff of the agent in every round. A {@link Dynamic} may use this
	 * information to determine the new strategy. Further This list can
	 * be used for further evaluation after the simulation.
	 */
	private List<Double> payoff;

	private int currentPayoffId;
	private int currentStrategyId;

	public Agent(int nodeId) {

		this.nodeId = nodeId;
		strategies = new ArrayList<Boolean>();
		strategies.add(false);
		payoff = new ArrayList<Double>();
		payoff.add(0.0);
	}

	public Agent() {
		this(0);
	}

	/**
	 * Initialize this agent for a new simulation. Should be called before every
	 * simulation.
	 * 
	 * @param strategy
	 *            the initial strategy
	 * @param network
	 *            the network of this simulation
	 */
	public void initialize(boolean strategy, Network network) {

		strategies.clear();
		currentStrategyId = 0;
		strategies.add(0, strategy);

		payoff.clear();
		currentPayoffId = 0;
		payoff.add(0, 0.0);

		if (this.neighbours == null)
			this.neighbours = calculateNeighbourhood(network);
	}

	/////////////////// Step ///////////////////////////

	/**
	 * Update the agents payoff. Called every round.
	 */
	@Override
	public void step(SimState state) {
		Simulation simulation = (Simulation) state;
		payoff.add(simulation.getGame().getPayoff(this));
		currentPayoffId++;
	}

	/**
	 * Update the agents strategy. Called every round.
	 */
	public void updateDynamicStep(SimState state) {
		Simulation simulation = (Simulation) state;
		strategies.add(simulation.getDynamic().getNewStrategy(this, simulation));
		currentStrategyId++;
	}

	////////////////// Network Utility ///////////////////////////

	/**
	 * Get a random neighbour agent. Uses the given random generator. A dynamic may use this
	 * method to determine the new strategy.
	 * 
	 * @param random a random generator
	 * @return a random neighbour agent
	 */
	public Agent getRandomNeighbour(MersenneTwisterFast random) {

		Bag agents = getNeighbourhood();
		if (agents.size() > 0) {
			return (Agent) agents.get(random.nextInt(agents.size()));
		}
		return null;
	}

	/**
	 * Calculate the neighbourhood of this agent by the given network.
	 * 
	 * @return agent
	 */
	protected Bag calculateNeighbourhood(Network network) {

		Bag edges = new Bag(network.getEdgesIn(this));
		Bag neighbours = new Bag();
		for (int i = 0, si = edges.size(); i < si; i++) {
			Edge edge = (Edge) edges.get(i);
			Agent neighbour = (Agent) edge.getOtherNode(this);
			neighbours.add(neighbour);
		}
		return neighbours;
	}

	
	/**
	 * Returns the Neighbourhood of this agent. A {@link Game} use this method to determine the agents new payoff. A {@link Dynamic} may use this
	 * method to determine the agents new strategy.
	 * 
	 * @return the neighbours of this agent
	 */
	public Bag getNeighbourhood() {

		return this.neighbours;
	}

	//////////////////// Getter ///////////////

	public long getNodeId() {
		return nodeId;
	}

	/**
	 * The agent is steady if it's strategy did not change between the last
	 * rounds. This is used by the {@link BreakCondition}.
	 * 
	 * @return true if agent is steady
	 */
	public boolean isSteady() {
		return getStrategy(currentStrategyId) == getStrategy(currentStrategyId - 1);
	}

	/**
	 * @return current strategy of this agent
	 */
	public boolean getStrategy() {
		return getStrategy(currentStrategyId);
	}

	/**
	 * @param round
	 *            of simulation
	 * @return strategy of this agent in round
	 */
	public boolean getStrategy(int round) {
		return strategies.get(round);
	}

	/**
	 * @return current payoff of this agent
	 */
	public double getPayoff() {
		return getPayoff(currentPayoffId);
	}

	/**
	 * @param round
	 *            of simulation
	 * @return payoff of this agent in the round
	 */
	public double getPayoff(int round) {
		return payoff.get(round);
	}

	/**
	 * @return list of every rounds strategy
	 */
	protected List<Boolean> getStrategiesList() {
		return strategies;
	}

	/**
	 * @return list of every rounds payoff
	 */
	protected List<Double> getPayoffList() {
		return this.payoff;
	}

}
