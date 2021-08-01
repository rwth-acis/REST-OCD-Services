package i5.las2peer.services.ocd.cooperation.simulation.termination;

import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Break Condition
 *
 */
public abstract class Condition implements Steppable {

	private static final long serialVersionUID = 1L;

	public Condition() {

	}

	/**
	 * checks if the break condition is fulfilled
	 * 
	 * @param simulation
	 * @return break condition fulfilled
	 */
	public abstract boolean isFullfilled(Simulation simulation);

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

	/**
	 * Returns a upper limit for iterations to avoid endless simulations.
	 * 
	 * @return the maximum number of possible iterations
	 */
	public int getMaxIterations() {
		return 10000;
	}

	/**
	 * Returns the number of generations to compute the cooperativity
	 * 
	 * @return timeWindow
	 */
	public int getWindow() {
		return 1;
	}

	/**
	 * Set parameters
	 * 
	 * @param parameters
	 */
	public void setParameters(int[] parameters) {

		if (parameters == null)
			throw new IllegalArgumentException("empty parameters");

	}

}
