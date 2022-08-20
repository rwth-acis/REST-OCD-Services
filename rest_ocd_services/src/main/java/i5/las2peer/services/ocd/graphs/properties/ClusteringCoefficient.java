package i5.las2peer.services.ocd.graphs.properties;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.algo.GraphConnectivity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.*;

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
	public double calculate(CustomGraph graph) throws InterruptedException {
		
		if (graph == null)
			throw new IllegalArgumentException();

		double[] localClusterings = new double[graph.getNodeCount()];
		int nodeId = 0;
		Iterator<Node> nodeIterator = graph.iterator();
		while(nodeIterator.hasNext()) {
			Node node = nodeIterator.next();
			localClusterings[nodeId] = calculateLocal(node, graph);
			nodeId++;
		}

		double max = 0;
		double length = localClusterings.length;
		for (double localClustering : localClusterings) {
			max += localClustering;
		}

		return (max / length);
	}
	
	
	/**
	 *  * Returns the local clustering coefficient of a node
	 *  	 
	 * @param node the node
	 * @param graph the containing graph
	 * @return the local clustering coefficient
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	protected double calculateLocal(Node node, CustomGraph graph) throws InterruptedException {
		//TODO: Check if neighbor and out edge iteration behaves similarly to yFiles here
		//GraphConnectivity.getNeighbors(graph, new NodeList(node), 1);
		Set<Node> nodeNeighbours = graph.getNeighbours(node);
		int links = 0;
		for(Node neighbour : nodeNeighbours) {
			Iterator<Edge> neighborOutEdgeIt = neighbour.leavingEdges().iterator();
			while (neighborOutEdgeIt.hasNext()) {
				Edge edge = neighborOutEdgeIt.next();
				if (nodeNeighbours.contains(edge.getTargetNode()))
					links++;
			}
		}
		
		int degree = node.getDegree() / 2;
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
