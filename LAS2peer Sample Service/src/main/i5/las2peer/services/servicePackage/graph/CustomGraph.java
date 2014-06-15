package i5.las2peer.services.servicePackage.graph;

import java.util.HashMap;
import java.util.Map;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.view.Graph2D;

public class CustomGraph extends Graph2D {
	
	private Map<Edge, Double> edgeWeights;
	private Map<Node, String> nodeNames;
	
	public CustomGraph() {
		this.nodeNames = new HashMap<Node, String>();
		this.edgeWeights = new HashMap<Edge, Double>();
	}
	
	public CustomGraph(Graph2D graph, Map<Edge, Double> edgeWeights, Map<Node, String> nodeNames) {
		super(graph);
		this.edgeWeights = edgeWeights;
		this.nodeNames = nodeNames;
	}
	
	public CustomGraph(CustomGraph graph) {
		super(graph);
		this.nodeNames = graph.getNodeNames();
		this.edgeWeights = graph.getEdgeWeights();
	}
	
	public Map<Edge, Double> getEdgeWeights() {
		return edgeWeights;
	}

	public void setEdgeWeights(Map<Edge, Double> edgeWeights) {
		this.edgeWeights = edgeWeights;
	}

	public Map<Node, String> getNodeNames() {
		return nodeNames;
	}

	public void setNodeNames(Map<Node, String> nodeNames) {
		this.nodeNames = nodeNames;
	}

	public double getEdgeWeight(Edge edge) {
		return edgeWeights.get(edge);
	}
	
	public void setEdgeWeight(Edge edge, double weight) {
		edgeWeights.put(edge, weight);
	}
	
	public String getNodeName(Node node) {
		return nodeNames.get(node);
	}
	
	public void setNodeName(Node node, String name) {
		this.nodeNames.put(node, name);
	}
	
	public double getWeightedInDegree(Node node) {
		double inDegree = 0;
		EdgeCursor inEdges = node.inEdges();
		while(inEdges.ok()) {
			Edge edge = inEdges.edge();
			inDegree += edgeWeights.get(edge);
			inEdges.next();
		}
		return inDegree;
	}

}
