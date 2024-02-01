package i5.las2peer.services.ocd.algorithms;


import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.Parameterizable;

import java.util.Set;

/**
 * The common interface for all Overlapping Community Detection Algorithms.
 * Any classes implementing this interface must provide a default (no-argument) constructor.
 * @author Sebastian
 *
 */
public interface OcdMultiplexAlgorithm extends Parameterizable, CoverCreationMethod {

	/**
	 * @throws OcdMetricException 
	 * Executes the algorithm on a connected graph.
	 * Implementations of this method should allow to be interrupted.
	 * I.e. they should periodically check the thread for interrupts
	 * and throw an InterruptedException if an interrupt was detected.
	 * @param graph An at least weakly connected graph whose community structure will be detected.
	 * @return A cover for the input graph containing the community structure.
	 * @throws OcdAlgorithmException If the execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 * @throws OcdMetricException If the metric execution failed.
	 */
	public Cover detectOverlappingCommunities(MultiplexGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException;
	
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	public CoverCreationType getAlgorithmType();

	/**
	 * Returns all graph types the algorithm is compatible with.
	 * @return The compatible graph types.
	 * An empty set if the algorithm is not compatible with any type.
	 */
	public Set<GraphType> compatibleGraphTypes();
	
}
