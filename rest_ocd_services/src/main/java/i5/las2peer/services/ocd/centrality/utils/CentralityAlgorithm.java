package i5.las2peer.services.ocd.centrality.utils;

import java.util.Set;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationMethod;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Parameterizable;

public interface CentralityAlgorithm extends Parameterizable, CentralityCreationMethod {
	
	/**
	 * Calculates the centrality values for all the nodes in the graph
	 * @param graph The graph on which the centrality values are calculated
	 * @return A map containing the centrality values
	 * @throws InterruptedException 
	 * @throws CentralityAlgorithmException 
	 */
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException, CentralityAlgorithmException;
	
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	public CentralityMeasureType getCentralityMeasureType();
	
	/**
	 * Returns all graph types the algorithm is compatible with.
	 * @return The compatible graph types.
	 * An empty set if the algorithm is not compatible with any type.
	 */
	public Set<GraphType> compatibleGraphTypes();
}
