package i5.las2peer.services.ocd.cooperation.simulation.termination;

import i5.las2peer.services.ocd.cooperation.simulation.Simulation;

/**
 * Break Condition
 *
 */
public class FixedIterationsCondition extends Condition {

	private static final long serialVersionUID = 1L;

	/**
	 * the number of rounds of one simulation
	 */
	private int iterations = 200;


	/**
	 * Instantiates a new FixedIterationsCondition
	 */
	public FixedIterationsCondition() {

	}

	/**
	 * Instantiates a new FixedIterationsCondition. Sets the number of iterations.
	 */
	public FixedIterationsCondition(int iterations) {
		this.iterations = iterations;

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
		
		if (round >= getIterations() - 1)
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see i5.las2peer.services.ocd.cooperation.simulation.termination.Condition#
	 * setParameters(int[])
	 */
	@Override
	public void setParameters(int[] parameters) {

		super.setParameters(parameters);

		if (parameters.length < 1)
			throw new IllegalArgumentException("no parameter");

		if (parameters[0] < 0)
			throw new IllegalArgumentException("negative iterations");

		if (parameters[0] > 100000)
			throw new IllegalArgumentException("too many iterations");

		setiterations(parameters[0]);
	}

	/**
	 * @return the number of iterations
	 */
	public int getIterations() {
		return iterations;
	}

	public void setiterations(int iterations) {
		this.iterations = iterations;
	}

}
