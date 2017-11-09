package i5.las2peer.services.ocd.centrality.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Node;

@Entity
@IdClass(CentralityMapId.class)
public class CentralityMap {
	/*
	 * Database column name definitions.
	 */
	public static final String nameColumnName = "NAME";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	public static final String idColumnName = "ID";
	private static final String creationMethodColumnName = "CREATION_METHOD";
	
	/*
	 * Field name definitions for JPQL queries.
	 */
	public static final String GRAPH_FIELD_NAME = "graph";
	public static final String CREATION_METHOD_FIELD_NAME  = "creationMethod";
	public static final String ID_FIELD_NAME = "id";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	/**
	 * The name of the CentralityMap.
	 */
	@Column(name = nameColumnName)
	private String name = "";
	/**
	 * The graph that the CentralityMap is based on.
	 */
	@Id
	@JoinColumns( {
		@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomGraph.idColumnName),
		@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomGraph.userColumnName)
	})
	private CustomGraph graph;
	
	/**
	 * Logged data about the algorithm that created the CentralityMap.
	 */
	@OneToOne(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name = creationMethodColumnName)
	private CentralityCreationLog creationMethod = new CentralityCreationLog(CentralityMeasureType.UNDEFINED, CentralityCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>());

	private Map<String, Double> map = new HashMap<String, Double>();
	
	/**
	 * Creates a new instance.
	 * Only for persistence purposes.
	 */
	protected CentralityMap() {
		
	}
	
	public CentralityMap(CustomGraph graph) {
		this.graph = graph;
	}
	
	/**
	 * Getter for the id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Getter for the CentralityMap name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the CentralityMap name.
	 * 
	 * @param name
	 *            The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Getter for the graph that the CentralityMap is based on.
	 * @return The graph.
	 */
	public CustomGraph getGraph() {
		return graph;
	}
	
	/**
	 * Getter for the map that maps the node names to the centrality values.
	 * @return The map.
	 */
	public Map<String, Double> getMap() {
		return map;
	}
	
	/**
	 * Setter for the map that maps the node names to the centrality values.
	 * @param map The map.
	 */
	public void setMap(Map<String, Double> map) {
		this.map = map;
	}
	
	/**
	 * If the given node is contained in the graph corresponding to the CentralityMap, 
	 * its centrality values is set to the given value.
	 * @param node The node whose value is set.
	 * @param value The centrality value that is assigned to the node.
	 */
	public void setNodeValue(Node node, double value) {
		if(graph.contains(node)) {
			map.put(graph.getNodeName(node), value);
		}
	}
	
	/**
	 * Get the centrality value of the node.
	 * @param node The node.
	 * @return The centrality assigned to the node by the CentralityMap.
	 */
	public double getNodeValue(Node node) {
		return map.get(graph.getNodeName(node));
	}
	
	/**
	 * Get the centrality value of the node.
	 * @param nodeName The name of the node.
	 * @return The centrality assigned to the node by the CentralityMap.
	 */
	public double getNodeValue(String nodeName) {
		return map.get(nodeName);
	}
	
	/**
	 * Get the minimum centrality value assigned by the CentralityMap.
	 * @return The minimum centrality value.
	 */
	public double getMinValue() {
		double res = Double.POSITIVE_INFINITY;
		for(double d : map.values()) {
			if(d < res)
				res = d;
		}
		return res;
	}
	
	/**
	 * Get the maximum centrality value assigned by the CentralityMap.
	 * @return The maximum centrality value.
	 */
	public double getMaxValue() {
		double res = Double.NEGATIVE_INFINITY;
		for(double d : map.values()) {
			if(d > res)
				res = d;
		}
		return res;
	}
	
	/**
	 * Getter for the CentralityMap creation method.
	 * @return The creation method.
	 */
	public CentralityCreationLog getCreationMethod() {
		return creationMethod;
	}

	/**
	 * Setter for the CentralityMap creation method.
	 * @param creationMethod The creation method.
	 */
	public void setCreationMethod(CentralityCreationLog creationMethod) {
		if(creationMethod != null) {
			this.creationMethod = creationMethod;
		}
		else {
			this.creationMethod = new CentralityCreationLog(CentralityMeasureType.UNDEFINED, CentralityCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>());
		}
	}

	@Override
	public String toString() {
		return "map=" + map;
	}
}
