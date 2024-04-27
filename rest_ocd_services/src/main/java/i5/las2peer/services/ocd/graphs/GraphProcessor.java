package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.Pair;



import org.apache.jena.atlas.iterator.Iter;//TODO: why this iterator? i think iterator is already in java.util
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.*;

/**
 * Pre-processes graphs to facilitate community detection.
 * 
 * @author Sebastian
 *
 */
public class GraphProcessor {

	/**
	 * Sets the graph types of a given graph.
	 * 
	 * @param graph
	 *            A graph without multi edges.
	 */
	public void determineGraphTypes(CustomGraph graph) {
		graph.clearTypes();
		Iterator<Edge> edgesIt = graph.edges().iterator();
		Edge edge;
		Edge reverseEdge;
		while (edgesIt.hasNext()) {
			edge = edgesIt.next();
			double edgeWeight = graph.getEdgeWeight(edge);
			if (edgeWeight != 1 && !graph.getTypes().contains(GraphType.WEIGHTED)) {
				graph.addType(GraphType.WEIGHTED);
			}
			if (edgeWeight == 0 && !graph.getTypes().contains(GraphType.ZERO_WEIGHTS)) {
				graph.addType(GraphType.ZERO_WEIGHTS );
			}
			if (edgeWeight < 0 && !graph.getTypes().contains(GraphType.NEGATIVE_WEIGHTS)) {
				graph.addType(GraphType.NEGATIVE_WEIGHTS);
			}
			if (edge.getSourceNode().equals(edge.getTargetNode()) && !graph.getTypes().contains(GraphType.SELF_LOOPS)) {
				graph.addType(GraphType.SELF_LOOPS);
			}
			reverseEdge = edge.getTargetNode().getEdgeToward(edge.getSourceNode());
			if ((reverseEdge == null || graph.getEdgeWeight(reverseEdge) != edgeWeight) && !graph.getTypes().contains(GraphType.DIRECTED)) {
				graph.addType(GraphType.DIRECTED);
			}			

		}

		if (graph instanceof DynamicGraph) {
			graph.addType(GraphType.DYNAMIC);
		}

		if (graph.getPath() != "" && graph.getPath() != null) {
			if (graph.getEdgeCount() == 0) {
				graph.addType(GraphType.CONTENT_UNLINKED);
			}
			else 
			{
				graph.addType(GraphType.CONTENT_LINKED);
			}
		}
	}

