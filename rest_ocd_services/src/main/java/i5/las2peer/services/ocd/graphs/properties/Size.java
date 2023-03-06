package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This class handles the size of a CustomGraph.
 */
public class Size extends AbstractProperty {

	/**
	 * Returns the size of a CustomGraph
	 * 
	 * @param graph the CustomGraph
	 * @return the size
	 */
	@Override
	public double calculate(CustomGraph graph) {

		if (graph == null)
			throw new IllegalArgumentException();
		
		return graph.getNodeCount();
	}
}
