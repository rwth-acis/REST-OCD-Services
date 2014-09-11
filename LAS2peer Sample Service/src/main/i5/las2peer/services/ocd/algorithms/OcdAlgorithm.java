package i5.las2peer.services.ocd.algorithms;


import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.Map;
import java.util.Set;

/**
 * The common interface for all Overlapping Community Detection Algorithms.
 * @author Sebastian
 *
 */
public interface OcdAlgorithm {
	
	/**
	 * Executes the algorithm on a connected graph.
	 * @param graph An at least weakly connected graph whose community structure will be detected.
	 * @return A cover for the input graph containing the community structure.
	 * @throws OcdAlgorithmException
	 */	
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException;
	
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	public AlgorithmType getAlgorithmType();

	/**
	 * Returns all graph types the algorithm is compatible with.
	 * @return The compatible graph types.
	 * An empty set if the algorithm is not compatible with any type.
	 */
	public Set<GraphType> compatibleGraphTypes();
	
	/**
	 * Returns the concrete parameters of the algorithm instance
	 * (including the default value for each parameter that was not explicitly set).
	 * @return A mapping from the name of each parameter to the actual parameter value in string format.
	 * An empty map if the algorithm does not take any parameters.
	 */
	public Map<String, String> getParameters();
	
	/**
	 * Sets the concrete parameters for the algorithm execution.
	 * @param parameters A mapping from parameter names to the actual parameter values.
	 * The mapping is not necessarily complete, i.e. some parameters might be missing
	 * and should be assigned their default value.
	 */
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException;
	
}