	/**
	 * Transforms a graph into an undirected Graph. For each edge a reverse edge
	 * leading the opposite way is added, if missing. The reverse edge is
	 * assigned the same weight as the original one. If edges in both ways do
	 * already exist, they are assigned the sum of both weights.
	 * 
	 * @param graph
	 *            The graph to be transformed.
	 */
	public void makeUndirected(CustomGraph graph) {

		// copy of the input graph to be used for iteration
		CustomGraph graphCopy = new CustomGraph(graph);

		Iterator<Edge> edges = graphCopy.edges().iterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			double edgeWeight = graphCopy.getEdgeWeight(edge);
			Edge reverseEdge;
			Node target = edge.getTargetNode();
			Node source = edge.getSourceNode();
			reverseEdge = target.getEdgeToward(source);
			if (reverseEdge != null && reverseEdge.getIndex() > edge.getIndex() && !target.equals(source)) {
				graph.combineEdgeWeights(target.getId(), source.getId());

			} else if (reverseEdge == null) {
				reverseEdge = graph.addEdge(UUID.randomUUID().toString(), target.getId(), source.getId());
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}

		}
		graph.removeType(GraphType.DIRECTED);
		graph.setNodeEdgeCountColumnFields(); //update graph edge count info
	}

	/**
	 * Removes multi edges from a graph. Each set of parallel edges is replaced
	 * by an edge whose weight is the sum of the original edge weights. Other
	 * edge attributes correspond to a random original edge.
	 * 
	 * @param graph
	 *            The graph to be transformed.
	 */
	protected void removeMultiEdges(CustomGraph graph) {
		Iterator<Edge> edgesIt = graph.edges().iterator();
		Map<Pair<Integer, Integer>, Double> nodePairWeights = new HashMap<Pair<Integer, Integer>, Double>();
		ArrayList<Edge> edgesToBeRemoved = new ArrayList<>();
		while (edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			Pair<Integer, Integer> nodePair = new Pair<Integer, Integer>(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex());
			Double edgeWeight = nodePairWeights.get(nodePair);
			if (edgeWeight == null) {
				nodePairWeights.put(nodePair, graph.getEdgeWeight(edge));
			} else {
				edgeWeight += graph.getEdgeWeight(edge);
				nodePairWeights.put(nodePair, edgeWeight);
				edgesToBeRemoved.add(edge);
			}
		}

		for (Edge edge : edgesToBeRemoved) {
			graph.removeEdge(edge);
		}
		
		edgesIt = graph.edges().iterator();
		while (edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			double edgeWeight = nodePairWeights
					.get(new Pair<Integer, Integer>(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex()));
			graph.setEdgeWeight(edge, edgeWeight);

		}
	}

	/**
	 * Redefines the edges of a graph according to certain criteria.
	 * 
	 * @param graph
	 *            The graph to be transformed
	 * @param noNegativeWeights
	 *            If true edges with negative weight are removed from the graph.
	 * @param noZeroWeights
	 *            If true edges with weight zero are removed from the graph.
	 * @param noSelfLoops
	 *            If true self loops will be removed from the graph.
	 * @param setToOne
	 *            If true the weight of remaining edges will be set to 1.
	 */
	protected void redefineEdges(CustomGraph graph, boolean noNegativeWeights, boolean noZeroWeights,
			boolean noSelfLoops, boolean setToOne) {
		Iterator<Edge> edgesIt = graph.edges().iterator();

		/*
		 this list will hold edges to be removed. This is needed to avoid edge removal
		 while iterating over edges to avoid unintended side effects.
		 */
		ArrayList<Edge> edgesToRemove = new ArrayList<Edge>();

		while (edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			double edgeWeight = graph.getEdgeWeight(edge);
			if (noNegativeWeights && edgeWeight < 0) {
				//graph.removeEdge(edge);
				edgesToRemove.add(edge);
			} else if (noZeroWeights && edgeWeight == 0) {
				//graph.removeEdge(edge);
				edgesToRemove.add(edge);
			} else if (noSelfLoops && edge.getSourceNode().equals(edge.getTargetNode())) {
				//graph.removeEdge(edge);
				edgesToRemove.add(edge);
			} else if (setToOne) {
				graph.setEdgeWeight(edge, 1);
			}

		}
		if (noSelfLoops) {
			graph.removeType(GraphType.SELF_LOOPS);
		}
		if (setToOne) {
			graph.removeType(GraphType.WEIGHTED);
		}
		if (noNegativeWeights) {
			graph.removeType(GraphType.NEGATIVE_WEIGHTS);
		}
		if (noZeroWeights) {
			graph.removeType(GraphType.ZERO_WEIGHTS);
		}

		/*
		 remove edges that were identified for removal
		 */
		for (Edge edgeToRemove : edgesToRemove){
			graph.removeEdge(edgeToRemove);
		}
	}

	/**
	 * Returns all connected components of a graph.
	 * 
	 * @param graph
	 *            The graph whose connected components are identified.
	 * @return A map containing the connected components and a corresponding
	 *         mapping from the new component nodes to the original graph nodes.
	 */
	public List<Pair<CustomGraph, Map<Node, Node>>> divideIntoConnectedComponents(CustomGraph graph) {
		/*
		 * Iterates over all connected components of the graph creating a copy
		 * for each of them.
		 */
		//TODO: Check usage of connected component algorithm here
		//NodeList[] componentsArray = GraphConnectivity.connectedComponents(graph);
		ConnectedComponents ccAlgo = new ConnectedComponents(graph);
		ccAlgo.compute();
		Iterator<ConnectedComponents.ConnectedComponent> componentsIterator = ccAlgo.iterator();
		List<Pair<CustomGraph, Map<Node, Node>>> componentsList = new ArrayList<Pair<CustomGraph, Map<Node, Node>>>();
		while (componentsIterator.hasNext()) {
			ConnectedComponents.ConnectedComponent component = componentsIterator.next();
			CustomGraph componentGraph = new CustomGraph(graph.getKey()); //Give component graph the same key as the main graph so that algorithms know to which graph they belong
			Map<Node, Node> nodeMap = new HashMap<Node, Node>();
			Map<Node, Node> tmpNodeMap = new HashMap<Node, Node>();
			/*
			 * Sets component nodes
			 */
			Iterator<Node> nodesIt = component.nodes().iterator();
			while (nodesIt.hasNext()) {
				Node originalNode = nodesIt.next();
				Node newNode = componentGraph.addNode(component.id + originalNode.getId());
				componentGraph.setNodeName(newNode, graph.getNodeName(originalNode));
				nodeMap.put(newNode, originalNode);
				tmpNodeMap.put(originalNode, newNode);

			}
			/*
			 * Sets component edges
			 */
			nodesIt = component.nodes().iterator();
			while (nodesIt.hasNext()) {
				Node node = nodesIt.next();
				Iterator<Edge> outEdgesIt = node.leavingEdges().iterator();
				while (outEdgesIt.hasNext()) {
					Edge outEdge = outEdgesIt.next();
					Node target = outEdge.getTargetNode();
					double edgeWeight = graph.getEdgeWeight(outEdge);
					Edge newEdge = componentGraph.addEdge(UUID.randomUUID().toString(),tmpNodeMap.get(node), tmpNodeMap.get(target));
					componentGraph.setEdgeWeight(newEdge, edgeWeight);
				}

			}
			componentsList.add(new Pair<CustomGraph, Map<Node, Node>>(componentGraph, nodeMap));
		}
		return componentsList;
	}

	/**
	 * Merges the covers of the separated connected components of a graph to one
	 * single cover.
	 * 
	 * @param graph
	 *            The graph containing the connected components.
	 * @param componentCovers
	 *            A mapping from covers of all the connected components of a
	 *            graph to a corresponding node mapping, that maps the nodes
	 *            from the connected component to the original graph nodes.
	 * @return The single cover of the original graph.
	 */
	public Cover mergeComponentCovers(CustomGraph graph, List<Pair<Cover, Map<Node, Node>>> componentCovers) {
		int totalCommunityCount = 0;
		for (Pair<Cover, Map<Node, Node>> componentCover : componentCovers) {
			totalCommunityCount += componentCover.getFirst().communityCount();
		}
		Matrix memberships = new CCSMatrix(graph.getNodeCount(), totalCommunityCount);
		Cover currentCover = null;
		CoverCreationLog algo = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
				new HashSet<GraphType>());
		if (!componentCovers.isEmpty()) {
			algo = componentCovers.get(0).getFirst().getCreationMethod();
		}
		Iterator<Node> currentNodesIt;
		Node node;
		int currentCoverFirstCommunityIndex = 0;
		double belongingFactor;
		for (Pair<Cover, Map<Node, Node>> componentCover : componentCovers) {
			currentCover = componentCover.getFirst();
			currentNodesIt = currentCover.getGraph().nodes().iterator();
			while (currentNodesIt.hasNext()) {
				node = currentNodesIt.next();
				for (int i = 0; i < currentCover.communityCount(); i++) {
					belongingFactor = currentCover.getBelongingFactor(node, i);
					memberships.set(componentCover.getSecond().get(node).getIndex(), currentCoverFirstCommunityIndex + i,
							belongingFactor);
				}

			}
			currentCoverFirstCommunityIndex += currentCover.communityCount();
			if (!currentCover.getCreationMethod().equals(algo)) {
				algo = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
						new HashSet<GraphType>());
			}
		}
		Cover cover = new Cover(graph, memberships);
		cover.setCreationMethod(algo);
		return cover;
	}

	/**
	 * Restructures a graph to make it compatible to given graph types. Note
	 * that the adapted graph might actually have a corrupted graph types
	 * attribute in the sense that there might be graph types included which are
	 * not actually required anymore. E.g. through the removal of self loops an
	 * originally weighted graph might become unweighted, although the graph
	 * type "weighted" will not be removed from the types attribute by this
	 * method due to efficiency reasons. However the graph types attribute of
	 * the adapted graph is guaranteed to only include types which appear in
	 * both the graph types attribute of the original graph and the compatible
	 * types.
	 * 
	 * @param graph
	 *            The graph to be restructured. Its graph types must be set
	 *            correctly.
	 * @param compatibleTypes
	 *            The graph types which are regarded compatible.
	 */
	public void makeCompatible(CustomGraph graph, Set<GraphType> compatibleTypes) {
		/*
		 * Directed is checked before weight because e.g. positive edge weights
		 * and negative edge weights might balance each other out in the
		 * resulting undirected edge. This way more information from the
		 * original graph is maintained.
		 */
		removeMultiEdges(graph);
		if (graph.isOfType(GraphType.DIRECTED) && !compatibleTypes.contains(GraphType.DIRECTED)) {
			this.makeUndirected(graph);
		}
		boolean noSelfLoops = false;
		boolean noNegativeWeights = false;
		boolean noZeroWeights = false;
		boolean setToOne = false;
		if (graph.isOfType(GraphType.SELF_LOOPS) && !compatibleTypes.contains(GraphType.SELF_LOOPS)) {
			noSelfLoops = true;
		}
		if (graph.isOfType(GraphType.WEIGHTED) && !compatibleTypes.contains(GraphType.WEIGHTED)) {
			setToOne = true;
			noNegativeWeights = true;
			noZeroWeights = true;
		}
		if (graph.isOfType(GraphType.NEGATIVE_WEIGHTS) && !compatibleTypes.contains(GraphType.NEGATIVE_WEIGHTS)) {
			noNegativeWeights = true;
		}
		if (graph.isOfType(GraphType.ZERO_WEIGHTS) && !compatibleTypes.contains(GraphType.ZERO_WEIGHTS)) {
			noZeroWeights = true;
		}
		this.redefineEdges(graph, noNegativeWeights, noZeroWeights, noSelfLoops, setToOne);
	}

	/**
	 * Transforms an undirected graph into a directed Graph. For each edge a
	 * "reverse" edge (if node A points to node B, then node B also points to
	 * node A) is added. The new edge is assigned the same weight as the
	 * original one.
	 * 
	 * @param graph
	 *            The graph to be transformed
	 * @author YLi
	 */
	public void makeDirected(CustomGraph graph) {
		Iterator<Edge> edgesIt = graph.edges().iterator();
		while (edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			double edgeWeight = graph.getEdgeWeight(edge);
			Edge reverseEdge;
			Node target = edge.getTargetNode();
			Node source = edge.getSourceNode();
			if (target.getIndex() > source.getIndex()) {
				reverseEdge = graph.addEdge(UUID.randomUUID().toString(), target, source);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}

		}
		graph.addType(GraphType.DIRECTED);
	}

	/**
	 * Creates a graph, which is exactly a copy of the input graph
	 * 
	 * @param graph
	 *            The graph to be copied.
	 * @return
	 * 		  	  The graph
	 * @author YLi
	 */
	public CustomGraph copyGraph(CustomGraph graph) {
		CustomGraph graphCopy = new CustomGraph();
		int nodeCount = graph.getNodeCount();
		Node t[] = new Node[nodeCount];
		for (int i = 0; i < nodeCount; i++) {
			t[i] = graphCopy.addNode(Integer.toString(i));
		}
		Iterator<Edge> edges = graph.edges().iterator();
		Edge edge;
		while (edges.hasNext()) {
			edge = edges.next();
			int source = edge.getSourceNode().getIndex();
			int target = edge.getTargetNode().getIndex();
			Edge newEdge = graphCopy.addEdge(UUID.randomUUID().toString(),t[source], t[target]);
			graphCopy.setEdgeWeight(newEdge, graph.getEdgeWeight(edge));

		}
		return graphCopy;
	}
	
	/**
	 * Invert all edge weights in the graph
	 * 
	 * @param graph
	 *            The graph to be transformed.
	 * @author Tobias
	 */
	public void invertEdgeWeights(CustomGraph graph) {
		Iterator<Edge> edges = graph.edges().iterator();
		
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1/graph.getEdgeWeight(edge));

		}
	}
	
	/**
	 * Reverse all edge directions in the graph
	 * 
	 * @param graph
	 *            The graph to be transformed.
	 * @author Tobias
	 */
	public void reverseEdgeDirections(CustomGraph graph) {
		Iterator<Edge> edges = graph.edges().iterator();

		if (graph.isDirected()) {
			while(edges.hasNext()) {
				//TODO: Finish edge reversal
				Edge edge = edges.next();
				graph.addEdge(UUID.randomUUID().toString(), edge.getTargetNode(), edge.getSourceNode());
				graph.removeEdge(edge);
			}
		}
	}
}
