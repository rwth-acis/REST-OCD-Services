package i5.las2peer.services.ocd.viewer.layouters;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * The common interface for all graph layouters.
 * Do the positioning and routing of nodes and edges.
 * @author Sebastian
 *
 */
public interface GraphLayouter {
	
	/**
	 * Applies the layout to the given graph;
	 * @param graph The graph.
	 */
	public abstract void doLayout(CustomGraph graph);
	
}
