package i5.las2peer.services.servicePackage.graph;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;

@Entity
public class CustomGraph extends Graph2D {
	
	/////////////////// DATABASE COLUMN NAMES
	
	/*
	 * Database column name definitions.
	 */
	protected static final String idColumnName = "ID";
	private static final String idJoinColumnName = "GRAPH_ID";
	private static final String userColumnName = "USER_NAME";
	private static final String nameColumnName = "NAME";
	private static final String descriptionColumnName = "DESCRIPTION";
	private static final String lastUpdateColumnName = "LAST_UPDATE";
	private static final String idEdgeMapColumnName = "id";
	private static final String idNodeMapColumnName = "id";
	private static final String idEdgeMapKeyColumnName = "RUNTIME_ID";
	private static final String idNodeMapKeyColumnName = "RUNTIME_ID";
	/////////////////////////// ATTRIBUTES
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private long id;
	/**
	 * The name of the user owning the graph.
	 */
	@Column(name = userColumnName)
	private String userName = "";
	/**
	 * The name of the graph.
	 */
	@Column(name = nameColumnName)
	private String name = "";
	/**
	 * The name of the graph.
	 */
	@Column(name = descriptionColumnName)
	private String description = "";
	/**
	 * Last time of modification.
	 */
	@Version
	@Column(name = lastUpdateColumnName)
	private Timestamp lastUpdate;
	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<GraphType> types = new HashSet<GraphType>();
	
	///////////////////// THE FOLLOWING ATTRIBUTES ARE MAINTAINED AUTOMATICALLY AND ONLY OF INTERNAL USE
	
	/*
	 * Mapping from fix node ids to custom nodes for additional node data and persistence.
	 */
	@OneToMany(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name=idJoinColumnName, referencedColumnName = idNodeMapColumnName)
	@MapKeyColumn(name = idNodeMapKeyColumnName)
	private Map<Integer, CustomNode> customNodes = new HashMap<Integer, CustomNode>();
	/*
	 * Mapping from fix edge ids to custom nodes for additional edge data and persistence.
	 */
	@OneToMany(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name=idJoinColumnName, referencedColumnName = idEdgeMapColumnName)
	@MapKeyColumn(name = idEdgeMapKeyColumnName)
	private Map<Integer, CustomEdge> customEdges = new HashMap<Integer, CustomEdge>();
	/*
	 * Mapping from edges to fix edge ids.
	 */
	@Transient
	private Map<Edge, Integer> edgeIds = new HashMap<Edge, Integer>();
	/*
	 * Mapping from nodes to fix node ids.
	 */
	@Transient
	private Map<Node, Integer> nodeIds = new HashMap<Node, Integer>();
	/*
	 * A counter for assigning runtime edge indices. 
	 */
	@Transient
	private int edgeCounter = 0;
	/*
	 * A counter for assigning runtime node indices.
	 */
	@Transient
	private int nodeCounter = 0;
	
	///////////////////////////// METHODS AND CONSTRUCTORS
	
	public CustomGraph() {
		this.addGraphListener(new CustomGraphListener());
	}
	
