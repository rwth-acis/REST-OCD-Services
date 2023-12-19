package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.*;
import java.util.*;

/**
 * Represents a graph (or network), i.e. the node / edge structure and
 * additional meta information.
 * 
 * @author Maren Hanke
 *
 */

public class MultiplexGraph {
	public static final String idColumnName = "ID";
	public static final String userColumnName = "USER_NAME";
	public static final String nameColumnName = "NAME";
	public static final String nodeCountColumnName = "NODE_COUNT";
	public static final String edgeCountColumnName = "EDGE_COUNT";
	public static final String layerCountColumnName = "LAYER_COUNT";
	public static final String layerKeysColumnName = "LAYER_KEYS";
	public static final String creationMethodKeyColumnName = "CREATION_METHOD_KEY";
	public static final String typesColumnName = "TYPES";
	public static final String collectionName = "multiplexgraph";

	//////////////////////////////////////////////////////////////////
	///////// Attributes
	//////////////////////////////////////////////////////////////////
	/**
	 * System generated persistence key.
	 */
	private String key = "";

	/**
	 * The name of the user owning the graph.
	 */
	private String userName = "";

	/**
	 * The name of the graph.
	 */
	private String name = "";

	/**
	 * The number of nodes in the graph.
	 */
	private long graphNodeCount;

	/**
	 * The number of edges in the graph.
	 */
	private long graphEdgeCount;

	/**
	 * The number of layers of the graph.
	 */
	private int layerCount;

	/**
	 * The keys of the layers of the graph.
	 */
	private List<String> layerKeys = new ArrayList<String>();

	/**
	 * The graph's types.
	 */
	private Set<Integer> types = new HashSet<Integer>();

	/**
	 * The log for the benchmark model the graph was created by.
	 */
	private GraphCreationLog creationMethod = new GraphCreationLog(GraphCreationType.REAL_WORLD,
			new HashMap<String, String>());

	/**
	 * The covers based on this graph.
	 */
	private List<Cover> covers = new ArrayList<Cover>();

	private Map<String, MultiplexCustomGraph> mapMultiplexCustomGraphs = new HashMap<String, MultiplexCustomGraph>();

	//private Map<MultiplexCustomGraph, String> mapMultiplexCustomGraphIds = new HashMap<MultiplexCustomGraph, String>();

	//////////////////////////////////////////////////////////////////
	///////// Constructor
	//////////////////////////////////////////////////////////////////
	public MultiplexGraph() {
	}

	//////////////////////////////////////////////////////////////////
	///////// Methods
	//////////////////////////////////////////////////////////////////
	/**
	 * Getter for the persistence id.
	 *
	 * @return The persistence id.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Getter for the username.
	 *
	 * @return The name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Setter for the username.
	 *
	 * @param user The name.
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
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

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

	/**
	 * Getter for the number of nodes.
	 *
	 * @return The number of nodes.
	 */
	public long getNodeCount(){return this.graphNodeCount;}

	/**
	 * Getter for the number of nodes.
	 *
	 * @param nodeCount The number of nodes.
	 */
	public void setNodeCount(long nodeCount){this.graphNodeCount = nodeCount;}
	/**
	 * Getter for the number of edges.
	 *
	 * @return The number of edges.
	 */
	public long getEdgeCount(){return this.graphEdgeCount;}
	/**
	 * Getter for the number of edges.
	 *
	 * @param edgeCount The number of edges.
	 */
	public void setEdgeCount(long edgeCount){this.graphEdgeCount = edgeCount;}
	/**
	 * Getter for the number of layers.
	 *
	 * @return The number of layers.
	 */
	public int getLayerCount(){return this.layerCount;}

	/**
	 * Getter for the layer keys.
	 * @return	The layer keys.
	 */
	public List<String> getLayerKeys(){return this.layerKeys;}

	/**
	 * Adds a layer key
	 * @param layerKey	The layer key
	 */
	public void addLayerKey(String layerKey){this.layerKeys.add(layerKey);}


	////////// Graph Types //////////
	/**
	 * States whether the graph is of a certain type.
	 *
	 * @param type The graph type.
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

	public void addLayer(String layerName, MultiplexCustomGraph multiplexCustomGraph) {
		this.mapMultiplexCustomGraphs.put(layerName, multiplexCustomGraph);
		layerCount++;
	}

	public Map<String, MultiplexCustomGraph> getMultiplexCustomGraphs() {
		return mapMultiplexCustomGraphs;
	}


	//public void setMultiplexCustomGraphs(Map<String, MultiplexCustomGraph> multiplexCustomGraphs) {
	//	this.mapMultiplexCustomGraphs = multiplexCustomGraphs;
	//}
	//protected MultiplexCustomGraph getMultiplexCustomGraph(MultiplexCustomGraph multiplexCustomGraph) {
	//	return mapMultiplexCustomGraphs.get(multiplexCustomGraph.getId());
	//}

	public void persist(ArangoDatabase db, String transId) throws InterruptedException {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		//options for the transaction
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		bd.addAttribute(userColumnName, this.userName);
		bd.addAttribute(nameColumnName, this.name);
		bd.addAttribute(typesColumnName, this.types);
		bd.addAttribute(nodeCountColumnName, this.graphNodeCount);
		bd.addAttribute(edgeCountColumnName, this.graphEdgeCount);
		bd.addAttribute(layerCountColumnName, this.layerCount);
		bd.addAttribute(layerKeysColumnName, this.layerKeys);
		this.creationMethod.persist(db, createOptions);
		bd.addAttribute(creationMethodKeyColumnName, this.creationMethod.getKey());
		collection.insertDocument(bd, createOptions);
		this.key = bd.getKey();

		bd = new BaseDocument();
		collection.updateDocument(this.key, bd, updateOptions);
	}
}
