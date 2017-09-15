package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This class handles the average degree computation of a CustomGraph.
 */
public class AverageDegree extends GraphPropertyAbstract {

	/**
	 * Returns the average degree of a CustomGraph
	 * 
	 * @param graph the CustomGraph
	 * @return the average degree
	 */
	@Override
	public double calculate(CustomGraph graph) {

		if (graph == null)
			throw new IllegalArgumentException();

		double degree = calculate(graph.nodeCount(), graph.edgeCount());
		if (!graph.isDirected())
			return degree / 2;

		return degree;
	}
	
	/**
	 * @param nodes number of nodes
	 * @param edges number of edges
	 * @return the average degree
	 */
	public double calculate(int nodes, int edges) {

		if (nodes < 1 || edges < 0)
			return 0.0;

		return ((2.0 * edges) / (nodes));
	}

}