	public CustomGraph(Graph2D graph) {
		super(graph);
		NodeCursor nodes = this.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			this.addCustomNode(node);
			nodes.next();
		}
		nodeCounter = this.nodeCount();
		EdgeCursor edges = this.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			this.addCustomEdge(edge);
			edges.next();
		}
		edgeCounter = this.edgeCount();
		this.addGraphListener(new CustomGraphListener());
		
	}
	
	public CustomGraph(CustomGraph graph) {
		super(graph);
		this.nodeIds = new HashMap<Node, Integer>(graph.nodeIds);
		this.edgeIds = new HashMap<Edge, Integer>(graph.edgeIds);
		this.customNodes = new HashMap<Integer, CustomNode>(graph.customNodes);
		this.customEdges = new HashMap<Integer, CustomEdge>(graph.customEdges);
		this.userName = new String(graph.userName);
		this.name = new String(graph.name);
		this.id = graph.id;
		this.description = new String(graph.description);
		this.lastUpdate.setTime(graph.lastUpdate.getTime());
		nodeCounter = graph.nodeCounter;
		edgeCounter = graph.edgeCounter;
		this.addGraphListener(new CustomGraphListener());
		this.types = new HashSet<GraphType>(graph.types);
	}

	public long getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String user) {
		this.userName = user;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isOfType(GraphType type) {
		return this.types.contains(type);
	}
	
	public void addType(GraphType type) {
		this.types.add(type);
	}
	
	public void removeType(GraphType type) {
		this.types.remove(type);
	}

	public double getEdgeWeight(Edge edge) {
		
		return getCustomEdge(edge).getWeight();
	}
	
	public void setEdgeWeight(Edge edge, double weight) {
		getCustomEdge(edge).setWeight(weight);
	}
	
	public String getNodeName(Node node) {
		return getCustomNode(node).getName();
	}
	
	public void setNodeName(Node node, String name) {
		getCustomNode(node).setName(name);
	}

	public double getWeightedInDegree(Node node) {
		double inDegree = 0;
		EdgeCursor inEdges = node.inEdges();
		while(inEdges.ok()) {
			Edge edge = inEdges.edge();
			inDegree += getCustomEdge(edge).getWeight();
			inEdges.next();
		}
		return inDegree;
	}
	
	public double getWeightedOutDegree(Node node) {
		double outDegree = 0;
		EdgeCursor outEdges = node.outEdges();
		while(outEdges.ok()) {
			Edge edge = outEdges.edge();
			outDegree += getCustomEdge(edge).getWeight();
			outEdges.next();
		}
		return outDegree;
	}
	
	public double getWeightedNodeDegree(Node node) {
		double degree = 0;
		EdgeCursor edges = node.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			degree += getCustomEdge(edge).getWeight();
			edges.next();
		}
		return degree;
	}
	
	/**
	 * Returns the maximum edge weight.
	 * @return The maximum edge weight.
	 */
	public double getMaxEdgeWeight() {
		double maxWeight = 0;
		if(!edgeIds.isEmpty()) {
			maxWeight = Collections.max(edgeIds.values());
		}
		return maxWeight;		
	}
	
	/**
	 * Returns the smallest edge weight greater 0.
	 * @return The smallest edge weight.
	 */
	public double getMinEdgeWeight() {
		double minWeight = Double.POSITIVE_INFINITY;
		for(double edgeWeight : edgeIds.values()) {
			if(edgeWeight < minWeight && edgeWeight > 0) {
				minWeight = edgeWeight;
			}
		}
		if(minWeight == Double.POSITIVE_INFINITY) {
			minWeight = 0;
		}
		return minWeight;
	}
	
	////////////////// THE FOLLOWING METHODS ARE ONLY OF INTERNAL PACKAGE USE
	
	/*
	 * Returns the custom edge object corresponding to an edge.
	 * @param edge An edge which must belong to this graph.
	 * @return The corresponding custom edge object.
	 */
	protected CustomEdge getCustomEdge(Edge edge) {
		int index = edgeIds.get(edge);
		return customEdges.get(index);	
	}
	/*
	 * Returns the custom node object corresponding to a node.
	 * @param node A node which must belong to this graph.
	 * @return The corresponding custom node object.
	 */
	protected CustomNode getCustomNode(Node node) {
		int index = nodeIds.get(node);
		return customNodes.get(index);	
	}
	/*
	 * Creates a new custom node object and maps the node to it.
	 * @param node The node.
	 */
	protected void addCustomNode(Node node) {
		CustomNode customNode = new CustomNode();
		this.nodeIds.put(node, this.nodeCounter);
		this.customNodes.put(nodeCounter, customNode);
		nodeCounter++;
	}
	/*
	 * Removes the mapping from a node to its custom node object.
	 * @param node
	 */
	protected void removeCustomNode(Node node) {
		int id = this.nodeIds.get(node);
		this.nodeIds.remove(node);
		this.customNodes.remove(id);
	}
	/*
	 * Creates a new custom edge object and maps the edge to it.
	 * @param edge The edge.
	 */
	protected void addCustomEdge(Edge edge) {
		CustomEdge customEdge = new CustomEdge();
		this.edgeIds.put(edge, this.edgeCounter);
		this.customEdges.put(edgeCounter, customEdge);
		edgeCounter++;
	}
	/*
	 * Removes the mapping from an edge to its custom edge.
	 * @param edge
	 */
	protected void removeCustomEdge(Edge edge) {
		int id = this.edgeIds.get(edge);
		this.edgeIds.remove(edge);
		this.customEdges.remove(id);
	}
	/*
	 * PostLoad Method.
	 * Creates node and edge objects from the custom nodes and edges and sets the mappings between the two.
	 * The mapping indices are reset to omit rising numbers due to deletions and reinsertions.
	 */
	@PostLoad
	private void postLoad() {
		List<CustomNode> nodes = new ArrayList<CustomNode>(this.customNodes.values());
		Map<CustomNode, Node> reverseNodeMap = new HashMap<CustomNode, Node>();
		this.nodeIds.clear();
		this.customNodes.clear();
		for(CustomNode customNode : nodes) {
			Node node = customNode.createNode(this);
			this.nodeIds.put(node, node.index());
			this.customNodes.put(node.index(), customNode);
			reverseNodeMap.put(customNode, node);
		}
		List<CustomEdge> edges = new ArrayList<CustomEdge>(this.customEdges.values());
		this.edgeIds.clear();
		this.customEdges.clear();
		for(CustomEdge customEdge : edges) {
			Edge edge = customEdge.createEdge(this, reverseNodeMap.get(customEdge.getSource()), reverseNodeMap.get(customEdge.getTarget()));
			this.edgeIds.put(edge, edge.index());
			this.customEdges.put(edge.index(), customEdge);
		}
		nodeCounter = this.nodeCount();
		edgeCounter = this.edgeCount();
		this.addGraphListener(new CustomGraphListener());
	}
	
	@PrePersist
	@PreUpdate
	/*
	 * PrePersist Method.
	 * Writes the attributes of nodes and edges into their corresponding custom nodes and edges.
	 */
	private void prePersist() {
		NodeCursor nodes = this.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			this.getCustomNode(node).update(this, node);
			nodes.next();
		}
		EdgeCursor edges = this.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			this.getCustomEdge(edge).update(this, edge);
			edges.next();
		}
	}

	
	
}
