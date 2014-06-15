package i5.las2peer.services.servicePackage.testsUtil;

import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graphInputAdapters.GraphInputAdapter;
import i5.las2peer.services.servicePackage.graphInputAdapters.GraphInputAdapterFactory;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

public class TestGraphFactory {
	
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
		graph.createEdge(n[8], n[9]);
		graph.createEdge(n[9], n[8]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		return graph;
	}
	
	public static CustomGraph getSawmillGraph() {
		GraphInputAdapterFactory factory = GraphInputAdapterFactory.getFactory();
		GraphInputAdapter adapter =
				factory.getEdgeListUndirectedGraphInputAdapter(TestConstants.getSawmillTxtInputFileName());
		return adapter.readGraph();
	}	
}
