package i5.las2peer.services.servicePackage.testsUtil;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.NodeWeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.evaluation.EvaluationConstants;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;

import java.io.FileNotFoundException;
import java.io.FileReader;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

public class OcdTestGraphFactory {
	
	public static CustomGraph getTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
		}
		// Creates edges
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[0], n[3]);
		graph.createEdge(n[0], n[4]);
		graph.createEdge(n[0], n[10]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[5], n[10]);
		graph.createEdge(n[1], n[0]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[3], n[0]);
		graph.createEdge(n[4], n[0]);
		graph.createEdge(n[10], n[0]);
		graph.createEdge(n[6], n[5]);
		graph.createEdge(n[7], n[5]);
		graph.createEdge(n[8], n[5]);
		graph.createEdge(n[9], n[5]);
		graph.createEdge(n[10], n[5]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		return graph;
	}
	
	public static CustomGraph getAperiodicTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
		}
		// Creates edges
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[0], n[3]);
		graph.createEdge(n[0], n[4]);
		graph.createEdge(n[0], n[10]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[5], n[10]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[1], n[10]);
		graph.createEdge(n[4], n[10]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getDirectedAperiodicTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
		}
		// Creates edges
		graph.createEdge(n[1], n[0]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[3], n[0]);
		graph.createEdge(n[4], n[0]);
		graph.createEdge(n[10], n[0]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[5], n[10]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[1], n[10]);
		graph.createEdge(n[4], n[10]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		return graph;
	}
	
	public static CustomGraph getSimpleTwoComponentsGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
		}
		// Creates edges
		graph.createEdge(n[1], n[0]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[3], n[0]);
		graph.createEdge(n[4], n[0]);
		graph.createEdge(n[10], n[0]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[1], n[10]);
		graph.createEdge(n[4], n[10]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
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
		// Creates nodes
		for (int i = 0; i < 9; i++) {
			graph.createNode();
		}
		// Creates edges
		Node n[] = graph.getNodeArray();
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[0], n[3]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[1], n[3]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[3], n[5]);
		graph.createEdge(n[3], n[6]);
		graph.createEdge(n[4], n[5]);
		graph.createEdge(n[6], n[7]);
		graph.createEdge(n[6], n[8]);
		graph.createEdge(n[7], n[8]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getDirectedSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		return adapter.readGraph();
	}
	
	public static CustomGraph getSiamDmGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.siamDmUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
}
