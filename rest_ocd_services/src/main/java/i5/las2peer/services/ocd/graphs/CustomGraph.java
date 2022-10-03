package i5.las2peer.services.ocd.graphs;

import java.util.*;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.MultiNode;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.graphs.properties.AbstractProperty;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;


/**
 * Represents a graph (or network), i.e. the node / edge structure and
 * additional meta information.
 *
 * @author Sebastian
 *
 */
@Entity
@IdClass(CustomGraphId.class)
//TODO: Add boolean/function to check if graph is connected or not.
//TODO: Decide about undirected edges, graphstream would have own functionalities for that.
//TODO: Check whether UUIDs work out as unique graph IDs, collision chances should however be extremely low
//TODO: Check whether UUIDs work out as unique edge IDs, collision chances should however be extremely low
//TODO: Check whether UUIDs work out as unique node IDs, collision chances should however be extremely low. Check whether this could actually replace the current node names. Would however break style with the naming of the other classes.
//TODO: Integrate graphstream attributes into persistence or not?
public class CustomGraph extends MultiGraph {

	/////////////////// DATABASE COLUMN NAMES

	/*
	 * Database column name definitions.
	 */
	public static final String idColumnName = "ID";
	public static final String userColumnName = "USER_NAME";
	private static final String nameColumnName = "NAME";
	// private static final String descriptionColumnName = "DESCRIPTION";
	// private static final String lastUpdateColumnName = "LAST_UPDATE";
	private static final String idEdgeMapKeyColumnName = "RUNTIME_ID";
	private static final String idNodeMapKeyColumnName = "RUNTIME_ID";
	private static final String creationMethodColumnName = "CREATION_METHOD";
	private static final String pathColumnName = "INDEX_PATH";

	/*
	 * Field name definitions for JPQL queries.
	 */
	public static final String USER_NAME_FIELD_NAME = "userName";
	public static final String ID_FIELD_NAME = "persistenceId";
	public static final String CREATION_METHOD_FIELD_NAME = "creationMethod";

	//////////////////////////////////////////////////////////////////
	///////// Attributes
	//////////////////////////////////////////////////////////////////

	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private long persistenceId;

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

	/**
	 * The path to the index for the content of each node belonging to the
	 * graph.
	 */
	@Column(name = pathColumnName)
	private String path = "";

	// /**
	// * The description of the graph.
	// */
	// @Column(name = descriptionColumnName)
	// private String description = "";
	// /**
	// * Last time of modification.
	// */
	// @Version
	// @Column(name = lastUpdateColumnName)
	// private Timestamp lastUpdate;

	/**
	 * The graph's types.
	 */
	@ElementCollection
	private Set<Integer> types = new HashSet<Integer>();

	/**
	 * The graph's properties.
	 */
	@ElementCollection
	private List<Double> properties;

