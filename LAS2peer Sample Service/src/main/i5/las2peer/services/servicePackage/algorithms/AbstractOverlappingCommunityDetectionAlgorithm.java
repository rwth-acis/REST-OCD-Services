package i5.las2peer.services.servicePackage.algorithms;


import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;

import java.util.Set;

public interface AbstractOverlappingCommunityDetectionAlgorithm {
	
	/**
	 * Returns the compatible graph types for the algorithm.
	 * 
	 */	
	public Set<GraphType> getCompatibleGraphTypes();
	/**
	 * Executes the algorithm.
	 * 
	 * @param graph The graph whose community structure will be detected.
	 * @return A cover containing the community structure.
	 */	
	public Cover detectOverlappingCommunities(CustomGraph graph);

}
