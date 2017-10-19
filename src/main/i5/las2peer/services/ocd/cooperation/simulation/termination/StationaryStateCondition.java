package i5.las2peer.services.ocd.cooperation.simulation.termination;

import i5.las2peer.services.ocd.cooperation.simulation.DataRecorder;
import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import sim.engine.SimState;

/**
 * Break Condition
 *
 */
public class StationaryStateCondition extends Condition {

	private static final long serialVersionUID = 1L;

	/**
	 * the maximum rounds of one simulation
	 */
	private int maxIterations = 1000;

	/**
	 * the minimum rounds of one simulation
	 */
	private int minIterations = 100;

	/**
	 * number of generations to thesis.execute the stationary state
	 */
	private int window = 100;

	/**
	 * threshold
	 */
	private double threshold = 0.1;

	public StationaryStateCondition() {

	}

	/**
	 * checks if the break condition is fulfilled
	 * 
	 * @param simulation
	 * @return break condition fulfilled
	 */
	@Override
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

	@Override
	public void setParameters(int[] parameters) {
		super.setParameters(parameters);

		if (parameters.length < 3)
			throw new IllegalArgumentException("not enough parameters");

		if (parameters[0] < 0 || parameters[1] < 0 || parameters[2] < 0)
			throw new IllegalArgumentException("negative iterations");

		if (parameters[0] > 100000)
			throw new IllegalArgumentException("too many iterations");

		if (parameters[0] > 0)		
			setMaxIterations(parameters[0]);
		
		if (parameters[1] > 0)		
			setMinIterations(parameters[1]);
		
		if (parameters[2] > 0)		
			setWindow(parameters[2]);
		
		if (getMaxIterations() < getMinIterations())
			setMaxIterations(getMinIterations());

		if (getWindow() > getMaxIterations())
			throw new IllegalArgumentException("time window oversized ");

	}

	@Override
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

	@Override
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
