package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This class handles the density computation of a CustomGraph.
 */
public class Density extends GraphPropertyAbstract {
	
	/**
	 * Returns the density of a CustomGraph
	 * 
	 * @param graph the CustomGraph
	 * @return the density
	 */
	@Override
	public double calculate(CustomGraph graph) {
		
		if (graph == null)
			throw new IllegalArgumentException();
		
		return calculate(graph.nodeCount(), graph.edgeCount());
	}
	
	/**
	 * @param nodes number of nodes
	 * @param edges number of edges
	 * @return density
	 */
	public double calculate(int nodes, int edges) {

		if (nodes < 2 || edges < 0)
			return 0.0;

		int num = edges;
		int den = nodes * (nodes - 1);

		return (num / (double) den);
	}


	
}
