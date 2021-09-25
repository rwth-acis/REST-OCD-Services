package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.Set;

/**
 * A common interface for statistical overlapping community detection measures.
 * @author Sebastian
 *
 */
public interface StatisticalMeasure extends OcdMetric {

	/**
	 * Measures a cover adds the resulting metric log to it.
	 * Implementations of this method must allow to be interrupted.
	 * I.e. they must periodically check the thread for interrupts
	 * and throw an InterruptedException if an interrupt was detected.
	 * @param cover The cover which is evaluated. It must also contain the corresponding graph.
	 * @return The calculated metric value.
	 * @throws OcdMetricException If the metric execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 * @throws OcdAlgorithmException If the execution failed.
	 */
	public double measure(Cover cover) throws OcdMetricException, InterruptedException, OcdAlgorithmException;
	
	/**
	 * Returns the graph types which are compatible for a metric.
	 * @return The compatible graph types.
	 */
	 public Set<GraphType> compatibleGraphTypes();
	
}
