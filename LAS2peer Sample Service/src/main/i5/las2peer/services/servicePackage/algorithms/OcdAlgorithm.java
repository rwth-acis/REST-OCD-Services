package i5.las2peer.services.servicePackage.algorithms;


import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import java.util.Set;

/**
 * Represents the common interface for all Overlapping Community Detection Algorithms.
 * @author Sebastian
 *
 */
public interface OcdAlgorithm {
	
	/**
	 * Returns the compatible graph types for the algorithm.
	 * 
	 */	
	public Set<GraphType> compatibleGraphTypes();
	
	/**
	 * Executes the algorithm.
	 * 
	 * @param graph The graph whose community structure will be detected.
	 * @return A normalized cover containing the community structure.
	 */	
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException;
	
	/**
	 * Returns an instance of the the enum Algorithm that corresponds to the 
	 * concrete algorithm.
	 * @return The enum instance.
	 */
	public Algorithm getAlgorithm();

}
