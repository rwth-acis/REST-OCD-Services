package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.benchmarks.GraphCreationLog;
import i5.las2peer.services.ocd.benchmarks.GraphCreationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;

/**
 * Represents a graph (or network), i.e. the node / edge structure and additional meta information.
 * @author Sebastian
 *
 */
@Entity
@IdClass(CustomGraphId.class)
public class CustomGraph extends Graph2D {
	
	/////////////////// DATABASE COLUMN NAMES
	
	/*
	 * Database column name definitions.
	 */
	protected static final String idColumnName = "ID";
	protected static final String userColumnName = "USER_NAME";
	private static final String nameColumnName = "NAME";
//	private static final String descriptionColumnName = "DESCRIPTION";
//	private static final String lastUpdateColumnName = "LAST_UPDATE";
	private static final String idEdgeMapKeyColumnName = "RUNTIME_ID";
	private static final String idNodeMapKeyColumnName = "RUNTIME_ID";
	private static final String creationMethodColumnName = "CREATION_METHOD";
	
	/*
	 * Field name definitions for JPQL queries.
	 */
	public static final String USER_NAME_FIELD_NAME = "userName";
	public static final String ID_FIELD_NAME = "id";
	public static final String CREATION_METHOD_FIELD_NAME = "creationMethod";
	
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
	@Id
	@Column(name = userColumnName)
	private String userName = "";
	
	/**
	 * The name of the graph.
	 */
	@Column(name = nameColumnName)
	private String name = "";
	
//	/**
//	 * The description of the graph.
//	 */
//	@Column(name = descriptionColumnName)
//	private String description = "";
//	/**
//	 * Last time of modification.
//	 */
//	@Version
//	@Column(name = lastUpdateColumnName)
//	private Timestamp lastUpdate;
	
	/**
	 * The graph's types.
	 */
	@ElementCollection
	private Set<Integer> types = new HashSet<Integer>();
	
