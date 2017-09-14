package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.algo.GraphConnectivity;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;

public class ClusteringCoefficient extends CustomGraphProperty {

	@Override
	public double calculate(CustomGraph graph) {
		
		if (graph == null)
			throw new IllegalArgumentException();

		double[] localClusterings = new double[graph.nodeCount()];
		int nodeId = 0;
		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
			Node node = nc.node();
			NodeList nodeNeighbours = GraphConnectivity.getNeighbors(graph, new NodeList(node), 1);
			int links = 0;
			for (NodeCursor outerNodeCursor = nodeNeighbours.nodes(); outerNodeCursor.ok(); outerNodeCursor.next()) {
				Node neighbour = outerNodeCursor.node();
				for (EdgeCursor ec = neighbour.outEdges(); ec.ok(); ec.next()) {
					Edge edge = ec.edge();
					if (nodeNeighbours.contains(edge.target()))
						links++;
				}
			}

			localClusterings[nodeId] = calculateLocalClusteringCoefficient(graph.isDirected(), links, node.degree());
		}

		double max = 0;
		double length = localClusterings.length;
		for (int i = 0; i < length; i++) {
			max += localClusterings[i];
		}

		return (max / length);
	}

	protected double calculateLocalClusteringCoefficient(boolean directed, int links, int degree) {

		if (degree <= 1)
			return 0;

		if (directed)
			return (double) links / (degree * (degree - 1));

		return (double) 2 * links / (degree * (degree - 1));

	}

}
