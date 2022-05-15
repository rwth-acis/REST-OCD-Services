package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Pair;
import io.reactivex.internal.observers.ForEachWhileObserver;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.functor.VectorAccumulator;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Graph;
import y.base.Node;
import y.base.NodeList;
import y.base.NodeCursor;
import y.algo.GraphConnectivity;

/**
 * The original version of the overlapping community detection algorithm by Joyce Jiyoung Whang, David F. Gleich, and Inderjit S. Dhillon:
 * Overlapping Community Detection Using Neighborhood-Inflated Seed Expansion
 * https://doi.org/10.1109/TKDE.2016.2518687
 * Handles weighted and undirected graphs.
 * Creates overlapping communities by running the four phases: Filtering, Seeding, Expansion, Propagation
 */
public class NISEAlgorithm implements OcdAlgorithm {

	/**
	 * The number of seed which will be determined by the seed strategy. This number
	 * will later result in the number of communities.
	 */
	private int seedCount = 10;
	/**
	 * The probability parameter is required in step 3 (Expansion).
	 * The standard value should be set to 0.99 and must be between 0 and 1.
	 */
	private double probability = 0.99;
	/**
	 * The accuracy parameter is required in step 3 (Expansion).
	 * It controls the size of the resulting communities.
	 * If the accuracy is large (e.g. 10^-2) the resulting communities are small.
	 * If the accuracy is small (e.g. 10^-8) the resulting communities are large.
	 */
	private double accuracy = 0.00000001;
	
	/**
	 * Maps the nodes of the biconnectedCore to the Nodes of the Graph
	 */
	HashMap<Node,Node> nodeMapping = new HashMap<Node,Node>();
	
	/*
	 * PARAMETER NAMES
	 */
	
	protected static final String SEED_COUNT_NAME = "seedCount";
		
	protected static final String PROBABILITY_NAME = "probability";
	
	protected static final String ACCURACY_NAME = "accuracy";
	
