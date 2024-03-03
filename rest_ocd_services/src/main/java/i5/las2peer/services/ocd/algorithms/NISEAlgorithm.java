package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Pair;
import io.reactivex.internal.observers.ForEachWhileObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.HopcroftTarjanBiconnectedComponents;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.functor.VectorAccumulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

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
	
	public static final String SEED_COUNT_NAME = "seedCount";

	public static final String PROBABILITY_NAME = "probability";

	public static final String ACCURACY_NAME = "accuracy";
	
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
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		Pair<CustomGraph, ArrayList<Edge>> biconnectedCoreInformation = filtering(graph);
		CustomGraph biconnectedCore = biconnectedCoreInformation.getFirst();
		ArrayList<Edge> bridges = biconnectedCoreInformation.getSecond();
		ArrayList<Node> seeds = seeding(biconnectedCore);
		ArrayList<ArrayList<Node>> lowConductanceSets = expansion(biconnectedCore, seeds);
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
	private Pair<CustomGraph, ArrayList<Edge>> filtering(CustomGraph graph) throws InterruptedException {
		CustomGraph biconnectedCore = new CustomGraph(graph);
		//HashMap to get the connection between the nodes of the original graph and the biconnectedCore
		HashMap<Edge,Edge> edgeMapping = new HashMap<Edge,Edge>();
		//Add all edges to the hashmap
		Iterator<Edge> graphCursor = graph.edges().iterator();
		Iterator<Edge> biconnectedCoreCursor = biconnectedCore.edges().iterator();
		while(graphCursor.hasNext()) {
			edgeMapping.put(biconnectedCoreCursor.next(), graphCursor.next());
		}
		//Add all nodes to the hashmap (defined globally)
		Iterator<Node> graphNodeCursor = graph.iterator();
		Iterator<Node> biconnectedCoreNodeCursor = biconnectedCore.iterator();
		while(graphNodeCursor.hasNext()) {
			nodeMapping.put(biconnectedCoreNodeCursor.next(), graphNodeCursor.next());
		}

		//TODO: Check this
		//Retrieve all biconnected components of the graph and save the bridges
		HopcroftTarjanBiconnectedComponents bccAlgo = new HopcroftTarjanBiconnectedComponents(biconnectedCore);
		bccAlgo.compute();
		ArrayList<HopcroftTarjanBiconnectedComponents.BiconnectedComponent> biconnectedComponents = bccAlgo.getBiconnectedComponents();
		ArrayList<Edge[]> biconnectedComponentsEdges = new ArrayList<Edge[]>();
		for (HopcroftTarjanBiconnectedComponents.BiconnectedComponent bcc : biconnectedComponents) {
			biconnectedComponentsEdges.add(bcc.edges().toArray(Edge[]::new));
		}
		
		//Remove all biconnected components of size one (as graphs are directed the edgeSize is 2)
		LinkedList<Edge> singleEdgeBiconnectedComponents = new LinkedList<Edge>();
		for (Edge[] edgeArray : biconnectedComponentsEdges) {
			if(edgeArray.length == 2) {
				biconnectedCore.removeEdge(edgeArray[0]);
				biconnectedCore.removeEdge(edgeArray[edgeArray.length-1]);
				singleEdgeBiconnectedComponents.add(edgeArray[0]);
				singleEdgeBiconnectedComponents.add(edgeArray[edgeArray.length-1]);
			}
		}

		//TODO: Check this
		//Find all nodes that are part of the biconnected core
		ConnectedComponents ccAlgo = new ConnectedComponents(biconnectedCore);
		ccAlgo.compute();
		List<Node> coreNodes = List.of(ccAlgo.getGiantComponent().nodes().toArray(Node[]::new));
//		for (Node[] nodeList: connectedComponents) {
//			if(nodeList.length > coreNodes.size()) {
//				coreNodes = nodeList;
//			}
//		}
		
		//Get all bridges (only those from the biconnected core to the whisker)
		ArrayList<Edge> bridges = new ArrayList<Edge>();
		for (Edge edge : singleEdgeBiconnectedComponents) {
			if(coreNodes.contains(edge.getSourceNode()) && !coreNodes.contains(edge.getTargetNode())) {
				//Add the mapped edge (which belongs to graph and not biconnectedCore) to the bridges array
				bridges.add(edgeMapping.get(edge));
			}
		}
		
		//Remove all nodes without neighbors from the biconnected core
		Node[] nodes = biconnectedCore.nodes().toArray(Node[]::new);
		for (Node node : nodes) {
			if(graph.getNeighbours(node).size() == 0) {
				biconnectedCore.removeNode(node);
			}
		}
			
		return new Pair<CustomGraph, ArrayList<Edge>>(biconnectedCore, bridges);
	}
	
	/**
	 * Step 2: Seeding
	 * Finds good seeds in the biconnected core depending on the seed count.
	 * Two methods are available: "spreadHubs" or "graclusCenters"
	 * @param biconnectedCore
	 * @return list of the seed nodes
	 */
	private ArrayList<Node> seeding(CustomGraph biconnectedCore) throws InterruptedException {
		ArrayList<Node> seeds = new ArrayList<Node>();
		seeds = spreadHubs(biconnectedCore);
		return seeds;
	}
	
	/**
	 * Algorithm Spread Hubs chooses an independent set of k (= seedCount) seeds by 
	 * looking at the vertices in order of decreasing degree
	 * @param biconnectedCore
	 * @return list of the seed nodes
	 */
	private ArrayList<Node> spreadHubs(CustomGraph biconnectedCore) throws InterruptedException {
		ArrayList<Node> seeds = new ArrayList<Node>();
		Node[] coreNodesArray = biconnectedCore.nodes().toArray(Node[]::new);
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
					if(entry.getKey().getDegree() > maxDegree) {
						maxDegree = entry.getKey().getDegree();
						maxDegreeNodes = new LinkedList<Map.Entry<Node,Boolean>>();
						maxDegreeNodes.add(entry);
					}
					else {
						if(entry.getKey().getDegree() == maxDegree) {
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
					Iterator<Node> neighborsIt = biconnectedCore.getNeighbours(entry.getKey()).iterator(); //TODO: previously was .neighbors. Check if behaves the same
					while (neighborsIt.hasNext()) {
						Node neighbor = neighborsIt.next();
						coreNodesMap.put(neighbor, true);
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
	private ArrayList<ArrayList<Node>> expansion(CustomGraph biconnectedCore, ArrayList<Node> seeds) throws InterruptedException {
		Node[] seedsArray = seeds.toArray(Node[]::new);
		ArrayList<ArrayList<Node>> lowConductanceSets = new ArrayList<ArrayList<Node>>(seedsArray.length);
		for(int i = 0; i < seedsArray.length; i++) {
			lowConductanceSets.add(i, computeLowConductanceSet(biconnectedCore, seedsArray[i]));
		}
		return lowConductanceSets;
	}
	
	/**
	 * Computes the lowConductanceSet for a given seed.
	 * @param biconnectedCore
	 * @param seed to compute the lowConductanceSet for
	 * @return List of nodes belong to the set of low conductance for that seed node
	 */
	private ArrayList<Node> computeLowConductanceSet(CustomGraph biconnectedCore, Node seed) throws InterruptedException {
		//Set of restart nodes
		Set<Node> restartNodes = new HashSet<Node>();
		restartNodes.add(seed);
		//Iterate over all neighbors add them to the restart nodes
		Iterator<Node> neighbors = biconnectedCore.getNeighbours(seed).iterator();
		while (neighbors.hasNext()) {
			restartNodes.add(neighbors.next());
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
				if(r.containsKey(v) && (r.get(v) > (v.getDegree()*accuracy))) {
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
					Iterator<Node> neighborsIterator = biconnectedCore.getNeighbours(v).iterator();
					while(neighborsIterator.hasNext()) {
						Node neighbor = neighborsIterator.next();
						//Update r of the neighbor node
						if(r.containsKey(neighbor)) {
							double newR = r.get(neighbor) + ((probability * r.get(v))/(2*biconnectedCore.getNeighbours(v).size()));
							r.put(neighbor, newR);
						}
						else {
							double newR = 0 + ((probability * r.get(v))/(2*biconnectedCore.getNeighbours(v).size()));
							r.put(neighbor, newR);
						}
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
			sortedNodes.put((x.get(v)/v.getDegree()), v);
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
		
        //Parse into ArrayList<Node> and return minConductanceSet
        ArrayList<Node> minConductanceSet = new ArrayList<Node>();
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
	private double calculateConductance(HashSet<Node> nodeSet, CustomGraph biconnectedCore) throws InterruptedException {
		// Number of edges between the nodeSet and the coreNodes\nodeSet
		int numberC_V_C = 0;
		// Number of edges between the nodeSet and the coreNodes
		int numberC_V = 0;
		// Number of edges between the coreNodes\nodeSet and the coreNodes
		int numberV_C_V = 0;
		//Number of coreEdges
		int allCoreEdges = 0;
		
		//Count the numbers of edges
		for (Node node : biconnectedCore.nodes().toArray(Node[]::new)) {
			Iterator<Node> neighbors = biconnectedCore.getNeighbours(node).iterator();
			while (neighbors.hasNext()) {
				Node neighbor = neighbors.next();
				if(nodeSet.contains(node)) {
					if(!nodeSet.contains(neighbor)) {
						numberC_V_C++;
					}
					numberC_V++;
				}
				allCoreEdges++;
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
	private Matrix propagation(CustomGraph graph, ArrayList<ArrayList<Node>> lowConductanceSets, ArrayList<Edge> bridges, CustomGraph biconnectedCore) throws InterruptedException {
		Matrix memberships = new Basic2DMatrix(graph.getNodeCount(), lowConductanceSets.size());
		memberships = memberships.blank();
		
		//iterate over all communities and set the values for all coreNodes
		for (int i = 0; i < lowConductanceSets.size(); i++) {
			Node[] currentCommunity = lowConductanceSets.get(i).toArray(Node[]::new);
			//iterate over all the nodes in that community
			for (Node node : currentCommunity) {
				memberships.set(nodeMapping.get(node).getIndex(), i, 1);
			}
		}
				
		//Determine the whiskers and copy the value of the corresponding coreNodes to the whiskerNodes
		while (!bridges.isEmpty()) {
			Edge bridge = bridges.get(0);
			//Remove the bridge
			bridges.remove(0);
			
			//Set core and whisker node
			//Bridges are saved so that the source is the coreNode
			Node coreNode;
			Node whiskerNode;
			coreNode = bridge.getSourceNode();
			whiskerNode = bridge.getTargetNode();
			
			//Determine the whole whisker
			HashSet<Node> whisker = new HashSet<Node>();
			LinkedList<Node> toSearch = new LinkedList<Node>();
			toSearch.add(whiskerNode);
			
			while(!toSearch.isEmpty()) {
				Node currentNode = toSearch.pop();
				if(!whisker.contains(currentNode)) {
					whisker.add(currentNode);
					//Add all neighbors to toSearch
					Iterator<Node> neighbors = biconnectedCore.getNeighbours(currentNode).iterator();
					while (neighbors.hasNext()) {
						Node neighbor = neighbors.next();
						if(!toSearch.contains(neighbor) && !whisker.contains(neighbor) && !coreNode.equals(neighbor)) {
							toSearch.add(neighbor);
						}
					}
				}
			}
			
			//Copy the values of the coreNode to the whisker nodes
			Vector coreRow = memberships.getRow(coreNode.getIndex());
			for (Node currentWhiskerNode: whisker) {
				memberships.setRow(currentWhiskerNode.getIndex(), coreRow);
			}
		}
		
		return memberships;
	}
	
}
