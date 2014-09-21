package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;

public interface KnowledgeDrivenMeasure extends OcdMetric {

	/**
	 * Measures a cover with respect to another ground truth cover and adds 
	 * the resulting metric log to the cover.
	 * Implementations of this method must allow to be interrupted.
	 * I.e. they must periodically check the thread for interrupts
	 * and throw an InterruptedException if an interrupt was detected.
	 * @param cover The cover which is evaluated. It must also contain the corresponding graph.
	 * @param groundTruth The ground truth cover on whose bases the metric value is calculated.
	 * It must contain the same graph as the output cover.
	 * @return The calculated metric value.
	 * @throws OcdMetricException If the execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public double measure(Cover cover, Cover groundTruth) throws OcdMetricException, InterruptedException;
	
}
