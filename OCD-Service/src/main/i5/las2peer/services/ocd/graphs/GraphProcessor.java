package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.algorithms.CoverCreationLog;
import i5.las2peer.services.ocd.algorithms.CoverCreationType;
import i5.las2peer.services.ocd.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * Sets the graph types of a given graph.
	 * @param graph A graph without multi edges.
	 */
	public void determineGraphTypes(CustomGraph graph) {
		graph.clearTypes();
		EdgeCursor edges = graph.edges();
		Edge edge;
		Edge reverseEdge;
		while(edges.ok()) {
			edge = edges.edge();
			double edgeWeight = graph.getEdgeWeight(edge);
			if(edgeWeight != 1) {
				graph.addType(GraphType.WEIGHTED);
			}
			if(edgeWeight == 0) {
				graph.addType(GraphType.ZERO_WEIGHTS);
			}
			if(edgeWeight < 0) {
				graph.addType(GraphType.NEGATIVE_WEIGHTS);
			}
			if(edge.source().equals(edge.target())) {
				graph.addType(GraphType.SELF_LOOPS);
			}
			reverseEdge = edge.target().getEdgeTo(edge.source());
			if(reverseEdge == null || graph.getEdgeWeight(reverseEdge) != edgeWeight) {
				graph.addType(GraphType.DIRECTED);
			}
			edges.next();
		}
	}
	
	/**
	 * Transforms a graph into an undirected Graph.
	 * For each edge a reverse edge leading the opposite way is added, if missing.
	 * The reverse edge is assigned the same weight as the original one. If edges in both
	 * ways do already exist, they are assigned the sum of both weights.
	 * @param graph The graph to be transformed.
	 */
	protected void makeUndirected(CustomGraph graph) {
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			double edgeWeight = graph.getEdgeWeight(edge);
			Edge reverseEdge;
			Node target = edge.target();
			Node source = edge.source();
			reverseEdge = target.getEdgeTo(source);
			if(reverseEdge != null && reverseEdge.index() > edge.index() && ! target.equals(source)) {
				edgeWeight += graph.getEdgeWeight(reverseEdge);
				graph.setEdgeWeight(edge, edgeWeight);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
			else if (reverseEdge == null){
				reverseEdge = graph.createEdge(target, source);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
			edges.next();
		}
		graph.removeType(GraphType.DIRECTED);
	}
	
	/**
	 * Removes multi edges from a graph. Each set of parallel edges is replaced
	 * by an edge whose weight is the sum of the original edge weights. Other edge
	 * attributes correspond to a random original edge.
	 * @param graph The graph to be transformed.
	 */
	protected void removeMultiEdges(CustomGraph graph) {
		EdgeCursor edges = graph.edges();
		Map<Pair<Integer, Integer>, Double> nodePairWeights = new HashMap<Pair<Integer, Integer>, Double>();
		while(edges.ok()) {
			Edge edge = edges.edge();
			Pair<Integer, Integer> nodePair = new Pair<Integer, Integer>(edge.source().index(), edge.target().index());
			Double edgeWeight = nodePairWeights.get(nodePair);
			if(edgeWeight == null) {
				nodePairWeights.put(nodePair, graph.getEdgeWeight(edge));
			}
			else {
				edgeWeight += graph.getEdgeWeight(edge);
				nodePairWeights.put(nodePair, edgeWeight);
				graph.removeEdge(edge);
			}
			edges.next();
		}
		edges.toFirst();
		while(edges.ok()) {
			Edge edge = edges.edge();
			double edgeWeight = nodePairWeights.get(new Pair<Integer, Integer>(edge.source().index(), edge.target().index()));
			graph.setEdgeWeight(edge, edgeWeight);
			edges.next();
		}
	}
		
	/**
	 * Redefines the edges of a graph according to certain criteria.
	 * @param graph The graph to be transformed
	 * @param noNegativeWeights If true edges with negative weight are removed from the graph.
	 * @param noZeroWeights If true edges with weight zero are removed from the graph.
	 * @param noSelfLoops If true self loops will be removed from the graph.
	 * @param setToOne If true the weight of remaining edges will be set to 1.
	 */
	protected void redefineEdges(CustomGraph graph, boolean noNegativeWeights, boolean noZeroWeights, boolean noSelfLoops, boolean setToOne) {
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			double edgeWeight = graph.getEdgeWeight(edge);
			if(noNegativeWeights && edgeWeight < 0) {
				graph.removeEdge(edge);
			}
			else if(noZeroWeights && edgeWeight == 0) {
				graph.removeEdge(edge);
			}
			else if(noSelfLoops && edge.source().equals(edge.target())) {
				graph.removeEdge(edge);
			}
			else if(setToOne) {
				graph.setEdgeWeight(edge, 1);
			}
			edges.next();
		}
		if(noSelfLoops) {
			graph.removeType(GraphType.SELF_LOOPS);
		}
		if(setToOne) {
			graph.removeType(GraphType.WEIGHTED);
		}
		if(noNegativeWeights) {
			graph.removeType(GraphType.NEGATIVE_WEIGHTS);
		}
		if(noZeroWeights) {
			graph.removeType(GraphType.ZERO_WEIGHTS);
		}
	}
	
	/**
	 * Returns all connected components of a graph.
	 * @param graph The graph whose connected components are identified.
	 * @return A map containing the connected components and a corresponding mapping
	 * from the new component nodes to the original graph nodes.
	 */
	public List<Pair<CustomGraph, Map<Node, Node>>> divideIntoConnectedComponents(CustomGraph graph) {
		/*
		 * Iterates over all connected components of the graph creating a copy for each of them.
		 */
		NodeList[] componentsArray = GraphConnectivity.connectedComponents(graph);
		List<Pair<CustomGraph, Map<Node, Node>>> componentsList = new ArrayList<Pair<CustomGraph, Map<Node, Node>>>();
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
			componentsList.add(new Pair<CustomGraph, Map<Node, Node>>(component, nodeMap));
		}
		return componentsList;
	}
	
	/**
	 * Merges the covers of the separated connected components of a graph to one single cover.
	 * @param graph The graph containing the connected components.
	 * @param componentCovers A mapping from covers of all the connected components of a graph to a corresponding node mapping,
	 * that maps the nodes from the connected component to the original graph nodes.
	 * @return The single cover of the original graph.
	 */
	public Cover mergeComponentCovers(CustomGraph graph, List<Pair<Cover, Map<Node, Node>>> componentCovers) {
		int totalCommunityCount = 0;
		for(Pair<Cover, Map<Node, Node>> componentCover : componentCovers) {
			totalCommunityCount += componentCover.getFirst().communityCount();
		}
		Matrix memberships = new CCSMatrix(graph.nodeCount(), totalCommunityCount);
		Cover currentCover = null;
		CoverCreationLog algo = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>());
		if(!componentCovers.isEmpty()) {
			algo = componentCovers.get(0).getFirst().getCreationMethod();
		}
		NodeCursor currentNodes;
		Node node;
		int currentCoverFirstCommunityIndex = 0;
		double belongingFactor;
		for(Pair<Cover, Map<Node, Node>> componentCover : componentCovers) {
			currentCover = componentCover.getFirst();
			currentNodes = currentCover.getGraph().nodes();
			while(currentNodes.ok()) {
				node = currentNodes.node();
				for(int i=0; i<currentCover.communityCount(); i++) {
					belongingFactor = currentCover.getBelongingFactor(node, i);
					memberships.set(componentCover.getSecond().get(node).index(), currentCoverFirstCommunityIndex + i, belongingFactor);
				}
				currentNodes.next();
			}
			currentCoverFirstCommunityIndex += currentCover.communityCount();
			if(!currentCover.getCreationMethod().equals(algo)) {
				algo = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>());
			}
		}
		Cover cover = new Cover(graph, memberships);
		cover.setCreationMethod(algo);
		return cover;
	}

	/**
	 * Restructures a graph to make it compatible to given graph types.
	 * Note that the adapted graph might actually have a corrupted graph types attribute in the sense
	 * that there might be graph types included which are not actually required anymore.
	 * E.g. through the removal of self loops an originally weighted graph might become unweighted, although
	 * the graph type "weighted" will not be removed from the types attribute by this method due to efficiency reasons.
	 * However the graph types attribute of the adapted graph is guaranteed to only include types
	 * which appear in both the graph types attribute of the original graph and the compatible types.
	 * @param graph The graph to be restructured. Its graph types must be set correctly.
	 * @param compatibleTypes The graph types which are regarded compatible.
	 */
	public void makeCompatible(CustomGraph graph, Set<GraphType> compatibleTypes) {
		/*
		 * Directed is checked before weight because e.g. positive edge weights and negative
		 * edge weights might balance each other out in the resulting undirected edge. This
		 * way more information from the original graph is maintained.
		 */
		removeMultiEdges(graph);
		if(graph.isOfType(GraphType.DIRECTED) && ! compatibleTypes.contains(GraphType.DIRECTED)) {
			this.makeUndirected(graph);
		}
		boolean noSelfLoops = false;
		boolean noNegativeWeights = false;
		boolean noZeroWeights = false;
		boolean setToOne = false;
		if(graph.isOfType(GraphType.SELF_LOOPS) && ! compatibleTypes.contains(GraphType.SELF_LOOPS)) {
			noSelfLoops = true;
		}
		if(graph.isOfType(GraphType.WEIGHTED) && ! compatibleTypes.contains(GraphType.WEIGHTED)) {
			setToOne = true;
			noNegativeWeights = true;
			noZeroWeights = true;
		}
		if(graph.isOfType(GraphType.NEGATIVE_WEIGHTS) && ! compatibleTypes.contains(GraphType.NEGATIVE_WEIGHTS)) {
			noNegativeWeights = true;
		}
		if(graph.isOfType(GraphType.ZERO_WEIGHTS) && ! compatibleTypes.contains(GraphType.ZERO_WEIGHTS)) {
			noZeroWeights = true;
		}
		this.redefineEdges(graph, noNegativeWeights, noZeroWeights, noSelfLoops, setToOne);
	}
}
