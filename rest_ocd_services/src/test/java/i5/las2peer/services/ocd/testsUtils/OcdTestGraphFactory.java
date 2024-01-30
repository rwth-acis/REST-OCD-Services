package i5.las2peer.services.ocd.testsUtils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GmlGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.NodeContentEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.NodeWeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.*;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * Provides graphs for testing purposes.
 * @author Sebastian
 *
 */
public class OcdTestGraphFactory {
	
	public static CustomGraph getTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.twoCommunitiesName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[9], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[5]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getAperiodicTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.aperiodicTwoCommunitiesName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[10]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	/**
	 * @return a directed chain graph of 10 nodes
	 */
	public static CustomGraph getSimpleDirectedChainGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("SimpleChain");
		Node n[] = new Node[10];
		for (int i = 0; i < 10; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		for (int i = 0; i < 9; i++) {
			graph.addEdge(UUID.randomUUID().toString(), n[i], n[i + 1]);
		}
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return Generate directed or undirected simple hub graphs
	 */
	public static CustomGraph generateHubGraph(Boolean directed){
		CustomGraph graph = new CustomGraph();
		Node n[] = new Node[10];
		for (int i = 0; i < 10; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		for (int i = 1; i < 10; i++) {
			graph.addEdge(UUID.randomUUID().toString(), n[0], n[i]);

		}
		if (!directed){
			GraphProcessor processor = new GraphProcessor();
			processor.makeUndirected(graph);
		} else{
			graph.addType(GraphType.DIRECTED);
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return An undirected graph where there are undirected edges between
	 * one center node and all other nodes
	 */
	public static CustomGraph getUndirectedHubGraph() {
		CustomGraph graph = generateHubGraph(false);
		graph.setName("undirected hub");
		return generateHubGraph(false);
	}

	/**
	 * @return A directed graph where one node is connected to all other nodes
	 */
	public static CustomGraph getDirectedHubGraph() {
		CustomGraph graph = generateHubGraph(true);
		graph.setName("directed hub");
		return generateHubGraph(true);
	}

	/**
	 * @return A directed graph where all nodes are connected to a single node
	 */
	public static CustomGraph getReverseDirectedHubGraph(){
		CustomGraph graph = new CustomGraph();
		Node n[] = new Node[10];
		for (int i = 0; i < 10; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		for (int i = 1; i < 10; i++) {
			graph.addEdge(UUID.randomUUID().toString(),n[i], n[0]);

		}

		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	/**
	 * @return A 3 node clique connected (with a directed edge) to a 3 node directed subgraph
	 */
	public static CustomGraph getCliqueWithOutliersGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("CliqueWithOutliers");
		Node n[] = new Node[6];
		for (int i = 0; i < 6; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);

		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	/**
	 * @return A directed aperiodic graph with two communities
	 */
	public static CustomGraph getDirectedAperiodicTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.directedAperiodicTwoCommunitiesName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[10]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return A simple undirected cycle graph
	 */
	public static CustomGraph getSimpleCycleGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("SimpleCycleGraph");
		Node n[] = new Node[4];
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);




		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return A small clique
	 */
	public static CustomGraph getSimpleClique() {
		CustomGraph graph = new CustomGraph();
		graph.setName("SimpleClique");
		Node n[] = new Node[3];
		for (int i = 0; i < 3; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);

		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return An undirected bipartite graph
	 */
	public static CustomGraph getUndirectedBipartiteGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("UndirectedBipartiteGraph");
		Node n[] = new Node[5];
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);

		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return An undirected graph consisting of a combination
	 * of a cycle and star graph.
	 */
	public static CustomGraph getUndirectedWheelGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("WheelGraph");
		Node n[] = new Node[5];
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Outer Cycle
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
		// Center Node
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);

		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	public static CustomGraph getSimpleTwoComponentsGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.simpleTwoComponentsName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[10]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 *
	 * @return a weighted graph consisting of equal edge weights
	 */
	public static CustomGraph getCompleteGraphWithEqualWeights() {
		CustomGraph graph = new CustomGraph();
		graph.setName("CompleteGraphWithEqualWeights");
		Node[] n = new Node[3];
		// Add nodes
		for (int i = 0; i < 3; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Add edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		// Set weights
		graph.setEdgeWeight(graph.getEdge(0), 5);
		graph.setEdgeWeight(graph.getEdge(1), 5);
		graph.setEdgeWeight(graph.getEdge(2), 5);
		graph.setEdgeWeight(graph.getEdge(3), 5);
		graph.setEdgeWeight(graph.getEdge(4), 5);
		graph.setEdgeWeight(graph.getEdge(5), 5);

		graph.addType(GraphType.WEIGHTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a weighted chain graph with increasing weights
	 */
	public static CustomGraph getChainGraphWithIncreasingWeights() {
		CustomGraph graph = new CustomGraph();
		graph.setName("ChainGraphWithIncreasingWeights");
		Node[] n = new Node[4];
		// Add nodes
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Add edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		// Set weights
		graph.setEdgeWeight(graph.getEdge(0), 1);
		graph.setEdgeWeight(graph.getEdge(1), 1);
		graph.setEdgeWeight(graph.getEdge(2), 2);
		graph.setEdgeWeight(graph.getEdge(3), 2);
		graph.setEdgeWeight(graph.getEdge(4), 3);
		graph.setEdgeWeight(graph.getEdge(5), 3);

		graph.addType(GraphType.WEIGHTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a graph consisting of a cycle that has an edge
	 *  with a weight value that represents an outlier
	 */
	public static CustomGraph getLoopWithOutlierWeight() {
		CustomGraph graph = new CustomGraph();
		graph.setName("LoopWithOutlierWeight");
		Node[] n = new Node[4];
		// Add nodes
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Add edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		// Set weights
		graph.setEdgeWeight(graph.getEdge(0), 1);
		graph.setEdgeWeight(graph.getEdge(1), 1);
		graph.setEdgeWeight(graph.getEdge(2), 1);
		graph.setEdgeWeight(graph.getEdge(3), 1);
		graph.setEdgeWeight(graph.getEdge(4), 1);
		graph.setEdgeWeight(graph.getEdge(5), 1);
		graph.setEdgeWeight(graph.getEdge(6), 50);
		graph.setEdgeWeight(graph.getEdge(7), 50);

		graph.addType(GraphType.WEIGHTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	/**
	 * @return a chain graph where most weights have decimal values
	 */
	public static CustomGraph getChainWithDecimalWeights() {
		CustomGraph graph = new CustomGraph();
		graph.setName("FourNodeChainWithDecimalWeights");
		Node[] n = new Node[4];
		// Add nodes
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Add edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		// Set weights
		graph.setEdgeWeight(graph.getEdge(0), 1.5);
		graph.setEdgeWeight(graph.getEdge(1), 1.5);
		graph.setEdgeWeight(graph.getEdge(2), 2.3);
		graph.setEdgeWeight(graph.getEdge(3), 2.3);
		graph.setEdgeWeight(graph.getEdge(4), 2);
		graph.setEdgeWeight(graph.getEdge(5), 2);

		graph.addType(GraphType.WEIGHTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a graph consisting of a cycle with only negative weights
	 */
	public static CustomGraph getNegativeCycleGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("NegativeCycleGraph");
		Node[] n = new Node[3];
		for (int i = 0; i < 3; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.setEdgeWeight(graph.getEdge(0), -1);
		graph.setEdgeWeight(graph.getEdge(1), -2);
		graph.setEdgeWeight(graph.getEdge(2), -1);

		graph.addType(GraphType.WEIGHTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a graph consisting of a mixture of positive and negative weights
	 */
	public static CustomGraph getMixedWeightsGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("MixedWeightsGraph");
		Node[] n = new Node[4];
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.setEdgeWeight(graph.getEdge(0), 1.5);
		graph.setEdgeWeight(graph.getEdge(1), -2.3);
		graph.setEdgeWeight(graph.getEdge(2), 1);
		graph.setEdgeWeight(graph.getEdge(3), -1);

		graph.addType(GraphType.WEIGHTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a graph consisting of a cycle with only zero weights
	 */
	public static CustomGraph getZeroCycleGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("ZeroWeightCycleGraph");
		Node[] n = new Node[3];
		for (int i = 0; i < 3; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.setEdgeWeight(graph.getEdge(0), 0);
		graph.setEdgeWeight(graph.getEdge(1), 0);
		graph.setEdgeWeight(graph.getEdge(2), 0);

		graph.addType(GraphType.WEIGHTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a graph consisting of a mixture of zero and non-zero weights
	 */
	public static CustomGraph getZeroAndNonZeroWeightMixGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("ZeroAndNonZeroWeightMixGraph");
		Node[] n = new Node[4];
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.setEdgeWeight(graph.getEdge(0), 0);
		graph.setEdgeWeight(graph.getEdge(1), 0);
		graph.setEdgeWeight(graph.getEdge(2), 0);
		graph.setEdgeWeight(graph.getEdge(3), 0);
		graph.setEdgeWeight(graph.getEdge(4), 1);
		graph.setEdgeWeight(graph.getEdge(5), 1);
		graph.setEdgeWeight(graph.getEdge(6), 2.5);
		graph.setEdgeWeight(graph.getEdge(7), 2.5);
		graph.addType(GraphType.WEIGHTED);
		graph.addType(GraphType.ZERO_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return a weighted graph consisting of two communities
	 * connected with a zero weight edge
	 */
	public static CustomGraph getTwoCommunitiesGraphConnectedWithZeroWeightEdge() {
		CustomGraph graph = new CustomGraph();
		graph.setName("TwoCommunitiesConnectedWithZeroWeightEdge");
		Node[] n = new Node[6];
		// Add nodes
		for (int i = 0; i < 6; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Add edges within the first community
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);

		// Add edges within the second community
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);

		// Connect the two communities
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);

		// Set uniform weights for simplicity
		Iterator<Edge> edges = graph.edges().iterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}

		// Set different edge weights on several edges
		graph.setEdgeWeight(graph.getEdge(0), 1.5);
		graph.setEdgeWeight(graph.getEdge(1), 1.5);
		graph.setEdgeWeight(graph.getEdge(2), 6.3);
		graph.setEdgeWeight(graph.getEdge(3), 6.3);
		graph.setEdgeWeight(graph.getEdge(4), 2);
		graph.setEdgeWeight(graph.getEdge(5), 2);
		graph.setEdgeWeight(graph.getEdge(8), 4);
		graph.setEdgeWeight(graph.getEdge(9), 4);
		graph.setEdgeWeight(graph.getEdge(12), 0);
		graph.setEdgeWeight(graph.getEdge(13), 0);

		graph.addType(GraphType.WEIGHTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	/**
	 * @return a graph consisting of a cycle where every node has a self loop
	 */
	public static CustomGraph getAllNodeSelfLoopGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("AllNodeSelfLoopGraph");
		Node[] n = new Node[3];
		for (int i = 0; i < 3; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);

		// self loops
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[2]);


		graph.addType(GraphType.SELF_LOOPS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/**
	 * @return An undirected bipartite graph with self loops
	 */
	public static CustomGraph getBipartiteGraphWithSelfLoops() {
		CustomGraph graph = new CustomGraph();
		graph.setName("BipartiteGraphWithSelfLoops");
		Node n[] = new Node[5];
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);

		// self loops
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[1]);

		graph.addType(GraphType.SELF_LOOPS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}


	/**
	 * @return a weighted graph consisting of two communities
	 */
	public static CustomGraph getTwoCommunitiesWeightedGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("TwoThreeNodeCommunities");
		Node[] n = new Node[6];
		// Add nodes
		for (int i = 0; i < 6; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Add edges within the first community
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);

		// Add edges within the second community
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);

		// Connect the two communities
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);

		// Set uniform weights for simplicity
		Iterator<Edge> edges = graph.edges().iterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}

		// Set different edge weights on several edges
		graph.setEdgeWeight(graph.getEdge(0), 1.5);
		graph.setEdgeWeight(graph.getEdge(1), 1.5);
		graph.setEdgeWeight(graph.getEdge(2), 6.3);
		graph.setEdgeWeight(graph.getEdge(3), 6.3);
		graph.setEdgeWeight(graph.getEdge(4), 2);
		graph.setEdgeWeight(graph.getEdge(5), 2);
		graph.setEdgeWeight(graph.getEdge(8), 4);
		graph.setEdgeWeight(graph.getEdge(9), 4);

		graph.addType(GraphType.WEIGHTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}





	/*
	 * A small test graph given in the original paper that introduces
	 * the link communities algorithm.
	 * Is undirected and unweighted.
	 */
	public static CustomGraph getLinkCommunitiesTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.linkCommunitiesTestName);
		// Creates nodes
		for (int i = 0; i < 9; i++) {
			graph.addNode(Integer.toString(i));
		}
		// Creates edges
		Node n[] = graph.nodes().toArray(Node[]::new);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[8]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.sawmillName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDirectedSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.directedSawmillName);
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSiamDmGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.siamDmUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.siamDmName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getFacebookGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.facebookUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.facebookName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getNewmanClizzGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.newmanClizzGraphWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.newmanClizzName);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.NEWMAN, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getNewmanLinkGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.newmanLinkGraphWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.newmanLinkName);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.NEWMAN, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getMiniServiceTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.miniServiceTestGraphName);
		// Creates nodes
		Node n[] = new Node[5];  
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDolphinsGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new GmlGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.dolphinsGmlInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.dolphinsName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.SELF_LOOPS);
		graph.addType(GraphType.WEIGHTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDocaTestGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.docaTestUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.docaTestGraphName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.SELF_LOOPS);
		graph.addType(GraphType.WEIGHTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	/*public static CustomGraph getContentTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.contentTestName);
		// Creates nodes
		
		Node n[] = new Node[5];  
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode();
			graph.setNodeName(n[i], Integer.toString(i));
		}
				
		graph.setNodeContent(n[0],"John likes movies theatre test case" );
		graph.setNodeContent(n[1], "Marie likes books theatre case");
		graph.setNodeContent(n[2], "John likes movies theatre test case");
		graph.setNodeContent(n[3], "Marie likes movies theatre case");
		graph.setNodeContent(n[4], "node unconnected");
		
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.CONTENT_UNLINKED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}*/
	
	public static CustomGraph getJmolTestGraph() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		
		NodeContentEdgeListGraphInputAdapter adapter = new NodeContentEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.jmolEdgeListInputPath));
		Map<String, String> adapterParam = new HashMap<String, String>();
		adapterParam.put("startDate", "2002-04-14");
		adapterParam.put("endDate", "2003-11-21");
		adapterParam.put("path", "C:\\indexes\\jmol2004");
		adapter.setParameter(adapterParam);
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.jmolName);
		graph.addType(GraphType.CONTENT_LINKED);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSignedGahukuGamaTestGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.gahukuGamaWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.gahukuGamaGraphName);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getLfrGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.lfrUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.lfrGraphName);
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	// @author: YLi
	public static CustomGraph getSignedLfrGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSignedLfrSixNodesGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrSixNodesWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrSixNodesGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSignedLfrBlurredGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrBlurredWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSignedLfrMadeUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrGraphName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSloveneParliamentaryPartyGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.sloveneParliamentaryPartyWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.sloveneParliamentaryPartyGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSloveneParliamentaryPartyDirectedGraph()
			throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.sloveneParliamentaryPartyWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.sloveneParliamentaryPartyGraphName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		processor.makeDirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getWikiElecGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.wikiElecWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.wikiElecGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getWikiElecUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.wikiElecWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.wikiElecGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getEpinionsGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.epinionsWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.epinionsGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getEpinionsUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.epinionsWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.epinionsGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getslashDotGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.slashDotWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.slashDotGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSlashDotUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.slashDotWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.slashDotGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getFiveNodesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.fiveNodesGraphName);
		// Creates nodes
		Node n[] = new Node[5];
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		// Set edge weight
		graph.setEdgeWeight(graph.getEdge(0), 1);
		graph.setEdgeWeight(graph.getEdge(1), -1);
		graph.setEdgeWeight(graph.getEdge(2), 1);
		graph.setEdgeWeight(graph.getEdge(3), -1);
		graph.setEdgeWeight(graph.getEdge(4), 1);
		graph.setEdgeWeight(graph.getEdge(5), 1);
		graph.setEdgeWeight(graph.getEdge(6), 1);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphUndirectedUnweighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Undirected Unweighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphUndirectedWeighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Undirected Weighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
		// Set edge weights
		graph.setEdgeWeight(n[0].getEdgeToward(n[1]), 3.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[0]), 3.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[2]), 2.0);
		graph.setEdgeWeight(n[2].getEdgeToward(n[1]), 2.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[3]), 2.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[1]), 2.0);
		graph.setEdgeWeight(n[2].getEdgeToward(n[3]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[2]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[4]), 1.0);
		graph.setEdgeWeight(n[4].getEdgeToward(n[3]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[5]), 1.0);
		graph.setEdgeWeight(n[5].getEdgeToward(n[3]), 1.0);
		graph.setEdgeWeight(n[4].getEdgeToward(n[6]), 4.0);
		graph.setEdgeWeight(n[6].getEdgeToward(n[4]), 4.0);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		graph.addType(GraphType.WEIGHTED);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphDirectedUnweighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Directed Unweighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		graph.addType(GraphType.DIRECTED);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphDirectedWeighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Directed Weighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		// Set edge weights
		graph.setEdgeWeight(n[0].getEdgeToward(n[1]), 3.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[2]), 2.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[3]), 2.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[2]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[4]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[5]), 1.0);
		graph.setEdgeWeight(n[4].getEdgeToward(n[6]), 4.0);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.WEIGHTED);
		return graph;
	}
	
	/*
	 * Graph taken from the paper "The worst-case time complexity for generating 
	 * all maximal cliques and computational experiments" from Tomita et al. 
	 */
	public static CustomGraph getMaximalCliqueGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph for Maximal Clique Search");
		
		// Create nodes
		Node n[] = new Node[9];
		for (int i = 0; i < 9; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[2]);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	/*
	 * Returns undirected karate graph, used in OCDA accuracy tests
	 */
	public static CustomGraph getUndirectedKarateGraph(){
		CustomGraph graph = new CustomGraph();
		try {
			GraphInputAdapter inputAdapter = new GmlGraphInputAdapter();
			inputAdapter.setReader(new FileReader(OcdTestConstants.zacharyGmlInputPath));
			graph = inputAdapter.readGraph();
			GraphProcessor graphProcessor = new GraphProcessor();
			graphProcessor.makeUndirected(graph);

		}catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}
	
	public static CustomGraph getLinkgraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.LinkGraphName);
		// Creates nodes
		Node n[] = new Node[8];  
		for (int i = 0; i < 8; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[6]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getModularityTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.ModularityTestGraphName);
		// Creates nodes
		Node n[] = new Node[4];  
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);

	
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
}
