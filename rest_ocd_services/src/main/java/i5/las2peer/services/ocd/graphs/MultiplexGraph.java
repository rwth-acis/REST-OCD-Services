package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.graphs.properties.AbstractProperty;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.MultiNode;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import java.util.*;
import java.util.stream.Stream;


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
	public static final String layerCountColumnName = "LAYER_COUNT";
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
	 * The number of layer of the graph.
	 */
	private long layerCount;

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

	private Map<String, CustomGraph> mapCustomGraphs = new HashMap<String, CustomGraph>();

	private Map<CustomGraph, String> mapCustomGraphIds = new HashMap<CustomGraph, String>();

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

	public void addLayer(String layerName, CustomGraph customGraph) {
		this.mapCustomGraphs.put(layerName, customGraph);
		layerCount++;
	}

	public Map<String, CustomGraph> getCustomGraphs() {
		return mapCustomGraphs;
	}

	public void setCustomGraphs(Map<String, CustomGraph> customGraphs) {
		this.mapCustomGraphs = customGraphs;
	}
	//protected CustomGraph getCustomGraph(CustomGraph customgraph) {
	//	return mapCustomGraphs.get(customgraph.getId());
	//}
}