	/**
	 * The log for the benchmark model the graph was created by.
	 */
	@OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = creationMethodColumnName)
	private GraphCreationLog creationMethod = new GraphCreationLog(GraphCreationType.REAL_WORLD,
			new HashMap<String, String>());

	/**
	 * The covers based on this graph.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Cover> covers = new ArrayList<Cover>();

	/**
	 * The simulations based on this graph.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SimulationSeries> simulations = new ArrayList<>();


	///////////////////// THE FOLLOWING ATTRIBUTES ARE MAINTAINED AUTOMATICALLY
	///////////////////// AND ONLY OF INTERNAL / PERSISTENCE USE

	/*
	 * Mapping from fix node ids to custom nodes for additional node data and
	 * persistence.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@MapKeyColumn(name = idNodeMapKeyColumnName)
	private Map<Integer, CustomNode> customNodes = new HashMap<Integer, CustomNode>();
	/*
	 * Mapping from fix edge ids to custom nodes for additional edge data and
	 * persistence.
	 */
	@OneToMany(mappedBy = "graph", orphanRemoval = true, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
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
	private Map<MultiNode, Integer> nodeIds = new HashMap<MultiNode, Integer>();
	/*
	 * Mapping from custom nodes to nodes.
	 */
	@Transient
	private Map<CustomNode, MultiNode> reverseNodeMap = new HashMap<CustomNode, MultiNode>();
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

	@Transient
	private Termmatrix termMatrix = new Termmatrix();

	@Transient
	public Layout layout;

	//////////////////////////////////////////////////////////////////
	///////// Constructor
	//////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance. The name attribute will be a random UUID
	 */
    public CustomGraph() {
		super(UUID.randomUUID().toString());
        this.addSink(new CustomGraphListener(this));
		layout = new SpringBox(false);
		this.addSink(layout); //Layout listener
		layout.addAttributeSink(this);
    }

	/**
	 * Copy constructor.
	 *
	 * @param graph
	 *            The graph to copy.
	 */
	//TODO: Refactor this to actually copy nodes/edges
	public CustomGraph(AbstractGraph graph) {
		super(UUID.randomUUID().toString()); //TODO: CHANGE to correct super execution
		this.addSink(new CustomGraphListener(this));
		layout = new SpringBox(false);
		this.addSink(layout); //Layout listener
		layout.addAttributeSink(this);
		//super(graph);
		Node[] nodes = this.nodes().toArray(Node[]::new);
		for(Node node : nodes) {
			//TODO: Maybe checks needed whether MultiNode or not
			this.addNode(node.getId());
		}
		Edge[] edges = this.edges().toArray(Edge[]::new);
		for(Edge edge : edges) {
			this.addEdge(edge.getId(), edge.getSourceNode().getId(),edge.getTargetNode().getId());
		}
//        Iterator<?> listenerIt = this.getGraphListeners();
//        while (listenerIt.hasNext()) {
//           this.removeGraphListener((GraphListener) listenerIt.next());
//            listenerIt.remove();
//        }
//        this.addGraphListener(new CustomGraphListener());
	}

	/**
	 * Copy constructor.
	 *
	 * @param graph
	 *            The graph to copy.
	 */
	public CustomGraph(CustomGraph graph) {
		super(UUID.randomUUID().toString());
		this.addSink(new CustomGraphListener(this));
		layout = new SpringBox(false);
		this.addSink(layout); //Layout listener
		layout.addAttributeSink(this);

		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			this.addNode(nodesIt.next().getId());
		}

		Iterator<Edge> edgesIt = graph.edges().iterator();
		while(edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			this.addEdge(edge.getId(),edge.getSourceNode().getId(),edge.getTargetNode().getId());
		}

		this.creationMethod = new GraphCreationLog(graph.creationMethod.getType(),
				graph.creationMethod.getParameters());
		this.creationMethod.setStatus(graph.creationMethod.getStatus());
		this.customNodes = new HashMap<Integer, CustomNode>();
		copyMappings(graph.customNodes, graph.customEdges, graph.nodeIds, graph.edgeIds);
		this.userName = new String(graph.userName);
		this.name = new String(graph.name);
		this.persistenceId = graph.persistenceId;
		this.path = graph.path;
		// this.description = new String(graph.description);
		// if(graph.lastUpdate != null) {
		// this.lastUpdate = new Timestamp(graph.lastUpdate.getTime());
		// }
		nodeIndexer = graph.nodeIndexer;
		edgeIndexer = graph.edgeIndexer;
		this.types = new HashSet<Integer>(graph.types);
	}

	//////////////////////////////////////////////////////////////////
	///////// Methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Sets all the structural information to that of another graph. This
	 * includes the structure of the nodes and edges, their custom information
	 * and the graph types.
	 *
	 * @param graph
	 *            The graph to obtain data from.
	 */
	//TODO: Possibly add graphstream attributes as well here (provided we start saving them as well)
	public void setStructureFrom(CustomGraph graph) {
		Node[] nodes = this.nodes().toArray(Node[]::new);
		/*
		 * Removes all nodes and edges including their custom information.
		 */
		for(Node NodeToRemove : nodes) {
			this.removeNode(NodeToRemove);
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

		Node node;
        nodes = graph.nodes().toArray(Node[]::new);
        for(Node nodeToCopy : nodes) {
            node = this.addNode(nodeToCopy.getId());
            this.setNodeName(node, graph.getNodeName(nodeToCopy));
        }
        Node[] nodeArr = this.nodes().toArray(Node[]::new);

        Iterator<Edge> edges = graph.edges().iterator();
        Edge edge;
        Edge refEdge;
        while (edges.hasNext()) {
            refEdge = edges.next();
            edge = this.addEdge(UUID.randomUUID().toString(), nodeArr[refEdge.getSourceNode().getIndex()], nodeArr[refEdge.getTargetNode().getIndex()]);
            this.setEdgeWeight(edge, graph.getEdgeWeight(refEdge));
        }
        /*
         * Updates graph types.
         */
        for (GraphType type : graph.getTypes()) {
            this.addType(type);
        }
	}

	/**
	 * Getter for the persistence id.
	 *
	 * @return The persistence id.
	 */
	public long getPersistenceId() {
		return persistenceId;
	}

	/**
	 * Getter for the user name.
	 *
	 * @return The name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Setter for the user name.
	 *
	 * @param user
	 *            The name.
	 */
	public void setUserName(String user) {
		this.userName = user;
	}

	/**
	 * Getter for the graph name.
	 *
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the graph name.
	 *
	 * @param name
	 *            The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for the graphs path to the index for the node content.
	 *
	 * @return The index path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Setter for the graphs path to the index for the node content.
	 *
	 * @param path
	 *            The index path.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public Termmatrix getTermMatrix() {
		return termMatrix;
	}

	public void setTermMatrix(Termmatrix t) {
		this.termMatrix = t;
	}

	// public String getDescription() {
	// return description;
	// }
	//
	// public Timestamp getLastUpdate() {
	// return lastUpdate;
	// }
	//
	// public void setDescription(String description) {
	// this.description = description;
	// }

	/**
	 * Setter for the creation method.
	 *
	 * @param creationMethod
	 *            The creation method.
	 */
	public void setCreationMethod(GraphCreationLog creationMethod) {
		this.creationMethod = creationMethod;
	}

	/**
	 * Getter for the creation method.
	 *
	 * @return The creation method.
	 */
	public GraphCreationLog getCreationMethod() {
		return this.creationMethod;
	}

	////////// Graph Types //////////

	/**
	 * States whether the graph is of a certain type.
	 *
	 * @param type
	 *            The graph type.
	 * @return TRUE if the graph is of the type, otherwise FALSE.
	 */
	public boolean isOfType(GraphType type) {
		return this.types.contains(type.getId());
	}

	/**
	 * Adds a graph type to the graph.
	 *
	 * @param type
	 *            The graph type.
	 */
	public void addType(GraphType type) {
		this.types.add(type.getId());
	}

	/**
	 * Removes a graph type from the graph.
	 *
	 * @param type
	 *            The graph type.
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
	 *
	 * @return The types.
	 */
	public Set<GraphType> getTypes() {
		Set<GraphType> types = new HashSet<GraphType>();
		for (int id : this.types) {
			types.add(GraphType.lookupType(id));
		}
		return types;
	}

	/**
	 * @return true if the graph is directed
	 */
	public boolean isDirected() {
		return isOfType(GraphType.DIRECTED);
	}

	/**
	 * @return true if the graph is weighted
	 */
	public boolean isWeighted() {
		return isOfType(GraphType.WEIGHTED);
	}

	/**
	 * TODO
	 * @param edgeId
	 *             Id of the edge to be added.
	 * @param src
	 *             Source node
	 * @param srcId
	 *             Source node id
	 * @param dst
	 *             Destination node
	 * @param dstId
	 *             Destination node id
	 * @param directed
	 *             True if edge is directed
	 * @return The directed edge
	 */
	protected Edge addEdge(String edgeId, AbstractNode src, String srcId, AbstractNode dst, String dstId,
								  boolean directed) {
		Edge edge = super.addEdge(edgeId, src, srcId, dst, dstId, true);
		return edge;
	}

	/**
	 * Getter for the edge weight of a certain edge.
	 *
	 * @param edge
	 *            The edge.
	 * @return The edge weight.
	 */
	public double getEdgeWeight(Edge edge) {
		return getCustomEdge(edge).getWeight();
	}

	/**
	 * Setter for the edge weight of a certain edge.
	 *
	 * @param edge
	 *            The edge.
	 * @param weight
	 *            The edge weight.
	 */
	public void setEdgeWeight(Edge edge, double weight) {
		getCustomEdge(edge).setWeight(weight);
	}

	// public long getEdgeId(Edge edge) {
	// return getCustomEdge(edge).getId();
	// }

	/**
	 * Getter for the node name of a certain node.
	 *
	 * @param node
	 *            The node.
	 * @return The node name.
	 */
	public String getNodeName(Node node) {
		return getCustomNode(node).getName();
	}

	/**
	 * Setter for the node name of a certain node.
	 *
	 * @param node
	 *            The node.
	 * @param name
	 *            The node name.
	 */
	public void setNodeName(Node node, String name) {
		getCustomNode(node).setName(name);
	}

	public int getNodeId(Node node) {
		return getCustomNode(node).getId();
	}

	/////////// node degree //////////

	/**
	 * Returns weighted in-degree, i.e. the sum of the weights of all incoming
	 * edges of a node.
	 *
	 * @param node
	 *            The node.
	 * @return The weighted in-degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	//TODO: Check whether we need to account extra for parallel edges here (and in all other edge methods)
	public double getWeightedInDegree(Node node) throws InterruptedException {
		Edge[] inEdges = Stream.concat(this.getPositiveInEdges(node).stream(), this.getNegativeInEdges(node).stream()).toArray(Edge[]::new);
		double inDegree = 0;
		for (Edge edge : inEdges) {
			inDegree += getCustomEdge(edge).getWeight();
		}
		return inDegree;
	}

	/**
	 * Returns positive in-degree, i.e. the weight sum of all positive incoming
	 * edges of a node.
	 *
	 * @param node
	 *            The node.
	 * @return The positive in-degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 *
	 * @author YLi
	 */
	public double getPositiveInDegree(Node node) throws InterruptedException {
		Edge[] inEdges = this.getPositiveInEdges(node).toArray(Edge[]::new);
		double inDegree = 0;
		for (Edge edge : inEdges) {
			inDegree += getCustomEdge(edge).getWeight();
		}
		return inDegree;
	}

	/**
	 * Returns positive out-degree, i.e. the weight sum of all positive outgoing
	 * edges of a node.
	 *
	 * @param node
	 *            The concerned node.
	 * @return The positive out-degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 *
	 * @author YLi
	 */
	public double getPositiveOutDegree(Node node) throws InterruptedException {
		Edge[] outEdges = this.getPositiveOutEdges(node).toArray(Edge[]::new);
		double outDegree = 0;
		for (Edge edge : outEdges) {
			outDegree += getCustomEdge(edge).getWeight();
		}
		return outDegree;
	}

	/**
	 * Returns negative in-degree, i.e. the sum of all negative incoming edges
	 * of a node.
	 *
	 * @param node
	 *            The node under observation.
	 * @return The negative in-degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 *
	 * @author YLi
	 */
	public double getNegativeInDegree(Node node) throws InterruptedException {
		Edge[] inEdges = this.getNegativeInEdges(node).toArray(Edge[]::new);
		double inDegree = 0;
		for (Edge edge : inEdges) {
			inDegree += getCustomEdge(edge).getWeight();
		}
		return inDegree;
	}

	/**
	 * Returns negative out-degree, i.e. the sum of all negative outgoing edges
	 * of a node.
	 *
	 * @param node
	 *            The node under observation.
	 * @return The negative out-degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 *
	 * @author YLi
	 */
	public double getNegativeOutDegree(Node node) throws InterruptedException {
		Edge[] outEdges = this.getNegativeOutEdges(node).toArray(Edge[]::new);
		double outDegree = 0;
		for (Edge edge : outEdges) {
			outDegree += getCustomEdge(edge).getWeight();
		}
		return outDegree;
	}

	/**
	 * Returns the weighted out-degree, i.e. the sum of the weights of all
	 * outgoing edges of a node.
	 *
	 * @param node
	 *            The node.
	 * @return The weighted out-degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public double getWeightedOutDegree(MultiNode node) throws InterruptedException {
		Edge[] outEdges = Stream.concat(this.getPositiveOutEdges(node).stream(), this.getNegativeOutEdges(node).stream()).toArray(Edge[]::new);
		double outDegree = 0;
		for (Edge edge : outEdges) {
			outDegree += getCustomEdge(edge).getWeight();
		}
		return outDegree;
	}

	/**
	 * Returns the weighted node degree, i.e. the sum of the weights of all
	 * incident edges of a node.
	 *
	 * @param node
	 *            The node.
	 * @return The weighted degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public double getWeightedNodeDegree(MultiNode node) throws InterruptedException {
		Edge[] edges = node.edges().toArray(Edge[]::new);
		double degree = 0;
		for (Edge edge : edges) {
			degree += getCustomEdge(edge).getWeight();
		}
		return degree;
	}

	/**
	 * Returns the absolute node degree, i.e. the sum of absolute weights of all
	 * incident edges of a node.
	 *
	 * @param node
	 *            The node.
	 * @return The absolute degree.
	 * @throws InterruptedException If the executing thread was interrupted.
	 *
	 * @author YLi
	 */
	public double getAbsoluteNodeDegree(MultiNode node) throws InterruptedException {
		Edge[] edges = node.edges().toArray(Edge[]::new);
		double degree = 0;
		for (Edge edge : edges) {
			degree += Math.abs(getCustomEdge(edge).getWeight());
		}
		return degree;
	}

	/**
	 * Returns all edge weights
	 * @return Double array containing all edge weights
	 *
	 * @author Tobias
	 */
	public double[] getEdgeWeights() {
		double[] res = new double[this.edgeCount];
		Edge[] edges = this.edges().toArray(Edge[]::new);

		int i = 0;
		for (Edge edge : edges) {
			res[i] = this.getEdgeWeight(edge);
			i++;
		}
		return res;
	}

	/**
	 * Returns the maximum edge weight of the graph.
	 *
	 * @return The maximum edge weight or negative infinity, if there are no
	 *         edges in the graph.
	 */
	public double getMaxEdgeWeight() {
		double maxWeight = Double.NEGATIVE_INFINITY;
		double edgeWeight;
		Edge[] edges = this.edges().toArray(Edge[]::new);

		for (Edge edge : edges) {
			edgeWeight = getCustomEdge(edge).getWeight();
			if (edgeWeight > maxWeight) {
				maxWeight = edgeWeight;
			}
		}
		return maxWeight;
	}

	/**
	 * Returns the minimum edge weight of the graph.
	 *
	 * @return The minimum edge weight or positive infinity, if there are no
	 *         edges in the graph.
	 */
	public double getMinEdgeWeight() {
		double minWeight = Double.POSITIVE_INFINITY;
		double edgeWeight;
		Edge[] edges = this.edges().toArray(Edge[]::new);

		for (Edge edge : edges) {
			edgeWeight = getCustomEdge(edge).getWeight();
			if (edgeWeight < minWeight) {
				minWeight = edgeWeight;
			}
		}
		return minWeight;
	}

	/**
	 * Returns the minimum weighted in-degree of the graph.
	 *
	 * @return The weighted in-degree or positive infinity if the graph does not
	 *         contain any nodes.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public double getMinWeightedInDegree() throws InterruptedException {
		double minDegree = Double.POSITIVE_INFINITY;
		Node[] nodes = this.nodes().toArray(Node[]::new);
		double curDegree;
		for (Node node : nodes) {
			curDegree = getWeightedInDegree(node);
			if (curDegree < minDegree) {
				minDegree = curDegree;
			}
		}
		return minDegree;
	}

	/**
	 * Returns the maximum weighted in-degree of the graph.
	 *
	 * @return The weighted in-degree or negative infinity if the graph does not
	 *         contain any nodes.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public double getMaxWeightedInDegree() throws InterruptedException {
		double maxDegree = Double.NEGATIVE_INFINITY;
		Node[] nodes = this.nodes().toArray(Node[]::new);
		double curDegree;
		for (Node node : nodes) {
			curDegree = getWeightedInDegree(node);
			if (curDegree > maxDegree) {
				maxDegree = curDegree;
			}
		}
		return maxDegree;
	}

	/////////////// neighbours ///////////
	/**
	 * Returns the neighbourhood matrix.
	 *
	 * @return The neighbourhood matrix.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Matrix getNeighbourhoodMatrix() throws InterruptedException {
		int nodeNumber = this.nodeCount;
		Matrix neighbourhoodMatrix = new CCSMatrix(nodeNumber, nodeNumber);
		Edge[] edges = this.edges().toArray(Edge[]::new);
		for (Edge edge : edges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			neighbourhoodMatrix.set(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex(), this.getEdgeWeight(edge));
		}
		return neighbourhoodMatrix;
	}

	/**
	 * Returns the set of all neighbours of a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The neighbour set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Set<Node> getNeighbours(Node node) throws InterruptedException {
		Set<Node> neighbourSet = new HashSet<Node>();
		Node[] neighbours = node.neighborNodes().toArray(Node[]::new); // Gets every "opposite" node of all adjacent edges, can therefore have duplicates

		for (Node neighbour : neighbours) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (!neighbourSet.contains(neighbour)) {
				neighbourSet.add(neighbour);
			}
		}
		return neighbourSet;
	}

	/**
	 * Returns the set of all neighbours of a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The neighbour set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Set<Node> getSuccessorNeighbours(Node node) throws InterruptedException {
		Set<Node> neighbourSet = new HashSet<Node>();
		Node[] neighbours = node.neighborNodes().toArray(Node[]::new);

		for (Node neighbour : neighbours) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (!neighbourSet.contains(neighbour) && neighbour.hasEdgeFrom(node)) {
				neighbourSet.add(neighbour);
			}
		}
		return neighbourSet;
	}

	/**
	 * Returns the set of all neighbours of a given node that have an edge toward it.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The neighbour set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Set<Node> getPredecessorNeighbours(Node node) throws InterruptedException {
		Set<Node> neighbourSet = new HashSet<Node>();
		Node[] neighbours = node.neighborNodes().toArray(Node[]::new);

		for (Node neighbour : neighbours) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (!neighbourSet.contains(neighbour) && neighbour.hasEdgeToward(node)) {
				neighbourSet.add(neighbour);
			}
		}
		return neighbourSet;
	}

	/**
	 * Returns the set of all positive neighbours of a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive neighbour set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Set<Node> getPositiveNeighbours(MultiNode node) throws InterruptedException {
		Set<Node> positiveNeighbourSet = new HashSet<Node>();
		Node[] neighbours = node.neighborNodes().toArray(Node[]::new);

		for (Node neighbour : neighbours) {
			/*
			 * if node a->b positive or node b->a positive
			 */
			Edge[] edges = node.getEdgeSetBetween(neighbour).toArray(Edge[]::new);
			for (Edge edge : edges) {
				if (this.getEdgeWeight(edge) >= 0) {
					positiveNeighbourSet.add(neighbour);
					break;
				}
			}
		}
		return positiveNeighbourSet;
	}

	/**
	 * Returns the set of all negative neighbours of a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The negative neighbour set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Node> getNegativeNeighbours(MultiNode node) throws InterruptedException {
		Set<Node> negativeNeighbourSet = new HashSet<Node>();
		Node[] neighbours = (Node[]) node.neighborNodes().toArray(Node[]::new);

		for (Node neighbour : neighbours) {
			/*
			 * if node a->b negative or node b->a negative
			 */
			Edge[] edges = node.getEdgeSetBetween(neighbour).toArray(Edge[]::new);
			for (Edge edge : edges) {
				if (this.getEdgeWeight(edge) < 0) {
					negativeNeighbourSet.add(neighbour);
					break;
				}
			}
		}
		return negativeNeighbourSet;
	}

	////////// incident edges ////////

	/**
	 * Returns the set of all positive edges incident to a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getPositiveEdges(Node node) throws InterruptedException {
		Set<Edge> positiveEdges = new HashSet<Edge>();
		Edge[] edges = node.edges().toArray(Edge[]::new);
		for (Edge edge : edges) {
			if (this.getEdgeWeight(edge) >= 0) {
				positiveEdges.add(edge);
				break;
			}
		}
		return positiveEdges;
	}

	/**
	 * Returns the set of all positive edges incident to a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getPositiveEdgesAboveZero(Node node) throws InterruptedException {
		Set<Edge> positiveEdges = new HashSet<Edge>();
		Edge[] edges = node.edges().toArray(Edge[]::new);
		for (Edge edge : edges) {
			if (this.getEdgeWeight(edge) > 0) {
				positiveEdges.add(edge);
				break;
			}
		}
		return positiveEdges;
	}

	/**
	 * Returns the set of all positive incoming edges for a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive incoming edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getPositiveInEdges(Node node) throws InterruptedException {
		Set<Edge> positiveInEdges = new HashSet<Edge>();
		Edge[] incidentInEdges = node.enteringEdges().toArray(Edge[]::new);
		for (Edge incidentInEdge : incidentInEdges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (getCustomEdge(incidentInEdge).getWeight() >= 0) {
				positiveInEdges.add(incidentInEdge);
			}
		}
		return positiveInEdges;
	}

	/**
	 * Returns the set of all positive incoming edges for a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive incoming edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getPositiveInEdgesAboveZero(Node node) throws InterruptedException {
		Set<Edge> positiveInEdges = new HashSet<Edge>();
		Edge[] incidentInEdges = node.enteringEdges().toArray(Edge[]::new);
		for (Edge incidentInEdge : incidentInEdges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (getCustomEdge(incidentInEdge).getWeight() > 0) {
				positiveInEdges.add(incidentInEdge);
			}
		}
		return positiveInEdges;
	}

	/**
	 * Returns the set of all positive outgoing edges for a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive outgoing edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getPositiveOutEdges(Node node) throws InterruptedException {
		Set<Edge> positiveOutEdges = new HashSet<Edge>();
		Edge[] incidentOutEdges = node.leavingEdges().toArray(Edge[]::new);
		for (Edge incidentOutEdge : incidentOutEdges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (getCustomEdge(incidentOutEdge).getWeight() >= 0) {
				positiveOutEdges.add(incidentOutEdge);
			}
		}
		return positiveOutEdges;
	}

	/**
	 * Returns the set of all positive outgoing edges for a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The positive outgoing edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getPositiveOutEdgesAboveZero(Node node) throws InterruptedException {
		Set<Edge> positiveOutEdges = new HashSet<Edge>();
		Edge[] incidentOutEdges = node.leavingEdges().toArray(Edge[]::new);
		for (Edge incidentOutEdge : incidentOutEdges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (getCustomEdge(incidentOutEdge).getWeight() > 0) {
				positiveOutEdges.add(incidentOutEdge);
			}
		}
		return positiveOutEdges;
	}

	/**
	 * Returns the set of all negative edges incident to a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The negative edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getNegativeEdges(Node node) throws InterruptedException {
		Set<Edge> negativeEdges = new HashSet<Edge>();
		Edge[] edges = node.edges().toArray(Edge[]::new);
		for (Edge edge : edges) {
			if (this.getEdgeWeight(edge) < 0) {
				negativeEdges.add(edge);
				break;
			}
		}
		return negativeEdges;
	}

	/**
	 * Returns the set of all negative incoming edges for a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The negative incoming edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getNegativeInEdges(Node node) throws InterruptedException {
		Set<Edge> negativeInEdges = new HashSet<Edge>();
		Edge[] incidentInEdges = node.enteringEdges().toArray(Edge[]::new);
		for (Edge incidentInEdge : incidentInEdges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (getCustomEdge(incidentInEdge).getWeight() < 0) {
				negativeInEdges.add(incidentInEdge);
			}
		}
		return negativeInEdges;
	}

	/**
	 * Returns the set of all negative outgoing edges for a given node.
	 *
	 * @param node
	 *            The node under observation.
	 *
	 * @return The negative outgoing edge set of the given node.
	 *
	 * @author YLi
	 * @throws InterruptedException if the thread was interrupted
	 */

	public Set<Edge> getNegativeOutEdges(Node node) throws InterruptedException {
		Set<Edge> negativeOutEdges = new HashSet<Edge>();
		Edge[] incidentOutEdges = node.leavingEdges().toArray(Edge[]::new);
		for (Edge incidentOutEdge : incidentOutEdges) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (getCustomEdge(incidentOutEdge).getWeight() < 0) {
				negativeOutEdges.add(incidentOutEdge);
			}
		}
		return negativeOutEdges;
	}

	////////// properties ////////

	/**
	 * @return properties list
	 */
	public List<Double> getProperties() {
		return this.properties;
	}



	/**
	 * Returns a specific graph property
	 *
	 * @param property requested property
	 *
	 * @return the graph property
	 *
	 */
	public double getProperty(GraphProperty property) {
		return getProperties().get(property.getId());
	}

	/**
	 * Initialize the properties
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	//TODO: Figure out what this means
	public void initProperties() throws InterruptedException {

		this.properties = new ArrayList<>(GraphProperty.size());
		for (int i = 0; i < GraphProperty.size(); i++) {
			AbstractProperty property = null;
			try {
				property = GraphProperty.lookupProperty(i).getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			this.properties.add(i, property.calculate(this));
		}
	}

	/**
	 * Returns a new sub graph of a CustomGraph
	 *
	 * @param nodeIds The node ids for the sub graph
	 * @return sub graph
	 */
	public CustomGraph getSubGraph(List<Integer> nodeIds) {

		CustomGraph subGraph = new CustomGraph();
		int graphSize = nodeCount;
		int subSize = nodeIds.size();
		Map<Integer, Node> nodeMap = new HashMap<>(subSize);

		for (int i = 0; i < subSize; i++) {
			int nodeId = nodeIds.get(i);

			if (nodeId < 0)
				throw new IllegalArgumentException("Invalid node id; negative id");

			if (nodeId > graphSize)
				throw new IllegalArgumentException("Invalid node id; id to high");

			nodeMap.put(nodeId, subGraph.addNode(UUID.randomUUID().toString()));
		}

		for (Edge edge : edges().toArray(Edge[]::new)) {
			int source = edge.getSourceNode().getIndex();
			int target = edge.getTargetNode().getIndex();

			if (nodeIds.contains(source) && nodeIds.contains(target)) {
				subGraph.addEdge(UUID.randomUUID().toString(), nodeMap.get(source), nodeMap.get(target));
			}
		}
		return subGraph;
	}

	////////////////// THE FOLLOWING METHODS ARE ONLY OF INTERNAL PACKAGE USE
	////////////////// AND FOR PERSISTENCE PURPOSES

	/**
	 * Initializes all node and edge mappings for the copy constructor.
	 *
	 * @param customNodes
	 *            The custom node mapping of the copied custom graph.
	 * @param customEdges
	 *            The custom edge mapping of the copied custom graph.
	 * @param nodeIds
	 *            The node id mapping of the copied custom graph.
	 * @param edgeIds
	 *            The edge id mapping of the copied custom graph.
	 */
	protected void copyMappings(Map<Integer, CustomNode> customNodes, Map<Integer, CustomEdge> customEdges,
								Map<MultiNode, Integer> nodeIds, Map<Edge, Integer> edgeIds) {

		for (Map.Entry<Integer, CustomNode> entry : customNodes.entrySet()) {
			this.customNodes.put(entry.getKey(), new CustomNode(entry.getValue()));
		}
		for (Map.Entry<Integer, CustomEdge> entry : customEdges.entrySet()) {
			this.customEdges.put(entry.getKey(), new CustomEdge(entry.getValue()));
		}
		Node[] nodeArr = this.nodes().toArray(Node[]::new);
		for (Map.Entry<MultiNode, Integer> entry : nodeIds.entrySet()) {
			this.nodeIds.put((MultiNode) nodeArr[entry.getKey().getIndex()], entry.getValue());
		}
		Node[] nodes = this.nodes().toArray(Node[]::new);
		for (Node node : nodes) {
			this.reverseNodeMap.put(this.getCustomNode(node), (MultiNode) node);
		}
		Edge[] edgeArr = this.edges().toArray(Edge[]::new);
		for (Map.Entry<Edge, Integer> entry : edgeIds.entrySet()) {
			this.edgeIds.put(edgeArr[entry.getKey().getIndex()], entry.getValue());
		}
	}

	/**
	 * Returns the custom edge object corresponding to an edge.
	 *
	 * @param edge
	 *            An edge which must belong to this graph.
	 * @return The corresponding custom edge object.
	 */
	protected CustomEdge getCustomEdge(Edge edge) {
		int index = edgeIds.get(edge);
		return customEdges.get(index);
	}

	/**
	 * Returns the custom node object corresponding to a node.
	 *
	 * @param node
	 *            A node which must belong to this graph.
	 * @return The corresponding custom node object.
	 */
	protected CustomNode getCustomNode(Node node) {
		int index = nodeIds.get(node);
		return customNodes.get(index);
	}

	/**
	 * Returns the node object corresponding to a custom node.
	 *
	 * @param customNode
	 *            A customMultiNode which must belong to this graph.
	 * @return The corresponding node object.
	 */
	protected Node getNode(CustomNode customNode) {
		return reverseNodeMap.get(customNode);
	}

	/**
	 * Creates a new custom node object and maps the node to it.
	 *
	 * @param node
	 *            The node.
	 */
	protected void addCustomNode(MultiNode node) {
		CustomNode customNode = new CustomNode();
		this.nodeIds.put(node, this.nodeIndexer);
		this.customNodes.put(nodeIndexer, customNode);
		this.reverseNodeMap.put(customNode, node);
		nodeIndexer++;
	}

	/**
	 * Removes the mappings between a node and its custom node object.
	 *
	 * @param node
	 * 		  the node
	 */
	protected void removeCustomNode(MultiNode node) {
		CustomNode customNode = this.getCustomNode(node);
		int id = this.nodeIds.get(node);
		this.nodeIds.remove(node);
		this.customNodes.remove(id);
		this.reverseNodeMap.remove(customNode);
	}

	/**
	 * Creates a new custom edge object and maps the edge to it.
	 *
	 * @param edge
	 *            The edge.
	 */
	protected void addCustomEdge(Edge edge) {
		CustomEdge customEdge = new CustomEdge();
		this.edgeIds.put(edge, this.edgeIndexer);
		this.customEdges.put(edgeIndexer, customEdge);
		edgeIndexer++;
	}

	/**
	 * Removes the mapping from an edge to its custom edge.
	 *
	 * @param edge
	 * 		  the edge
	 */
	protected void removeCustomEdge(Edge edge) {
		int id = this.edgeIds.get(edge);
		this.edgeIds.remove(edge);
		this.customEdges.remove(id);
	}

	/////////////////////////// PERSISTENCE CALLBACK METHODS

	/**
	 * PostLoad Method. Creates node and edge objects from the custom nodes and
	 * edges and sets the mappings between the two. The mapping indices are
	 * reset to omit rising numbers due to deletions and reinsertions.
	 */
	@PostLoad
	private void postLoad() {
		List<CustomNode> nodes = new ArrayList<CustomNode>(this.customNodes.values());
		this.nodeIds.clear();
		this.customNodes.clear();
		for (CustomNode customNode : nodes) {
			MultiNode node = (MultiNode) customNode.createNode(this);
			this.nodeIds.put(node, node.getIndex());
			this.customNodes.put(node.getIndex(), customNode);
			this.reverseNodeMap.put(customNode, node);
		}
		List<CustomEdge> edges = new ArrayList<CustomEdge>(this.customEdges.values());
		this.edgeIds.clear();
		this.customEdges.clear();
		for (CustomEdge customEdge : edges) {
			Edge edge = customEdge.createEdge(this, reverseNodeMap.get(customEdge.getSource()),
					this.reverseNodeMap.get(customEdge.getTarget()));
			this.edgeIds.put(edge, edge.getIndex());
			this.customEdges.put(edge.getIndex(), customEdge);
		}
		nodeIndexer = this.nodeCount;
		edgeIndexer = this.edgeCount;
//        Iterator<?> listenerIt = this.getGraphListeners();
//        while (listenerIt.hasNext()) {
//            this.removeGraphListener((GraphListener) listenerIt.next());
//            listenerIt.remove();
//        }
//        this.addGraphListener(new CustomGraphListener());
	}


	/**
	 * PrePersist Method. Writes the attributes of nodes and edges into their
	 * corresponding custom nodes and edges.
	 *
	 * Makes sure the Graph Properties are up-to-date.
	 *
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	@PrePersist
	@PreUpdate
	protected void prePersist() throws InterruptedException {
		Node[] nodes = this.nodes().toArray(Node[]::new);
		for (Node node : nodes) {
			this.getCustomNode(node).update(this, (Node)node);
		}
		Edge[] edges = this.edges().toArray(Edge[]::new);
		for (Edge edge : edges) {
			this.getCustomEdge(edge).update(this, edge);
		}

		initProperties();
	}


}