	/**
	 * Creates a standard instance of the algorithm.
	 * All attributes are assigned their default values.
	 */
	public NISEAlgorithm() {
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.UNWEIGHTED);
		compatibilities.add(GraphType.UNDIRECTED);
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		Pair<Graph, EdgeList> biconnctedCoreInformation = filtering(graph);
		Graph biconnectedCore = biconnctedCoreInformation.getFirst();
		EdgeList bridges = biconnctedCoreInformation.getSecond();
		NodeList seeds = seeding(biconnectedCore);
		NodeList[] lowConductanceSets = expansion(biconnectedCore, seeds);
		Matrix memberships = propagation(graph, lowConductanceSets, bridges, biconnectedCore);
		return new Cover(graph, memberships);
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(SEED_COUNT_NAME, Integer.toString(seedCount));
		parameters.put(PROBABILITY_NAME, Double.toString(probability));
		parameters.put(ACCURACY_NAME, Double.toString(accuracy));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(SEED_COUNT_NAME)) {
			seedCount = Integer.parseInt(parameters.get(SEED_COUNT_NAME));
			if(seedCount <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(SEED_COUNT_NAME);
		}
		if(parameters.containsKey(ACCURACY_NAME)) {
			accuracy = Double.parseDouble(parameters.get(ACCURACY_NAME));
			if(accuracy <= 0 || accuracy >= 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(ACCURACY_NAME);
		}
		if(parameters.containsKey(PROBABILITY_NAME)) {
			probability = Double.parseDouble(parameters.get(PROBABILITY_NAME));
			if(probability <= 0 || probability >= 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(PROBABILITY_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.NISE_ALGORITHM;
	}
	
	/**
	 * Step 1: Filtering
	 * Removes regions of the graph that are only connected to the rest via one edge. 
	 * The new graph is called the biconnected core
	 * @param graph
	 * @return Pair<Graph, EdgeList> with the biconnectedCore and the bridges
	 */
	private Pair<Graph, EdgeList> filtering(CustomGraph graph) {
		Graph biconnectedCore = graph.createCopy();
		//HashMap to get the connection between the nodes of the original graph and the biconnectedCore
		HashMap<Edge,Edge> edgeMapping = new HashMap<Edge,Edge>();
		//Add all edges to the hashmap
		EdgeCursor graphCursor = graph.edges();
		EdgeCursor biconnectedCoreCursor = biconnectedCore.edges();
		while(graphCursor.ok()) {
			edgeMapping.put(biconnectedCoreCursor.edge(), graphCursor.edge());
			biconnectedCoreCursor.next();
			graphCursor.next();
		}
		//Add all nodes to the hashmap (defined globally)
		NodeCursor graphNodeCursor = graph.nodes();
		NodeCursor biconnectedCoreNodeCursor = biconnectedCore.nodes();
		while(graphNodeCursor.ok()) {
			nodeMapping.put(biconnectedCoreNodeCursor.node(), graphNodeCursor.node());
			biconnectedCoreNodeCursor.next();
			graphNodeCursor.next();
		}
		
		//Retrieve all biconnected components of the graph and save the bridges
		EdgeList[] biconnectedComponents = GraphConnectivity.biconnectedComponents(biconnectedCore);
		
		//Remove all biconnected components of size one (as graphs are directed the edgeSize is 2)
		LinkedList<Edge> singleEdgeBiconnectedComponents = new LinkedList<Edge>();
		for (EdgeList edgeList : biconnectedComponents) {
			if(edgeList.size() == 2) {
				biconnectedCore.removeEdge(edgeList.firstEdge());
				biconnectedCore.removeEdge(edgeList.lastEdge());
				singleEdgeBiconnectedComponents.add(edgeList.firstEdge());
				singleEdgeBiconnectedComponents.add(edgeList.lastEdge());
			}
		}
		
		//Find all nodes that are part of the biconnected core
		NodeList[] connectedComponents = GraphConnectivity.connectedComponents(biconnectedCore);
		NodeList coreNodes = new NodeList();
		for (NodeList nodeList: connectedComponents) {
			if(nodeList.size() > coreNodes.size()) {
				coreNodes = nodeList;
			}
		}
		
		//Get all bridges (only those from the biconnected core to the whisker)
		EdgeList bridges = new EdgeList();
		for (Edge edge : singleEdgeBiconnectedComponents) {
			if(coreNodes.contains(edge.source()) && !coreNodes.contains(edge.target())) {
				//Add the mapped edge (which belongs to graph and not biconnectedCore) to the bridges array
				bridges.add(edgeMapping.get(edge));
			}
		}
		
		//Remove all nodes without neighbors from the biconnected core
		Node[] nodes = biconnectedCore.getNodeArray();
		for (Node node : nodes) {
			if(node.neighbors().size() == 0) {
				biconnectedCore.removeNode(node);
			}
		}
			
		return new Pair<Graph, EdgeList>(biconnectedCore, bridges);
	}
	
	/**
	 * Step 2: Seeding
	 * Finds good seeds in the biconnected core depending on the seed count.
	 * Two methods are available: "spreadHubs" or "graclusCenters"
	 * @param biconnectedCore
	 * @return list of the seed nodes
	 */
	private NodeList seeding(Graph biconnectedCore) {
		NodeList seeds = new NodeList();
		seeds = spreadHubs(biconnectedCore);
		return seeds;
	}
	
	/**
	 * Algorithm Spread Hubs chooses an independent set of k (= seedCount) seeds by 
	 * looking at the vertices in order of decreasing degree
	 * @param biconnected core
	 * @return list of the seed nodes
	 */
	private NodeList spreadHubs(Graph biconnectedCore) {
		NodeList seeds = new NodeList();
		Node[] coreNodesArray = biconnectedCore.getNodeArray();
		ConcurrentHashMap<Node, Boolean> coreNodesMap = new ConcurrentHashMap<Node, Boolean>();
		for (Node node : coreNodesArray) {
			// All nodes of the core are unmarked
			coreNodesMap.put(node, false);
		}
		boolean allMarked = false;
		
		while(seeds.size() < seedCount && !allMarked) { //max seedCount times
			//Get nodes with the maximal degree
			int maxDegree = 0;
			LinkedList<Map.Entry<Node,Boolean>> maxDegreeNodes = new LinkedList<Map.Entry<Node,Boolean>>();
			//iterate over all entries to
			allMarked = true;
	        Set<Map.Entry<Node,Boolean>> coreNodesSet = coreNodesMap.entrySet();
	        for(Map.Entry<Node,Boolean> entry : coreNodesSet)	{        	
				if(!entry.getValue()) {
					//Indicate that so far not every node has been marked
					allMarked = false;
					if(entry.getKey().degree() > maxDegree) {
						maxDegree = entry.getKey().degree();
						maxDegreeNodes = new LinkedList<Map.Entry<Node,Boolean>>();
						maxDegreeNodes.add(entry);
					}
					else {
						if(entry.getKey().degree() == maxDegree) {
							maxDegreeNodes.add(entry);
						}
					}
				}
			}
			
			//add the maxDegreeNodes to the seed list, mark them and their neighbors
			for (Map.Entry<Node, Boolean> entry : maxDegreeNodes) {
				if(!entry.getValue()) {
					seeds.add(entry.getKey());
					entry.setValue(true);
					//Iterate over all neighbors and mark them as well
					NodeCursor neighborsCursor = entry.getKey().neighbors();
					for (int j = 0; j < neighborsCursor.size(); j++) {
						Node neighbor = neighborsCursor.node();
						coreNodesMap.put(neighbor, true);
						neighborsCursor.next();
					}
				}
			}
		}
		
		return seeds;
	}
	
	/**
	 * Step 3: Expansion
	 * Expansion of the seeds using a personalized PageRank clustering scheme.
	 * @param biconnectedCore
	 * @param seeds which should be expanded
	 * @return Array of expanded clusters
	 */
	private NodeList[] expansion(Graph biconnectedCore, NodeList seeds) {
		Node[] seedsArray = seeds.toNodeArray();
		NodeList[] lowConductanceSets = new NodeList[seedsArray.length];
		for(int i = 0; i < seedsArray.length; i++) {
			lowConductanceSets[i] = computeLowConductanceSet(biconnectedCore, seedsArray[i]);
		}
		return lowConductanceSets;
	}
	
	/**
	 * Computes the lowConductanceSet for a given seed.
	 * @param biconnectedCore
	 * @param seed to compute the lowConductanceSet for
	 * @return List of nodes belong to the set of low conductance for that seed node
	 */
	private NodeList computeLowConductanceSet(Graph biconnectedCore, Node seed) {
		//Set of restart nodes
		Set<Node> restartNodes = new HashSet<Node>();
		restartNodes.add(seed);
		//Iterate over all neighbors add them to the restart nodes
		NodeCursor neighbors = seed.neighbors();
		for (int j = 0; j < neighbors.size(); j++) {
			restartNodes.add(neighbors.node());
			neighbors.next();
		}
		
		HashMap<Node, Double> x = new HashMap<Node, Double>();
		//Initialize r with r=1 if node is in restartNodes (else r=0 is expected)
		ConcurrentHashMap<Node, Double> r = new ConcurrentHashMap<Node, Double>();
		for (Node node : restartNodes) {
			r.put(node, 1.0);
		}
		
		//counter to count if there is a node in r for which r > deg(v)*accuracy
		boolean stop = false;
		while(!stop) {
			//go over all elements of the hashMap as long as there is a node that fulfills the next if statement
			stop = true;
			for (Map.Entry<Node, Double> entry : r.entrySet()) {
				Node v = entry.getKey();
				if(r.containsKey(v) && (r.get(v) > (v.degree()*accuracy))) {
					stop = false;
					//Update the value in x for v
					if(x.containsKey(v)) {
						double newX = x.get(v) + ((1-probability)*r.get(v));
						x.put(v, newX);
					}
					else {
						double newX = 0 + ((1-probability)*r.get(v));
						x.put(v, newX);
					}
					
					//Update r of all neighbor nodes
					NodeCursor neighborsCursor = v.neighbors();
					while(neighborsCursor.ok()) {
						Node neighbor = neighborsCursor.node();
						//Update r of the neighbor node
						if(r.containsKey(neighbor)) {
							double newR = r.get(neighbor) + ((probability * r.get(v))/(2*v.neighbors().size()));
							r.put(neighbor, newR);
						}
						else {
							double newR = 0 + ((probability * r.get(v))/(2*v.neighbors().size()));
							r.put(neighbor, newR);
						}
						neighborsCursor.next();
					}
					
					//Update r of v
					double newR = ((probability * r.get(v)) / 2);
					r.put(v, newR);
				}
			}
		}
		
		//sort vertices by decreasing x(v)/deg(v)
		TreeMap<Double, Node> sortedNodes = new TreeMap<Double, Node>();
		for (Map.Entry<Node, Double> entry : x.entrySet()) {
			Node v = entry.getKey();
			sortedNodes.put((x.get(v)/v.degree()), v);
		}
		
		//Calculate the min conductance of the decreasing set        
        HashSet<Node> currentSet = new HashSet<Node>();
        double currentMinConductance = 1.1; //Conductance is <= 1
        Node[] currentMinConductanceSet = new Node[0];
        //iterate over all entries to
        Set<Map.Entry<Double,Node>> descendingEntrySet = sortedNodes.descendingMap().entrySet();
        for(Map.Entry<Double,Node> entry : descendingEntrySet) {
        	currentSet.add(entry.getValue());
        	double conductance = calculateConductance(currentSet, biconnectedCore);
            if(conductance < currentMinConductance) {
            	currentMinConductance = conductance;
            	currentMinConductanceSet = currentSet.toArray(new Node[currentSet.size()]);
            }
        }
		
        //Parse into NodeList and return minConductanceSet
        NodeList minConductanceSet = new NodeList();
        for (Node node : currentMinConductanceSet) {
			minConductanceSet.add(node);
		}

		return minConductanceSet;
	}
	
	/**
	 * Calculates the conductance of a nodeSet
	 * @param nodeSet to calculate the conductance for
	 * @param biconnectedCore
	 * @return calculated conductance
	 */
	private double calculateConductance(HashSet<Node> nodeSet, Graph biconnectedCore) {
		// Number of edges between the nodeSet and the coreNodes\nodeSet
		int numberC_V_C = 0;
		// Number of edges between the nodeSet and the coreNodes
		int numberC_V = 0;
		// Number of edges between the coreNodes\nodeSet and the coreNodes
		int numberV_C_V = 0;
		//Number of coreEdges
		int allCoreEdges = 0;
		
		//Count the numbers of edges
		for (Node node : biconnectedCore.getNodeArray()) {
			NodeCursor neighbors = node.neighbors();
			while (neighbors.ok()) {
				Node neighbor = neighbors.node();
				if(nodeSet.contains(node)) {
					if(!nodeSet.contains(neighbor)) {
						numberC_V_C++;
					}
					numberC_V++;
				}
				allCoreEdges++;
				neighbors.next();
			}
		}
		
		//Calculate V_C_V
		numberC_V_C = numberC_V_C/2;
		numberC_V = numberC_V/2;
		numberV_C_V = (allCoreEdges/2) - numberC_V;
		
		//Calculate Conductance
		double conductance = 0.0;
		if(numberV_C_V != 0) {
			if(numberC_V < numberV_C_V) {
				conductance = (((double)numberC_V_C)/((double)numberC_V));
			}
			else {
				conductance = (((double)numberC_V_C)/((double)numberV_C_V));
			}
		}
		return conductance;
	}
	
	/**
	 * Step 4: Propagation
	 * Further expands the communities to the regions that were removed during Step 1 Filtering
	 * and produces the membership matrix.
	 * @param graph
	 * @param lowConductanceSets calculated in Step3 which equal the communities
	 * @param bridges calculated in Step1 which connect the biconnectedCore to the whiskers
	 * @param biconnectedCore
	 * @return membership matrix
	 */
	private Matrix propagation(CustomGraph graph, NodeList[] lowConductanceSets, EdgeList bridges, Graph biconnectedCore) {
		Matrix memberships = new Basic2DMatrix(graph.nodeCount(), lowConductanceSets.length);
		memberships = memberships.blank();
		
		//iterate over all communities and set the values for all coreNodes
		for (int i = 0; i < lowConductanceSets.length; i++) {
			Node[] currentCommunity = lowConductanceSets[i].toNodeArray();
			//iterate over all the nodes in that community
			for (Node node : currentCommunity) {
				memberships.set(nodeMapping.get(node).index(), i, 1);
			}
		}
				
		//Determine the whiskers and copy the value of the corresponding coreNodes to the whiskerNodes
		while (!bridges.isEmpty()) {
			Edge bridge = bridges.firstEdge();
			//Remove the bridge
			bridges.remove(0);
			
			//Set core and whisker node
			//Bridges are saved so that the source is the coreNode
			Node coreNode;
			Node whiskerNode;
			coreNode = bridge.source();
			whiskerNode = bridge.target();
			
			//Determine the whole whisker
			HashSet<Node> whisker = new HashSet<Node>();
			LinkedList<Node> toSearch = new LinkedList<Node>();
			toSearch.add(whiskerNode);
			
			while(!toSearch.isEmpty()) {
				Node currentNode = toSearch.pop();
				if(!whisker.contains(currentNode)) {
					whisker.add(currentNode);
					//Add all neighbors to toSearch
					NodeCursor neighbors = currentNode.neighbors();
					for(int j = 0; j < neighbors.size(); j++) {
						Node neighbor = neighbors.node();
						if(!toSearch.contains(neighbor) && !whisker.contains(neighbor) && !coreNode.equals(neighbor)) {
							toSearch.add(neighbor);
						}
						neighbors.next();
					}
				}
			}
			
			//Copy the values of the coreNode to the whisker nodes
			Vector coreRow = memberships.getRow(coreNode.index());
			for (Node currentWhiskerNode: whisker) {
				memberships.setRow(currentWhiskerNode.index(), coreRow);
			}
		}
		
		return memberships;
	}
	
}
