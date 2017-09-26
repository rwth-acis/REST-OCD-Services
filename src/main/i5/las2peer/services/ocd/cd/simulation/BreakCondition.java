package i5.las2peer.services.ocd.cd.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Break Condition
 *
 */
public class BreakCondition implements Steppable {

	private static final long serialVersionUID = 1L;

	/**
	 * the maximum rounds of one simulation
	 */
	private int maxIterations = 9000;

	/**
	 * the minimum rounds of one simulation
	 */
	private int minIterations = 1000;

	/**
	 * number of generations to thesis.execute the stationary state
	 */
	private int window = 200;

	/**
	 * threshold
	 */
	private double threshold = 0.1;

	public BreakCondition() {

	}

	public BreakCondition(Simulation simulation) {
		setThreshold(1 / (Math.sqrt(simulation.getAgents().size())));
	}

	/**
	 * checks if the break condition is fulfilled
	 * 
	 * @param simulation
	 * @return break condition fulfilled
	 */
	public boolean isFullfilled(Simulation simulation) {

		int round = simulation.getRound();
		
		if (round >= getMaxIterations() - 1)
			return true;
		
		if (round < getMinIterations() - 1)
			return false;

		DataRecorder data = simulation.getDataRecorder();
		if ((round - getMinIterations() + 1) % window == 0)
			return (data.isSteady(window, threshold));

		return false;
	}

	/**
	 * Called every time this object is stepped. Checks if the break condition
	 * is fulfilled and stops the schedule if necessary.
	 * 
	 * @param state
	 *            Simulation
	 */
	@Override
	public void step(SimState state) {
		Simulation simulation = (Simulation) state;

		if (this.isFullfilled(simulation)) {
			simulation.stopSchedule();
		}
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public int getMinIterations() {
		return minIterations;
	}

	public void setMinIterations(int minIterations) {
		this.minIterations = minIterations;
	}

	public int getWindow() {
		return window;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
