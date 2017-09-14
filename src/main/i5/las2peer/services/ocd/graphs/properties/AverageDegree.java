package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;

public class AverageDegree extends CustomGraphProperty {

	@Override
	public double calculate(CustomGraph graph) {
		
		if (graph == null)
			throw new IllegalArgumentException();

		return calculate(graph.nodeCount(), graph.edgeCount());
	}

	public double calculate(int nodes, int edges) {

		if (nodes < 1 || edges < 0)
			return 0.0;

		return ((2.0 * edges) / (nodes));
	}
}
