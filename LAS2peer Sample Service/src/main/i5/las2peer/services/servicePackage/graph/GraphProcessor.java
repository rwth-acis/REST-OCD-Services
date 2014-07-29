package i5.las2peer.services.servicePackage.graph;

import i5.las2peer.services.servicePackage.algorithms.AlgorithmLog;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.algo.GraphConnectivity;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;

/**
 * Pre-processes graphs to facilitate community detection.
 * @author Sebastian
 *
 */
public class GraphProcessor {

	/**
	 * Transforms a graph into an undirected Graph.
	 * For each edge a reverse edge leading the opposite way is added, if missing.
	 * The reverse edge is assigned the same weight as the original one. If edges in both
	 * ways do already exist, they both are assigned their average weight.
	 * @param graph - The graph to be transformed.
	 */
	public void makeUndirected(CustomGraph graph) {
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			double edgeWeight = graph.getEdgeWeight(edge);
			Edge reverseEdge;
			Node target = edge.target();
			Node source = edge.source();
			if(graph.containsEdge(target, source)) {
				reverseEdge = target.getEdgeTo(source);
				edgeWeight += graph.getEdgeWeight(reverseEdge);
				edgeWeight /= 2;
				graph.setEdgeWeight(edge, edgeWeight);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
			else {
				reverseEdge = graph.createEdge(target, source);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
			edges.next();
		}
	}
	
	/**
	 * Returns all connected components of a graph.
	 * @param graph - The graph whose connected components are identified.
	 * @return A map containing the connected components and a corresponding mapping
	 * from the new component nodes to the original graph nodes.
	 */
	public Map<CustomGraph, Map<Node, Node>> divideIntoConnectedComponents(CustomGraph graph) {
		/*
		 * Iterates over all connected components of the graph creating a copy for each of them.
		 */
		NodeList[] componentsArray = GraphConnectivity.connectedComponents(graph);
		/* TODO
		 * Change variable type. componentsMap must allow duplicate keys.
		 */
		Map<CustomGraph, Map<Node, Node>> componentsMap = new HashMap<CustomGraph, Map<Node, Node>>();
		for(int i=0; i<componentsArray.length; i++) {
			CustomGraph component = new CustomGraph();
			Map<Node, Node> nodeMap = new HashMap<Node, Node>();
			Map<Node, Node> tmpNodeMap = new HashMap<Node, Node>();
			/*
			 * Sets component nodes
			 */
			NodeCursor nodes = componentsArray[i].nodes();
			while(nodes.ok()) {
				Node originalNode = nodes.node();
				Node newNode = component.createNode();
				component.setNodeName(newNode, graph.getNodeName(originalNode));
				nodeMap.put(newNode, originalNode);
				tmpNodeMap.put(originalNode, newNode);
				nodes.next();
			}
			/*
			 * Sets component edges
			 */
			nodes.toFirst();
			while(nodes.ok()) {
				Node node = nodes.node();
				EdgeCursor outEdges = node.outEdges();
				while(outEdges.ok()) {
					Edge outEdge = outEdges.edge();
					Node target = outEdge.target();
					Edge newEdge = component.createEdge(tmpNodeMap.get(node), tmpNodeMap.get(target));
					double edgeWeight = graph.getEdgeWeight(outEdge);
					component.setEdgeWeight(newEdge, edgeWeight);
					outEdges.next();
				}
				nodes.next();
			}
			componentsMap.put(component, nodeMap);
		}
		return componentsMap;
	}
	
	/**
	 * Merges the covers of the separated connected components of a graph to one single cover.
	 * @param graph The graph containing the connected components.
	 * @param componentCovers A mapping from covers of all the connected components of a graph to a corresponding node mapping,
	 * that maps the nodes from the connected component to the original graph nodes.
	 * @return The single cover of the original graph.
	 */
	public Cover mergeComponentCovers(CustomGraph graph, Map<Cover, Map<Node, Node>> componentCovers) {
		int totalCommunityCount = 0;
		for(Cover cover : componentCovers.keySet()) {
			totalCommunityCount += cover.communityCount();
		}
		Matrix memberships = new CCSMatrix(graph.nodeCount(), totalCommunityCount);
		Cover currentCover = null;
		AlgorithmLog algo = new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>());
		Iterator<Cover> it = componentCovers.keySet().iterator();
		if(it.hasNext()) {
			algo = it.next().getAlgorithm();
		}
		NodeCursor currentNodes;
		Node node;
		int currentCoverFirstCommunityIndex = 0;
		double belongingFactor;
		for(Map.Entry<Cover, Map<Node, Node>> entry : componentCovers.entrySet()) {
			currentCover = entry.getKey();
			currentNodes = currentCover.getGraph().nodes();
			while(currentNodes.ok()) {
				node = currentNodes.node();
				for(int i=0; i<currentCover.communityCount(); i++) {
					belongingFactor = currentCover.getBelongingFactor(node, i);
					memberships.set(entry.getValue().get(node).index(), currentCoverFirstCommunityIndex + i, belongingFactor);
				}
				currentNodes.next();
			}
			currentCoverFirstCommunityIndex += currentCover.communityCount();
			if(!currentCover.getAlgorithm().equals(algo)) {
				algo = new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>());
			}
		}
		return new Cover(graph, memberships, currentCover.getAlgorithm());
	}

}
