package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.*;

import org.graphstream.graph.Edge;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.graph.Node;

/**
 * Implements the Newman Benchmark Model.
 * @author Sebastian
 *
 */
public class NewmanBenchmark implements GroundTruthBenchmark {
	
	/**
	 * Defines the number of external edges of each ground truth community.
	 * The default value is 0. Must be in [0, 8].
	 */
	private int externalEdges = 0;
	
	/*
	 * PARAMETER NAMES
	 */
	protected static final String EXTERNAL_EDGES_NAME = "externalEdges";
	
	/**
	 * Creates a standardized instance of the benchmark model.
	 */
	public NewmanBenchmark() {
	}
	
	public NewmanBenchmark(int externalEdges) {
		if(externalEdges > 8) {
			externalEdges = 8;
		}
		else if(externalEdges < 0) {
			externalEdges = 0;
		}
		this.externalEdges = externalEdges;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(EXTERNAL_EDGES_NAME, Integer.toString(externalEdges));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.containsKey(EXTERNAL_EDGES_NAME)) {
			externalEdges = Integer.parseInt(parameters.get(EXTERNAL_EDGES_NAME));
			parameters.remove(EXTERNAL_EDGES_NAME);
			if(externalEdges < 0 || externalEdges > 8) {
				throw new IllegalArgumentException();
			}
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Cover createGroundTruthCover() throws InterruptedException {
		CustomGraph graph = new CustomGraph();
		Matrix membershipMatrix = new CCSMatrix(128, 4);
		Random rand = new Random();
		/*
		 * Randomizes the order of the node indices to randomly assign them to 4 groups.
		 */
		List<Node> nodeOrder = new ArrayList<Node>();
		for(int i=0; i<128; i++) {
			Node node = graph.addNode(Integer.toString(i));
			graph.setNodeName(node, Integer.toString(node.getIndex()));
			nodeOrder.add(node);
		}
		Collections.shuffle(nodeOrder);
		Map<Node, Integer> groupMap = new HashMap<Node, Integer>();
		for(int i=0; i<4; i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			/*
			 * Defines the current group and the corresponding membership entries.
			 */
			List<Node> group = nodeOrder.subList(i*32, (i+1)*32);
			for(Node node : group) {
				membershipMatrix.set(node.getIndex(), i, 1);
				groupMap.put(node, i);
			}
			/*
			 * Creates edges until each group node has the required amount of internal edges.
			 */
			List<Node> unsatisfiedNodes = new ArrayList<Node>(group);
			while(!unsatisfiedNodes.isEmpty()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				int nodeAListIndex = rand.nextInt(unsatisfiedNodes.size());
				Node nodeA = unsatisfiedNodes.get(nodeAListIndex);
				boolean edgeCreated = generateRandomInternalEdge(graph, unsatisfiedNodes, nodeA, rand);
				if(!edgeCreated) {
					redesignInternalEdges(graph, group, unsatisfiedNodes, nodeA, rand);
				}
			}
		}
		if(externalEdges > 0) {
			List<Node> unsatisfiedNodes = new ArrayList<Node>(nodeOrder);
			while(!unsatisfiedNodes.isEmpty()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				int nodeAListIndex = rand.nextInt(unsatisfiedNodes.size());
				Node nodeA = unsatisfiedNodes.get(nodeAListIndex);
				boolean edgeCreated = generateRandomExternalEdge(graph, groupMap, unsatisfiedNodes, nodeA, rand);
				if(!edgeCreated) {
					redesignExternalEdges(graph, groupMap, nodeOrder, unsatisfiedNodes, nodeA, rand);
				}
			}
		}
		Cover cover = new Cover(graph, membershipMatrix);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.GROUND_TRUTH, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	/**
	 * Tries to generate a random internal edge between node A and another unsatisfied Node.
	 * An edge must only be created with a node which is not yet a neighbor of A. Returns true after success, otherwise false.
	 * @param graph The graph which is being created.
	 * @param unsatisfiedNodes A list of nodes which still require additional internal edges.
	 * @param nodeA An unsatisfied node for which the edge should be created.
	 * @param rand A generator for random numbers, included for performance.
	 * @return TRUE if an edge could be created, or FALSE if all unsatisfied nodes are already neighbors of A.
	 */
	private boolean generateRandomInternalEdge(CustomGraph graph, List<Node> unsatisfiedNodes, Node nodeA, Random rand) {
		List<Node> potentialNeighbors = new ArrayList<Node>(unsatisfiedNodes);
		potentialNeighbors.remove(nodeA);
		boolean edgeCreated = false;
		while(potentialNeighbors.size() > 0 && !edgeCreated) {
			int nodeBListIndex = rand.nextInt(potentialNeighbors.size());
			Node nodeB = potentialNeighbors.get(nodeBListIndex);
			if(!nodeA.hasEdgeBetween(nodeB)) {
				edgeCreated = true;
				graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
				graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeA);
				/*
				 * Nodes are removed from the unsatisfied nodes when they have reached
				 * the bound for the amount of internal edges.
				 */
				if(nodeA.getOutDegree() == 16 - externalEdges) {
					unsatisfiedNodes.remove(nodeA);
				}
				if(nodeB.getOutDegree() == 16 - externalEdges) {
					unsatisfiedNodes.remove(nodeB);
				}
			}
			else {
				potentialNeighbors.remove(nodeB);
			}
		}
		return edgeCreated;
	}
	
	/**
	 * Deletes an existing internal edge to create a new internal edge for the unsatisfied node A.
	 * The existing edge is chosen as an edge incident to a satisfied node B, which is not a neighbor of A.
	 * After deleting the existing edge an edge between A and B is created. Finally the list of unsatisfied nodes is updated.
	 * @param graph The graph which is being created
	 * @param group The group whose internal edges are being created.
	 * @param unsatisfiedNodes A list of nodes which still require additional internal edges.
	 * @param nodeA The node for which a new edge will be created.
	 * @param rand A generator for random numbers, included for performance.
	 */
	private void redesignInternalEdges(CustomGraph graph, List<Node> group, List<Node> unsatisfiedNodes, Node nodeA, Random rand) {
		List<Node> potentialNeighbors = new ArrayList<Node>(group);
		potentialNeighbors.removeAll(unsatisfiedNodes);
		/*
		 * Searches a non neighbor node B for A
		 */
		Node nodeB = null;
		boolean nodeBFound = false;
		while(!nodeBFound) {
			int nodeBListIndex = rand.nextInt(potentialNeighbors.size());
			nodeB = potentialNeighbors.get(nodeBListIndex);
			if(!nodeA.hasEdgeBetween(nodeB)) {
				nodeBFound = true;
			}
			else {
				potentialNeighbors.remove(nodeB);
			}
		}
		/*
		 * Deletes an edge incident to B and creates
		 * a new edge between A and B
		 */
		Edge outEdge = nodeB.leavingEdges().skip(nodeB.leavingEdges().count() - 1).findFirst().get();
		Node nodeC = outEdge.getTargetNode();
		Edge inEdge = nodeB.getEdgeFrom(nodeC);
		graph.removeEdge(outEdge);
		graph.removeEdge(inEdge);
		graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
		graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeA);
		if(!unsatisfiedNodes.contains(nodeC)) {
			unsatisfiedNodes.add(nodeC);
		}
		if(nodeA.getOutDegree() == 16 - externalEdges) {
			unsatisfiedNodes.remove(nodeA);
		}
	}
	
