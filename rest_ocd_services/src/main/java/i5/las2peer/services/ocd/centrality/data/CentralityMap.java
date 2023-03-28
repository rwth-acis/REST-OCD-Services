package i5.las2peer.services.ocd.centrality.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;












import com.fasterxml.jackson.core.type.TypeReference;
import org.graphstream.graph.Node;
import org.w3c.dom.Element;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;




public class CentralityMap {
	/*
	 * Database column name definitions.
	 */
	public static final String nameColumnName = "NAME";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	public static final String idColumnName = "ID";
	private static final String creationMethodColumnName = "CREATION_METHOD";
	//ArangoDB
	public static final String collectionName = "centralitymap";
	private static final String mapColumnName = "MAP";
	public static final String creationMethodKeyColumnName = "CREATION_METHOD";
	public static final String graphKeyColumnName = "GRAPH_KEY";
	/*
	 * Field name definitions for JPQL queries.
	 */
	public static final String GRAPH_FIELD_NAME = "graph";
	public static final String CREATION_METHOD_FIELD_NAME  = "creationMethod";
	public static final String ID_FIELD_NAME = "key";
	
	/**
	 * System generated persistence id.
	 */
	private long id;
	/**
	 * System generated persistence key.
	 */


	private String key = "";
	/**
	 * The name of the CentralityMap.
	 */

	private String name = "";
	/**
	 * The graph that the CentralityMap is based on.
	 */
	private CustomGraph graph;
	
	/**
	 * Logged data about the algorithm that created the CentralityMap.
	 */

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
	 * Getter for the key.
	 * @return The key.
	 */
	public String getKey() {
		return key;
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
	public void setNodeValue(Node node, double value) { //TODO: Check If original functionality maintained
		if(graph.getNode(node.getId()) != null) { // TODO: should be key not id?
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
	 * Retrieve the names of the top k nodes of the CentralityMap.
	 * 
	 * @param k The number of top nodes that are considered.
	 * @return The list of node names.
	 */
	public List<String> getTopNodes(int k) {
		Map<String, Double> valuesMap = this.getMap();
		Set<String> keySet = valuesMap.keySet();
	    List<String> keys = new ArrayList<String>(keySet);

	    Collections.sort(keys, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	        	return Double.compare(valuesMap.get(s2), valuesMap.get(s1));
	        }
	    });
	    return keys.subList(0, k);
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

	//persistence functions
	public void persist(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
	
		if(this.graph == null) {
			throw new IllegalArgumentException("graph attribute of the centralityMap to be persisted does not exist");
		}
		else if(this.graph.getKey().equals("")) {
			throw new IllegalArgumentException("the graph of the centralityMap is not persisted yet");
		}
		bd.addAttribute(graphKeyColumnName, this.graph.getKey());
		bd.addAttribute(nameColumnName, this.name);
		this.creationMethod.persist(db, createOptions);
		bd.addAttribute(creationMethodKeyColumnName, this.creationMethod.getKey());
		bd.addAttribute(mapColumnName, this.map);
		collection.insertDocument(bd, createOptions);
		this.key = bd.getKey();
		
	}
	
	public void updateDB(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);		
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		
		BaseDocument bd = new BaseDocument();		
		if(this.graph == null) {
			throw new IllegalArgumentException("graph attribute of the map to be updated does not exist");
		}
		else if(this.graph.getKey().equals("")) {
			throw new IllegalArgumentException("the graph of the map is not persisted yet");
		}
		bd.addAttribute(graphKeyColumnName, this.graph.getKey());
		bd.addAttribute(nameColumnName, this.name);
		bd.addAttribute(creationMethodKeyColumnName, this.creationMethod.getKey());
		bd.addAttribute(mapColumnName, this.map);
		
		this.creationMethod.updateDB(db, transId);
		
		collection.updateDocument(this.key, bd, updateOptions);
	}
	
	public static CentralityMap load(String key, CustomGraph g, ArangoDatabase db, String transId) {	
		CentralityMap cm = null;
		ArangoCollection collection = db.collection(collectionName);
		DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);
		if (bd != null) {
			cm = new CentralityMap(g);
			ObjectMapper om = new ObjectMapper();	//prepair attributes
			String graphKey = bd.getAttribute(graphKeyColumnName).toString();
			if(!graphKey.equals(g.getKey())) {
				System.out.println("graph with key: " + g.getKey() + " does not fit to centralityMap with GraphKey: " + graphKey);
				return null;
			}
			String creationMethodKey = bd.getAttribute(creationMethodKeyColumnName).toString();
			Object objMap = bd.getAttribute(mapColumnName);
			
			//restore all attributes
			cm.key = key;
			cm.name = bd.getAttribute(nameColumnName).toString();
			cm.creationMethod = CentralityCreationLog.load(creationMethodKey, db, readOpt);
			cm.map = om.convertValue(objMap,new TypeReference<HashMap<String,Double>>() { });
		}	
		else {
			System.out.println("empty CentralityMap document");
		}
		return cm;
	}
	
	@Override
	public String toString() {
		String centralityMapString = "Centrality Map: " + getName() + "\n";
		centralityMapString += "Graph: " + getGraph().getName() + "\n";
		centralityMapString += "Values:" + "\n";
		for(String nodeName : getMap().keySet()) {
			centralityMapString += nodeName + ": " + Double.toString(getNodeValue(nodeName)) + "\n";
		}
		return centralityMapString;
	}
}
