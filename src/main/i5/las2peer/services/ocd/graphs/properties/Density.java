package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;

public class Density extends CustomGraphProperty {

	@Override
	public double calculate(CustomGraph graph) {
		
		if (graph == null)
			throw new IllegalArgumentException();
		
		return calculate(graph.nodeCount(), graph.edgeCount());
	}
	
	public double calculate(int nodes, int edges) {

		if (nodes < 2 || edges < 0)
			return 0.0;

		int num = edges;
		int den = nodes * (nodes - 1);

		return (num / (double) den);
	}


	
}