	/**
	 * Tries to generate a random external edge between node A and another unsatisfied Node.
	 * An edge must only be created with a node that is not yet a neighbor of A and that belongs to a different group than A.
	 * Returns true after success, otherwise false.
	 * @param graph The graph which is being created.
	 * @param groupMap A mapping containing the group id of each node.
	 * @param unsatisfiedNodes A list of nodes which still require additional external edges.
	 * @param nodeA An unsatisfied node for which the edge should be created.
	 * @param rand A generator for random numbers, included for performance.
	 * @return TRUE if an edge could be created, or FALSE if all unsatisfied nodes are either neighbors of A or belong to the same community.
	 */
	private boolean generateRandomExternalEdge(CustomGraph graph, Map<Node, Integer> groupMap, List<Node> unsatisfiedNodes, Node nodeA, Random rand) {
		List<Node> potentialNeighbors = new ArrayList<Node>(unsatisfiedNodes);
		potentialNeighbors.remove(nodeA);
		boolean edgeCreated = false;
		while(potentialNeighbors.size() > 0 && !edgeCreated) {
			int nodeBListIndex = rand.nextInt(potentialNeighbors.size());
			Node nodeB = potentialNeighbors.get(nodeBListIndex);
			if(!nodeA.hasEdgeBetween(nodeB) && groupMap.get(nodeA) != groupMap.get(nodeB)) {
				edgeCreated = true;
				graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
				graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeA);
				/*
				 * Nodes are removed from the unsatisfied nodes when they have reached
				 * the bound for the amount of total edges.
				 */
				if(nodeA.getOutDegree() == 16) {
					unsatisfiedNodes.remove(nodeA);
				}
				if(nodeB.getOutDegree() == 16) {
					unsatisfiedNodes.remove(nodeB);
				}
			}
			else {
				potentialNeighbors.remove(nodeB);
			}
		}
		return edgeCreated;
	}
	
	/**
	 * Deletes an existing external edge to create a new external edge for the unsatisfied node A.
	 * The existing edge is chosen as an edge incident to a satisfied node B, which is not a neighbor of A and belongs to a different community than A.
	 * After deleting the existing edge an edge between A and B is created. Finally the list of unsatisfied nodes is updated.
	 * @param graph The graph which is being created
	 * @param groupMap A mapping containing the group id of each node.
	 * @param nodes A list of all nodes of the graph.
	 * @param unsatisfiedNodes A list of nodes which still require additional external edges.
	 * @param nodeA The node for which a new edge will be created.
	 * @param rand A generator for random numbers, included for performance.
	 */
	private void redesignExternalEdges(CustomGraph graph, Map<Node, Integer> groupMap, List<Node> nodes, List<Node> unsatisfiedNodes, Node nodeA, Random rand) throws InterruptedException {
		List<Node> potentialNeighbors = new ArrayList<Node>(nodes);
		potentialNeighbors.removeAll(unsatisfiedNodes);
		/*
		 * Searches a non neighbor node B from another group for A.
		 */
		Node nodeB = null;
		boolean nodeBFound = false;
		while(!nodeBFound) {
			int nodeBListIndex = rand.nextInt(potentialNeighbors.size());
			nodeB = potentialNeighbors.get(nodeBListIndex);
			if(!nodeA.hasEdgeBetween(nodeB) && groupMap.get(nodeA) != groupMap.get(nodeB)) {
				nodeBFound = true;
			}
			else {
				potentialNeighbors.remove(nodeB);
			}
		}
		/*
		 * Deletes an external edge incident to B and creates
		 * a new edge between A and B
		 */
		Iterator<Node> neighbors = graph.getSuccessorNeighbours(nodeB).iterator();
		Node neighbor = null;
		boolean externalNeighborFound = false;
		while(neighbors.hasNext() && !externalNeighborFound) {
			neighbor = neighbors.next();
			if(groupMap.get(nodeB) != groupMap.get(neighbor)) {
				externalNeighborFound = true;
			}
		}
		Edge outEdge = nodeB.getEdgeToward(neighbor);
		Edge inEdge = nodeB.getEdgeFrom(neighbor);
		graph.removeEdge(outEdge);
		graph.removeEdge(inEdge);
		graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
		graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeA);
		if(!unsatisfiedNodes.contains(neighbor)) {
			unsatisfiedNodes.add(neighbor);
		}
		if(nodeA.getOutDegree() == 16) {
			unsatisfiedNodes.remove(nodeA);
		}
	}

}