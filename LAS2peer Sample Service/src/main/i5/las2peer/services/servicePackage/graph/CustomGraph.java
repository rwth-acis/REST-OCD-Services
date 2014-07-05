package i5.las2peer.services.servicePackage.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;

@Entity
public class CustomGraph extends Graph2D {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Id
	private String user;
	
	@ElementCollection
	private List<NodeEntity> nodes;
	
	@ElementCollection
	private List<EdgeEntity> edges;
	
	private Map<Edge, Double> edgeWeights;
	private Map<Node, String> nodeNames;
	
	public CustomGraph() {
		this.nodeNames = new HashMap<Node, String>();
		this.edgeWeights = new HashMap<Edge, Double>();
		this.nodes = null;
		this.edges = null;
		this.user = "";
	}
	
	public CustomGraph(Graph2D graph, Map<Edge, Double> edgeWeights, Map<Node, String> nodeNames) {
		super(graph);
		setEdgeWeights(edgeWeights);
		setNodeNames(nodeNames);
		this.nodes = null;
		this.edges = null;
		this.user = "";
	}
	
	public CustomGraph(CustomGraph graph) {
		super(graph);
		this.nodeNames = graph.getNodeNames();
		this.edgeWeights = graph.getEdgeWeights();
		this.nodes = graph.nodes;
		this.edges = graph.edges;
		this.user = graph.getUser();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Map<Edge, Double> getEdgeWeights() {
		return edgeWeights;
	}

	public void setEdgeWeights(Map<Edge, Double> edgeWeights) {
		boolean containsEdges = true;
		for(Edge edge : edgeWeights.keySet()) {
			if(!this.contains(edge)) {
				containsEdges = false;
				break;
			}
		}
		if(containsEdges) {
			this.edgeWeights = edgeWeights;
		}
	}

	public Map<Node, String> getNodeNames() {
		return nodeNames;
	}

	public void setNodeNames(Map<Node, String> nodeNames) {
		boolean containsNodes = true;
		for(Map.Entry<Node, String> entry : nodeNames.entrySet()) {
			if(!this.contains(entry.getKey())) {
				containsNodes = false;
				break;
			}
			if(entry.getValue() == null) {
				entry.setValue("");
			}
		}
		if(containsNodes) {
			this.nodeNames = nodeNames;
		}
	}

	public double getEdgeWeight(Edge edge) {
		return edgeWeights.get(edge);
	}
	
	public void setEdgeWeight(Edge edge, double weight) {
		if(this.contains(edge)) {
			edgeWeights.put(edge, weight);
		}
	}
	
	public String getNodeName(Node node) {
		return nodeNames.get(node);
	}
	
	public void setNodeName(Node node, String name) {
		if(this.contains(node)) {
			if(name == null) {
				this.nodeNames.put(node, "");
			}
			else {
				this.nodeNames.put(node, name);
			}
		}
	}
	
	public double getWeightedInDegree(Node node) {
		double inDegree = 0;
		if(this.contains(node)) {
			EdgeCursor inEdges = node.inEdges();
			while(inEdges.ok()) {
				Edge edge = inEdges.edge();
				inDegree += edgeWeights.get(edge);
				inEdges.next();
			}
		}
		return inDegree;
	}
	
	@PostLoad
	private void postLoad() {
		for(int i=0; i<nodes.size(); i++) {
			nodes.get(i).createNode(this);
		}
		for(int i=0; i<edges.size(); i++) {
			EdgeEntity edgeEntity= edges.get(i);
			Node[] nodeArray = this.getNodeArray();
			Node source = nodeArray[edgeEntity.getSource().getIndex()];
			Node target = nodeArray[edgeEntity.getTarget().getIndex()];
			edgeEntity.createEdge(this, source, target);
		}
		this.nodes = null;
		this.edges = null;
	}

	@PrePersist
	@PreUpdate
	private void prePersist() {
		NodeCursor nodeIt = this.nodes();
		while(nodeIt.ok()) {
			Node node = nodeIt.node();
			nodes.add(new NodeEntity(this, node));
			nodeIt.next();
		}
		EdgeCursor edgeIt = this.edges();
		while(edgeIt.ok()) {
			Edge edge = edgeIt.edge();
			NodeEntity source = nodes.get(edge.source().index());
			NodeEntity target = nodes.get(edge.target().index());
			edges.add(new EdgeEntity(this, edge, source, target));
			edgeIt.next();
		}
	}
	
	@PostPersist
	@PostUpdate
	private void postPersist() {
		this.nodes = null;
		this.edges = null;
	}
}