	/**
	 * The log for the benchmark model the graph was created by.
	 */
	@OneToOne(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name = creationMethodColumnName)
	private GraphCreationLog creationMethod = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());

	/**
	 * The covers based on this graph.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade={CascadeType.ALL} /*, fetch=FetchType.LAZY */)
	private List<Cover> covers = new ArrayList<Cover>();
	
	///////////////////// THE FOLLOWING ATTRIBUTES ARE MAINTAINED AUTOMATICALLY AND ONLY OF INTERNAL / PERSISTENCE USE
	
	/*
	 * Mapping from fix node ids to custom nodes for additional node data and persistence.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade={CascadeType.ALL} /*, fetch=FetchType.LAZY */)
	@MapKeyColumn(name = idNodeMapKeyColumnName)
	private Map<Integer, CustomNode> customNodes = new HashMap<Integer, CustomNode>();
	/*
	 * Mapping from fix edge ids to custom nodes for additional edge data and persistence.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade={CascadeType.ALL} /*, fetch=FetchType.LAZY */)
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
	 * Mapping from custom nodes to nodes.
	 */
	@Transient
	private Map<CustomNode, Node> reverseNodeMap = new HashMap<CustomNode, Node>();
	/*
	 * Used for assigning runtime edge indices. 
	 */
	@Transient
	private int edgeIndexer = 0;
	/*
	 * Used for assigning runtime node indices.
	 */
	@Transient
	private int nodeIndexer = 0;
	
	///////////////////////////// METHODS AND CONSTRUCTORS
	
	/**
	 * Creates a new instance.
	 */
	public CustomGraph() {
		this.addGraphListener(new CustomGraphListener());
	}

	/**
	 * Copy constructor.
	 * @param graph The graph to copy.
	 */
	public CustomGraph(Graph2D graph) {
		super(graph);
		NodeCursor nodes = this.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			this.addCustomNode(node);
			nodes.next();
		}
		EdgeCursor edges = this.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			this.addCustomEdge(edge);
			edges.next();
		}
		Iterator<?> listenerIt = this.getGraphListeners();
		while(listenerIt.hasNext()) {
			this.removeGraphListener((GraphListener)listenerIt.next());
			listenerIt.remove();
		}
		this.addGraphListener(new CustomGraphListener());
	}
	
	/**
	 * Copy constructor.
	 * @param graph The graph to copy.
	 */
	public CustomGraph(CustomGraph graph) {
		super(graph);
		this.creationMethod = new GraphCreationLog(graph.creationMethod.getType(), graph.creationMethod.getParameters());
		this.creationMethod.setStatus(graph.creationMethod.getStatus());
		this.customNodes = new HashMap<Integer, CustomNode>();
		copyMappings(graph.customNodes, graph.customEdges, graph.nodeIds, graph.edgeIds);
		this.userName = new String(graph.userName);
		this.name = new String(graph.name);
		this.id = graph.id;
//		this.description = new String(graph.description);
//		if(graph.lastUpdate != null) {
//			this.lastUpdate = new Timestamp(graph.lastUpdate.getTime());
//		}
		nodeIndexer = graph.nodeIndexer;
		edgeIndexer = graph.edgeIndexer;
		this.types = new HashSet<Integer>(graph.types);
	}
	
	/**
	 * Sets all the structural information to that of another graph.
	 * This includes the structure of the nodes and edges, their custom information
	 * and the graph types.
	 * @param graph The graph to obtain data from.
	 */
	public void setStructureFrom(CustomGraph graph) {
		NodeCursor nodes = this.nodes();
		Node node;
		/*
		 * Removes all nodes and edges
		 * including their custom information.
		 */
		while(nodes.ok()) {
			node = nodes.node();
			this.removeNode(node);
			nodes.next();
		}
		/*
		 * Adds new nodes and edges.
		 */
		this.nodeIds.clear();
		this.customNodes.clear();
		this.edgeIds.clear();
		this.nodeIds.clear();
		this.edgeIndexer = 0;
		this.nodeIndexer = 0;
		this.reverseNodeMap.clear();
		this.types.clear();
		nodes = graph.nodes();
		while(nodes.ok()) {
			node = this.createNode();
			this.setNodeName(node, graph.getNodeName(nodes.node()));
			nodes.next();
		}
		Node[] nodeArr = this.getNodeArray();
		EdgeCursor edges = graph.edges();
		Edge edge;
		Edge refEdge;
		while(edges.ok()) {
			refEdge = edges.edge();
			edge = this.createEdge(nodeArr[refEdge.source().index()], nodeArr[refEdge.target().index()]);
			this.setEdgeWeight(edge, graph.getEdgeWeight(refEdge));
			edges.next();
		}
		/*
		 * Updates graph types.
		 */
		for(GraphType type : graph.getTypes()) {
			this.addType(type);
		}
	}

	/**
	 * Getter for the id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Getter for the user name.
	 * @return The name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Setter for the user name.
	 * @param user The name.
	 */
	public void setUserName(String user) {
		this.userName = user;
	}
	
	/**
	 * Getter for the graph name.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the graph name.
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

//	public String getDescription() {
//		return description;
//	}
//	
//	public Timestamp getLastUpdate() {
//		return lastUpdate;
//	}
//
//	public void setDescription(String description) {
//		this.description = description;
//	}
	
	/**
	 * Setter for the creation method.
	 * @param creationMethod The creation method.
	 */
	public void setCreationMethod(GraphCreationLog creationMethod) {
		this.creationMethod = creationMethod;
	}
	
	/**
	 * Getter for the creation method.
	 * @return The creation method.
	 */
	public GraphCreationLog getCreationMethod() {
		return this.creationMethod;
	}
	
	/**
	 * States whether the graph is of a certain type.
	 * @param type The graph type.
	 * @return TRUE if the graph is of the type, otherwise FALSE.
	 */
	public boolean isOfType(GraphType type) {
		return this.types.contains(type.getId());
	}
	
	/**
	 * Adds a graph type to the graph.
	 * @param type The graph type.
	 */
	public void addType(GraphType type) {
		this.types.add(type.getId());
	}
	
	/**
	 * Removes a graph type from the graph.
	 * @param type The graph type.
	 */
	public void removeType(GraphType type) {
		this.types.remove(type.getId());
	}
	
	/**
	 * Removes all graph types from the graph.
	 */
	public void clearTypes() {
		this.types.clear();
	}
	
	/**
	 * Getter for the graph types.
	 * @return The types.
	 */
	public Set<GraphType> getTypes() {
		Set<GraphType> types = new HashSet<GraphType>();
		for(int id : this.types) {
			types.add(GraphType.lookupType(id));
		}
		return types;
	}

	/**
	 * Getter for the edge weight of a certain edge.
	 * @param edge The edge.
	 * @return The edge weight.
	 */
	public double getEdgeWeight(Edge edge) {
		return getCustomEdge(edge).getWeight();
	}
	
	/**
	 * Setter for the edge weight of a certain edge.
	 * @param edge The edge.
	 * @param weight The edge weight.
	 */
	public void setEdgeWeight(Edge edge, double weight) {
		getCustomEdge(edge).setWeight(weight);
	}
	
