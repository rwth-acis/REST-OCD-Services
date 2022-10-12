package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.algo.GraphConnectivity;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;

/**
 * This class handles the clustering coefficient computation of a CustomGraph.
 */
public class ClusteringCoefficient extends AbstractProperty {
	
	/**
	 * Returns the clustering coefficient of a CustomGraph
	 * 
	 * @param graph the CustomGraph
	 * @return the clustering coefficient
	 */
	@Override
	public double calculate(CustomGraph graph) {
		
		if (graph == null)
			throw new IllegalArgumentException();
		if(graph.nodeCount() < 1) {return 0;}	//TODO ok?
		double[] localClusterings = new double[graph.nodeCount()];
		int nodeId = 0;
		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
			Node node = nc.node();
			localClusterings[nodeId] = calculateLocal(node, graph);
			nodeId++;
		}

		double max = 0;
		double length = localClusterings.length;
		for (int i = 0; i < length; i++) {
			max += localClusterings[i];
		}
		
		return (max / length);
	}
	
	
	/**
	 *  * Returns the local clustering coefficient of a node
	 *  	 
	 * @param node the node
	 * @param graph the containing graph
	 * @return the local clustering coefficient 
	 */
	protected double calculateLocal(Node node, CustomGraph graph) {
		
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
		
		int degree = node.degree() / 2;
		if(graph.isDirected())
			return localDirected(links, degree);
		
		return localUndirected(links / 2, degree);
	}
	
	/**
	 * Returns the local clustering coefficient of a node in a undirected graph
	 * 
	 * @param links the number of connected neighbours 
	 * @param degree the degree of a node
	 * @return the coefficient
	 */
	protected double localUndirected(int links, int degree) {

		if (degree <= 1)
			return 0;		

		return links / (0.5 * degree * (degree - 1));

	}
	
	/**
	 * Returns the local clustering coefficient of a node in a directed graph
	 * 
	 * @param links the number of connected neighbours 
	 * @param degree the degree of a node
	 * @return the coefficient
	 */
	protected double localDirected(int links, int degree) {

		if (degree <= 1)
			return 0;		

		return (double) links / (degree * (degree - 1));

	}
	

}
