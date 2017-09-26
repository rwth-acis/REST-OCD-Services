package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This is the super class of all graph properties
 *
 */
public abstract class AbstractProperty {

	public abstract double calculate(CustomGraph graph);
	
	public double calculate(Community community) {
		
		if(community == null) 
			throw new IllegalArgumentException("no community");
		
		return calculate(community.getCover().getGraph().getSubGraph(community.getMemberIndices()));		
	}
	
}