//	public long getEdgeId(Edge edge) {
//		return getCustomEdge(edge).getId();
//	}
	
	/**
	 * Getter for the node name of a certain node.
	 * @param node The node.
	 * @return The node name.
	 */
	public String getNodeName(Node node) {
		return getCustomNode(node).getName();
	}
	
	/**
	 * Setter for the node name of a certain node.
	 * @param node The node.
	 * @param name The node name.
	 */
	public void setNodeName(Node node, String name) {
		getCustomNode(node).setName(name);
	}

//	public int getNodeId(Node node) {
//		return getCustomNode(node).getId();
//	}
	
	/**
	 * Returns weighted in-degree, i.e. the sum of the weights of all incoming edges of a node.
	 * @param node The node.
	 * @return The weighted in-degree.
	 */
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
	
	/**
	 * Returns the weighted out-degree, i.e. the sum of the weights of all outgoing edges of a node.
	 * @param node The node.
	 * @return The weighted out-degree.
	 */
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
	
	/**
	 * Returns the weighted node degree, i.e. the sum of the weights of all incident edges of a node.
	 * @param node The node.
	 * @return The weighted degree.
	 */
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
	 * Returns the maximum edge weight of the graph.
	 * @return The maximum edge weight or negative infinity, if there are no edges in the graph.
	 */
	public double getMaxEdgeWeight() {
		double maxWeight = Double.NEGATIVE_INFINITY;
		double edgeWeight;
		EdgeCursor edges = this.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			edgeWeight = getCustomEdge(edge).getWeight();
			if(edgeWeight > maxWeight) {
				maxWeight = edgeWeight;
			}
			edges.next();
		}
		return maxWeight;
	}
	
	/**
	 * Returns the minimum edge weight of the graph.
	 * @return The minimum edge weight or positive infinity, if there are no edges in the graph.
	 */
	public double getMinEdgeWeight() {
		double minWeight = Double.POSITIVE_INFINITY;
		double edgeWeight;
		EdgeCursor edges = this.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			edgeWeight = getCustomEdge(edge).getWeight();
			if(edgeWeight < minWeight) {
				minWeight = edgeWeight;
			}
			edges.next();
		}
		return minWeight;
	}
	
	/**
	 * Returns the minimum weighted in-degree of the graph.
	 * @return The weighted in-degree or positive infinity if the graph does not contain any nodes.
	 */
	public double getMinWeightedInDegree() {
		double minDegree = Double.POSITIVE_INFINITY;
		NodeCursor nodes = this.nodes();
		double curDegree;
		while(nodes.ok()) {
			curDegree = getWeightedInDegree(nodes.node());
			if(curDegree < minDegree) {
				minDegree = curDegree;
			}
			nodes.next();
		}
		return minDegree;
	}
	
	/**
	 * Returns the maximum weighted in-degree of the graph.
	 * @return The weighted in-degree or negative infinity if the graph does not contain any nodes.
	 */
	public double getMaxWeightedInDegree() {
		double maxDegree = Double.NEGATIVE_INFINITY;
		NodeCursor nodes = this.nodes();
		double curDegree;
		while(nodes.ok()) {
			curDegree = getWeightedInDegree(nodes.node());
			if(curDegree > maxDegree) {
				maxDegree = curDegree;
			}
			nodes.next();
		}
		return maxDegree;
	}
	
	////////////////// THE FOLLOWING METHODS ARE ONLY OF INTERNAL PACKAGE USE AND FOR PERSISTENCE PURPOSES
	/*
	 * Initializes all node and edge mappings for the copy constructor.
	 * @param customNodes The custom node mapping of the copied custom graph.
	 * @param customEdges The custom edge mapping of the copied custom graph.
	 * @param nodeIds The node id mapping of the copied custom graph.
	 * @param edgeIds The edge id mapping of the copied custom graph.
	 */
	protected void copyMappings(Map<Integer, CustomNode> customNodes, Map<Integer, CustomEdge> customEdges,
			Map<Node, Integer> nodeIds, Map<Edge, Integer> edgeIds) {
		for(Map.Entry<Integer, CustomNode> entry : customNodes.entrySet()) {
			this.customNodes.put(entry.getKey(), new CustomNode(entry.getValue()));
		}
		for(Map.Entry<Integer, CustomEdge> entry : customEdges.entrySet()) {
			this.customEdges.put(entry.getKey(), new CustomEdge(entry.getValue()));
		}
		Node[] nodeArr = this.getNodeArray();
		for(Map.Entry<Node, Integer> entry : nodeIds.entrySet()) {
			this.nodeIds.put(nodeArr[entry.getKey().index()], entry.getValue());
		}
		NodeCursor nodes = this.nodes();
		while(nodes.ok()) {
			this.reverseNodeMap.put(this.getCustomNode(nodes.node()), nodes.node());
			nodes.next();
		}
		Edge [] edgeArr = this.getEdgeArray();
		for(Map.Entry<Edge, Integer> entry : edgeIds.entrySet()) {
			this.edgeIds.put(edgeArr[entry.getKey().index()], entry.getValue());
		}
	}
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
	 * Returns the node object corresponding to a custom node.
	 * @param customNode A customNode which must belong to this graph.
	 * @return The corresponding node object.
	 */
	protected Node getNode(CustomNode customNode) {
		return reverseNodeMap.get(customNode);
	}
	/*
	 * Creates a new custom node object and maps the node to it.
	 * @param node The node.
	 */
	protected void addCustomNode(Node node) {
		CustomNode customNode = new CustomNode();
		this.nodeIds.put(node, this.nodeIndexer);
		this.customNodes.put(nodeIndexer, customNode);
		this.reverseNodeMap.put(customNode,  node);
		nodeIndexer++;
	}
	/*
	 * Removes the mappings between a node and its custom node object.
	 * @param node
	 */
	protected void removeCustomNode(Node node) {
		CustomNode customNode = this.getCustomNode(node);
		int id = this.nodeIds.get(node);
		this.nodeIds.remove(node);
		this.customNodes.remove(id);
		this.reverseNodeMap.remove(customNode);
	}
	/*
	 * Creates a new custom edge object and maps the edge to it.
	 * @param edge The edge.
	 */
	protected void addCustomEdge(Edge edge) {
		CustomEdge customEdge = new CustomEdge();
		this.edgeIds.put(edge, this.edgeIndexer);
		this.customEdges.put(edgeIndexer, customEdge);
		edgeIndexer++;
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
	
	/////////////////////////// PERSISTENCE CALLBACK METHODS
	
	/*
	 * PostLoad Method.
	 * Creates node and edge objects from the custom nodes and edges and sets the mappings between the two.
	 * The mapping indices are reset to omit rising numbers due to deletions and reinsertions.
	 */
	@PostLoad
	private void postLoad() {
		List<CustomNode> nodes = new ArrayList<CustomNode>(this.customNodes.values());
		this.nodeIds.clear();
		this.customNodes.clear();
		for(CustomNode customNode : nodes) {
			Node node = customNode.createNode(this);
			this.nodeIds.put(node, node.index());
			this.customNodes.put(node.index(), customNode);
			this.reverseNodeMap.put(customNode, node);
		}
		List<CustomEdge> edges = new ArrayList<CustomEdge>(this.customEdges.values());
		this.edgeIds.clear();
		this.customEdges.clear();
		for(CustomEdge customEdge : edges) {
			Edge edge = customEdge.createEdge(this, reverseNodeMap.get(customEdge.getSource()), this.reverseNodeMap.get(customEdge.getTarget()));
			this.edgeIds.put(edge, edge.index());
			this.customEdges.put(edge.index(), customEdge);
		}
		nodeIndexer = this.nodeCount();
		edgeIndexer = this.edgeCount();
		Iterator<?> listenerIt = this.getGraphListeners();
		while(listenerIt.hasNext()) {
			this.removeGraphListener((GraphListener)listenerIt.next());
			listenerIt.remove();
		}
		this.addGraphListener(new CustomGraphListener());
	}	
	@PrePersist
	@PreUpdate
	/*
	 * PrePersist Method.
	 * Writes the attributes of nodes and edges into their corresponding custom nodes and edges.
	 */
	protected void prePersist() {
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
