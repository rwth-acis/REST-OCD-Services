package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.graphs.properties.GraphProperty;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.utils.NonZeroEntriesVectorProcedure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphstream.graph.Node;

/**
 * Represents a cover, i.e. the result of an overlapping community detection
 * algorithm holding the community structure and additional meta data.
 *
 * @author Sebastian
 *
 */
public class Cover {

	//////////////////////// DATABASE COLUMN NAMES ////////////////////////

	/*
	 * Database column name definitions.
	 */
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	public static final String nameColumnName = "NAME";
	public static final String idColumnName = "ID";
	private static final String creationMethodColumnName = "CREATION_METHOD";
	public static final String simCostsColumnName = "SIMILARITYCOSTS";
	public static final String numberOfCommunitiesColumnName = "NUMBER_OF_COMMUNITIES";
	// private static final String descriptionColumnName = "DESCRIPTION";
	// private static final String lastUpdateColumnName = "LAST_UPDATE";

	//ArangoDB name definitions
	public static final String collectionName = "cover";
	public static final String graphKeyColumnName = "GRAPH_KEY";
	public static final String creationMethodKeyColumnName = "CREATION_METHOD_KEY";
	public static final String communityKeysColumnName = "COMMUNITY_KEYS";
	/*
	 * Field name definitions for JPQL queries.
	 */
	public static final String GRAPH_FIELD_NAME = "graph";
	public static final String CREATION_METHOD_FIELD_NAME = "creationMethod";
	public static final String METRICS_FIELD_NAME = "metrics";
	public static final String ID_FIELD_NAME = "key";
	public static final String NAME_FIELD_NAME = "name";
	public static final String COMMUNITY_COUNT_FIELD_NAME = "numberOfCommunities";

	////////////////////////////// ATTRIBUTES //////////////////////////////
	/**
	 * System generated persistence id.
	 */
	private long id;
	/**
	 * System generated persistence key.
	 */
	private String key = "";
	/**
	 * The graph that the cover is based on.
	 */
	private CustomGraph graph = new CustomGraph();

	/**
	 * The name of the cover.
	 */
	private String name = "";

	/**
	 * The number of communities in the cover
	 */
	private Integer numberOfCommunities;

	/**
	 * Logged data about the algorithm that created the cover.
	 */
	private CoverCreationLog creationMethod = new CoverCreationLog(CoverCreationType.UNDEFINED,
			new HashMap<String, String>(), new HashSet<GraphType>());

	/**
	 * The communities forming the cover.
	 */
	private List<Community> communities = new ArrayList<Community>();

	/**
	 * The metric logs calculated for the cover.
	 */
	private List<OcdMetricLog> metrics = new ArrayList<OcdMetricLog>();

	/**
	 * The similarity costs calculated for the cover.
	 */
	private double simCosts;

	///////////////////////////// CONSTRUCTORS /////////////////////////////

	/**
	 * Creates a new instance. Only for persistence purposes.
	 */
	protected Cover() {
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param graph
	 *            The graph that the cover is based on.
	 */
	public Cover(CustomGraph graph) {
		this.graph = graph;
	}

	/**
	 * Creates an instance of a cover by deriving the communities from a
	 * membership matrix. Note that the membership matrix (and consequently the
	 * cover) will automatically be row-wise normalized according to the 1-norm.
	 * 
	 * @param graph
	 *            The corresponding graph.
	 * @param memberships
	 *            A membership matrix, with non-negative entries. Contains one
	 *            row for each node and one column for each community. Entry
	 *            (i,j) in row i and column j represents the membership degree /
	 *            belonging factor of the node with index i with respect to the
	 *            community with index j.
	 */
	public Cover(CustomGraph graph, Matrix memberships) {
		this.graph = graph;
		setMemberships(memberships, true);
		this.numberOfCommunities = communityCount();
	}

	//////////////////////////// GETTER & SETTER ////////////////////////////

	/**
	 * Getter for the id.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Getter for the key.
	 * 
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Getter for the graph that the cover is based on.
	 * 
	 * @return The graph.
	 */
	public CustomGraph getGraph() {
		return graph;
	}

	/**
	 * Setter for the graph that the cover is based on.
	 * 
	 * @param graph
	 *            The graph.
	 */
	public void setGraph(CustomGraph graph) {
		this.graph = graph;
	}

	/**
	 * Getter for the cover name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for the cover name.
	 * 
	 * @param name
	 *            The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for the simulation costs.
	 * 
	 * @return The simCost.
	 */
	public double getSimCosts() {
		return this.simCosts;
	}

	/**
	 * Setter for the simulation costs.
	 * @param costs the costs
	 */
	public void setSimCosts(double costs) {
		this.simCosts = costs;
	}

	/**
	 * Getter for the cover creation method.
	 * 
	 * @return The creation method.
	 */
	public CoverCreationLog getCreationMethod() {
		return creationMethod;
	}

	/**
	 * Setter for the cover creation method.
	 * 
	 * @param creationMethod
	 *            The creation method.
	 */
	public void setCreationMethod(CoverCreationLog creationMethod) {
		if (creationMethod != null) {
			this.creationMethod = creationMethod;
		} else {
			this.creationMethod = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
					new HashSet<GraphType>());
		}
	}

	// /**
	// * Getter for the cover description.
	// * @return
	// */
	// public String getDescription() {
	// return description;
	// }

	// /**
	// * Setter for the cover description.
	// * @param description
	// */
	// public void setDescription(String description) {
	// this.description = description;
	// }

	// /**
	// * Getter for the last update
	// * @return
	// */
	// public Timestamp getLastUpdate() {
	// return lastUpdate;
	// }

	///////////////////////////// MEMBERSHIPS /////////////////////////////

	/**
	 * Getter for the membership matrix representing the community structure.
	 * 
	 * @return The membership matrix. Contains one row for each node and one
	 *         column for each community. Entry (i,j) in row i and column j
	 *         represents the membership degree / belonging factor of the node
	 *         with index i with respect to the community with index j. All
	 *         entries are non-negative and the matrix is row-wise normalized
	 *         according to the 1-norm.
	 */
	public Matrix getMemberships() {
		Matrix memberships = new CCSMatrix(graph.getNodeCount(), communities.size());
		Map<CustomNode, Node> reverseNodeMap = new HashMap<CustomNode, Node>();
		Iterator<Node> nodes = graph.iterator();
		while (nodes.hasNext()) {
			Node node = nodes.next();
			reverseNodeMap.put(graph.getCustomNode(node), node);

		}
		for (int i = 0; i < communities.size(); i++) {
			Community community = communities.get(i);
			for (Map.Entry<Node, Double> membership : community.getMemberships().entrySet()) {
				memberships.set(membership.getKey().getIndex(), i, membership.getValue());
			}
		}
		return memberships;
	}

	/**
	 * Sets the communities from a membership matrix. All metric logs (besides
	 * optionally the execution time) will be removed from the cover. Note that
	 * the membesrship matrix (and consequently the cover) will automatically be
	 * row normalized.
	 * 
	 * @param memberships
	 *            A membership matrix, with non negative entries. Each row i
	 *            contains the belonging factors of the node with index i of the
	 *            corresponding graph. Hence the number of rows corresponds the
	 *            number of graph nodes and the number of columns the number of
	 *            communities.
	 * @param keepExecutionTime
	 *            Decides whether the (first) execution time metric log is kept.
	 */
	protected void setMemberships(Matrix memberships, boolean keepExecutionTime) {
		if (memberships.rows() != graph.getNodeCount()) {
			throw new IllegalArgumentException(
					"The row number of the membership matrix must correspond to the graph node count.");
		}
		communities.clear();
		OcdMetricLog executionTime = getMetric(OcdMetricType.EXECUTION_TIME);
		metrics.clear();
		if (executionTime != null && keepExecutionTime) {
			metrics.add(executionTime);
		}
		memberships = this.normalizeMembershipMatrix(memberships);
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		for (int j = 0; j < memberships.columns(); j++) {
			Community community = new Community(this);
			communities.add(community);
		}
		for (int i = 0; i < memberships.rows(); i++) {
			NonZeroEntriesVectorProcedure procedure = new NonZeroEntriesVectorProcedure();
			memberships.getRow(i).eachNonZero(procedure);
			List<Integer> nonZeroEntries = procedure.getNonZeroEntries();
			for (int j : nonZeroEntries) {
				Community community = communities.get(j);
				community.setBelongingFactor(nodes[i], memberships.get(i, j));
			}

		}
		this.updateNumberOfCommunities(this.communityCount());
	}

	/**
	 * Sets the communities from a membership matrix. All metric logs (besides
	 * optionally the execution time) will be removed from the cover. Note that
	 * the membership matrix (and consequently the cover) will automatically be
	 * row-wise normalized according to the 1-norm.
	 * 
	 * @param memberships
	 *            A membership matrix, with non negative entries. Each row i
	 *            contains the belonging factors of the node with index i of the
	 *            corresponding graph. Hence the number of rows corresponds the
	 *            number of graph nodes and the number of columns the number of
	 *            communities.
	 */
	public void setMemberships(Matrix memberships) {
		setMemberships(memberships, false);
		this.updateNumberOfCommunities(this.communityCount());
	}

	//////////////////////////// METRICS ////////////////////////////

	/**
	 * Getter for the metric logs calculated for the cover.
	 * 
	 * @return The metric logs.
	 */
	public List<OcdMetricLog> getMetrics() {
		return metrics;
	}

	/**
	 * Setter for the metric logs calculated for the cover.
	 * 
	 * @param metrics
	 *            The metric logs.
	 */
	public void setMetrics(List<OcdMetricLog> metrics) {
		this.metrics.clear();
		for (OcdMetricLog metric : metrics) {
			if (metric != null)
				this.metrics.add(metric);
		}
	}

	/**
	 * Returns the first metric occurrence with the corresponding metric type.
	 * 
	 * @param metricType
	 *            The metric type.
	 * @return The metric. Null if no such metric exists.
	 */
	public OcdMetricLog getMetric(OcdMetricType metricType) {
		for (OcdMetricLog metric : this.metrics) {
			if (metricType == metric.getType()) {
				return metric;
			}
		}
		return null;
	}

	/**
	 * Adds a metric log to the cover.
	 * 
	 * @param metric
	 *            The metric log.
	 */
	public void addMetric(OcdMetricLog metric) {
		if (metric != null) {
			this.metrics.add(metric);
		}
	}

	/**
	 * Removes a metric log from the cover.
	 * 
	 * @param metric
	 *            The metric log.
	 */
	public void removeMetric(OcdMetricLog metric) {
		this.metrics.remove(metric);
	}

	///////////////////////////// COMMUNITIES /////////////////////////////

	/**
	 * Returns the community count of the cover.
	 * 
	 * @return The community count.
	 */
	public int communityCount() {
		return communities.size();
	}
	
	/**
	 * Returns the communities of this cover
	 * 
	 * @return community list
	 */
	public List<Community> getCommunities() {
		return this.communities;
	}
	
	/**
	 * Returns the size (i.e. the amount of members) of a certain community.
	 * 
	 * @param communityIndex
	 *            The community index.
	 * @return The size.
	 */
	public int getCommunitySize(int communityIndex) {
		return communities.get(communityIndex).getSize();
	}

	/**
	 * Getter for the name of a certain community.
	 * 
	 * @param communityIndex
	 *            The community index.
	 * @return The name.
	 */
	public String getCommunityName(int communityIndex) {
		return communities.get(communityIndex).getName();
	}

	/**
	 * Setter for the name of a certain community.
	 * 
	 * @param communityIndex
	 *            The community index.
	 * @param name
	 *            The name.
	 */
	public void setCommunityName(int communityIndex, String name) {
		communities.get(communityIndex).setName(name);
	}

	/**
	 * Setter for the number of communities in the cover.
	 *
	 * @param numberOfCommunities
	 *            The community count.
	 */
	public void updateNumberOfCommunities(Integer numberOfCommunities) {
		this.numberOfCommunities = numberOfCommunities;
	}

	/**
	 * Getter for the color of a certain community.
	 * 
	 * @param communityIndex
	 *            The community index.
	 * @return The color.
	 */
	public Color getCommunityColor(int communityIndex) {
		return communities.get(communityIndex).getColor();
	}
	
	/**
	 * Checks whether the cover has been painted already
	 * 
	 * @return false if not painted(everything white), true if not
	 */
	public boolean isPainted() {
		for(Community comm : communities) {
			if( !(comm.getColor().equals(new Color(Color.WHITE.getRGB()))) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Setter for the color of a certain community.
	 * 
	 * @param communityIndex
	 *            The community index.
	 * @param color
	 *            The color.
	 */
	public void setCommunityColor(int communityIndex, Color color) {
		communities.get(communityIndex).setColor(color);
	}
	
	/**
	 * Returns the property value of a community
	 * 
	 * @param communityIndex The id of the community
	 * @param property The property of the communtiy
	 * @return the property value
	 */
	public double getCommunityProperty(int communityIndex, GraphProperty property) {
		Community community = communities.get(communityIndex);
				
		return community.getProperty(property);		
	}
	
	/**
	 * Returns the indices of all nodes that have a belonging to the community
	 *
	 * @param communityIndex the index of the community
	 * @return member indices list
	 */
	public List<Integer> getCommunityMemberIndices(int communityIndex) {
		return communities.get(communityIndex).getMemberIndices();
	}

	/**
	 * Returns the indices of the communities that a node is member of.
	 * 
	 * @param node
	 *            The node.
	 * @return The community indices.
	 */
	public List<Integer> getCommunityIndices(Node node) {
		List<Integer> communityIndices = new ArrayList<Integer>();
		for (int j = 0; j < communities.size(); j++) {
			if (this.communities.get(j).getBelongingFactor(node) > 0) {
				communityIndices.add(j);
			}
		}
		return communityIndices;
	}

	/**
	 * Getter for the belonging factor / membership degree of a node for a
	 * certain community.
	 * 
	 * @param node
	 *            The node.
	 * @param communityIndex
	 *            The community index.
	 * @return The belonging factor.
	 */
	public double getBelongingFactor(Node node, int communityIndex) {
		return communities.get(communityIndex).getBelongingFactor(node);
	}

	/**
	 * Get the community structure of a cover, i.e. the number of communities of
	 * a certain size YLi
	 * @return a map of community ids and structures
	 */
	public Map<Integer, Integer> getCommunityStructure() {
		Map<Integer, Integer> communityStructure = new HashMap<Integer, Integer>();
		for (int i = 0; i < communities.size(); i++) {
			int size = communities.get(i).getSize();
			if (communityStructure.keySet().contains(size)) {
				communityStructure.put(size, communityStructure.get(size) + 1);
			} else {
				communityStructure.put(size, 1);
			}
		}
		return communityStructure;
	}

	/**
	 * Removes all empty communities from the graph. A community is considered
	 * to be empty when it does not have any members, i.e. the corresponding
	 * belonging factor equals 0 for each node.
	 */
	protected void removeEmptyCommunities() {
		Iterator<Community> it = communities.iterator();
		while (it.hasNext()) {
			Community community = it.next();
			if (community.getSize() == 0) {
				it.remove();
			}
		}
	}	
	
	/**
	 * Initializes the properties of all communities of this cover.
	 */
	public void initCommunityProperties() throws InterruptedException {
		for(Community community: getCommunities()) {
			CustomGraph subGraph = getGraph().getSubGraph(community.getMemberIndices());
			community.setProperties(GraphProperty.getPropertyList(subGraph));
		}
	}

	////////////////////// MEMBERSHIP MATRIX OPERATIONS //////////////////////

	/**
	 * Normalizes each row of a matrix using the one norm. Note that a unit
	 * vector column is added for each row that is equal to zero to create a
	 * separate node community.
	 * 
	 * @param matrix
	 *            The memberships matrix to be normalized and set.
	 * @return The normalized membership matrix.
	 */
	protected Matrix normalizeMembershipMatrix(Matrix matrix) {
		List<Integer> zeroRowIndices = new ArrayList<Integer>();
		for (int i = 0; i < matrix.rows(); i++) {
			Vector row = matrix.getRow(i);
			double norm = row.fold(Vectors.mkManhattanNormAccumulator());
			if (norm != 0) {
				row = row.divide(norm);
				matrix.setRow(i, row);
			} else {
				zeroRowIndices.add(i);
			}
		}
		/*
		 * Resizing also rows is required in case there are zero columns.
		 */
		matrix = matrix.resize(graph.getNodeCount(), matrix.columns() + zeroRowIndices.size());
		for (int i = 0; i < zeroRowIndices.size(); i++) {
			matrix.set(zeroRowIndices.get(i), matrix.columns() - zeroRowIndices.size() + i, 1d);
		}
		return matrix;
	}

	/**
	 * Filters the cover membership matrix by removing insignificant membership
	 * values. The cover is then normalized and empty communities are removed.
	 * All metric results besides the execution time are removed as well.
	 * 
	 * @param threshold
	 *            A threshold value, all entries below the threshold will be set
	 *            to 0, unless they are the maximum belonging factor of the
	 *            node.
	 */
	public void filterMembershipsbyThreshold(double threshold) {
		Matrix memberships = this.getMemberships();
		for (int i = 0; i < memberships.rows(); i++) {
			setRowEntriesBelowThresholdToZero(memberships, i, threshold);
		}
		this.setMemberships(memberships, true);
		removeEmptyCommunities();
	}

	/**
	 * Filters a matrix row by setting all entries which are lower than a
	 * threshold value and the row's max entry to zero.
	 * 
	 * @param matrix
	 *            The matrix.
	 * @param rowIndex
	 *            The index of the row to filter.
	 * @param threshold
	 *            The threshold.
	 */
	protected void setRowEntriesBelowThresholdToZero(Matrix matrix, int rowIndex, double threshold) {
		Vector row = matrix.getRow(rowIndex);
		double rowThreshold = Math.min(row.fold(Vectors.mkMaxAccumulator()), threshold);
		BelowThresholdEntriesVectorProcedure procedure = new BelowThresholdEntriesVectorProcedure(rowThreshold);
		row.eachNonZero(procedure);
		List<Integer> belowThresholdEntries = procedure.getBelowThresholdEntries();
		for (int i : belowThresholdEntries) {
			row.set(i, 0);
		}
		matrix.setRow(rowIndex, row);
	}
	
	//persistence functions
	public void persist(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		if(this.graph == null) {
			throw new IllegalArgumentException("graph attribute of the cover to be persisted does not exist");
		}
		else if(this.graph.getKey().equals("")) {
			throw new IllegalArgumentException("the graph of the cover is not persisted yet");
		}
		bd.addAttribute(graphKeyColumnName, this.graph.getKey());
		bd.addAttribute(nameColumnName, this.name);
		bd.addAttribute(simCostsColumnName, this.simCosts);
		bd.addAttribute(numberOfCommunitiesColumnName, this.numberOfCommunities);
		
		this.creationMethod.persist(db, transId);
		bd.addAttribute(creationMethodKeyColumnName, this.creationMethod.getKey());
		collection.insertDocument(bd, createOptions);
		this.key = bd.getKey();
		
		bd = new BaseDocument();
		List<String> communityKeyList = new ArrayList<String>();
		for(Community c : this.communities) {
			c.persist(db, createOptions);
			communityKeyList.add(c.getKey());
		}
		
		bd.addAttribute(communityKeysColumnName, communityKeyList);
		for(OcdMetricLog oml : this.metrics) {
			oml.persist(db, createOptions);
		}
		collection.updateDocument(this.key, bd, updateOptions);
	}
	
	public void updateDB(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		ArangoCollection communityCollection = db.collection(Community.collectionName);
		ObjectMapper om = new ObjectMapper();
		
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		DocumentReadOptions readOptions = new DocumentReadOptions().streamTransactionId(transId);
		DocumentDeleteOptions deleteOpt = new DocumentDeleteOptions().streamTransactionId(transId);
		
		BaseDocument bd = collection.getDocument(this.key, BaseDocument.class, readOptions);
		
		if(this.graph == null) {
			throw new IllegalArgumentException("graph attribute of the cover to be updated does not exist");
		}
		else if(this.graph.getKey().equals("")) {
			throw new IllegalArgumentException("the graph of the cover is not persisted yet");
		}
		bd.updateAttribute(nameColumnName, this.name);
		bd.updateAttribute(simCostsColumnName, this.simCosts);
		this.creationMethod.updateDB(db, transId);
		
		Object objCommunityKeys = bd.getAttribute(communityKeysColumnName);
		List<String> communityKeys = om.convertValue(objCommunityKeys, List.class);
		for(String communityKey : communityKeys) {			//delete all communitys
			communityCollection.deleteDocument(communityKey, null, deleteOpt);
		}		
		
		List<String> communityKeyList = new ArrayList<String>();
		for(Community c : this.communities) {		//add new communities
			c.persist(db, createOptions);
			communityKeyList.add(c.getKey());
		}	
		bd.updateAttribute(communityKeysColumnName, communityKeyList);
		bd.addAttribute(numberOfCommunitiesColumnName, this.numberOfCommunities);
			
		for(OcdMetricLog oml : this.metrics) {		//updates or persists a metric depending on its current existence
			if(oml.getKey().equals("")) {
				oml.persist(db, createOptions);
			}
			else {
				oml.updateDB(db, transId);
			}
		}
		collection.updateDocument(this.key, bd, updateOptions);
	}
	
	public static Cover load(String key, CustomGraph g, ArangoDatabase db, String transId) {
		
		Cover cover = null;
		ArangoCollection collection = db.collection(collectionName);
		DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
		AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);
		
		if (bd != null) {
			cover = new Cover(g);
			ObjectMapper om = new ObjectMapper();	//prepair attributes
			String graphKey = bd.getAttribute(graphKeyColumnName).toString();
			if(!graphKey.equals(g.getKey())) {
				System.out.println("graph with key: " + g.getKey() + " does not fit to cover with GraphKey: " + graphKey);
				return null;
			}
			String creationMethodKey = bd.getAttribute(creationMethodKeyColumnName).toString();
			Object objCommunityKeys = bd.getAttribute(communityKeysColumnName);
			List<String> communityKeys = om.convertValue(objCommunityKeys, List.class);
			Object objSimCost = bd.getAttribute(simCostsColumnName);
			
			//restore all attributes
			cover.key = key;
			cover.name = bd.getAttribute(nameColumnName).toString();
			cover.creationMethod = CoverCreationLog.load(creationMethodKey, db, readOpt);
			for(String communityKey : communityKeys) {
				Community community = Community.load(communityKey,  cover, db, readOpt);
				cover.communities.add(community);
			}
			cover.numberOfCommunities = (Integer) bd.getAttribute(numberOfCommunitiesColumnName);
			
			String queryStr = "FOR m IN " + OcdMetricLog.collectionName + " FILTER m." + OcdMetricLog.coverKeyColumnName +
					" == @cKey RETURN m._key";
			Map<String, Object> bindVars = Collections.singletonMap("cKey", key);
			ArangoCursor<String> metricKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			
			for(String metricKey : metricKeys) {
				OcdMetricLog oml = OcdMetricLog.load(metricKey, cover, db, readOpt);
				cover.metrics.add(oml);
			}
			if(objSimCost != null) {
				cover.simCosts = Double.parseDouble(objSimCost.toString());
			}
		}	
		else {
			System.out.println("empty Cover document");
		}
		return cover;
	}	

	@Override
	public String toString() {
		String coverString = "Cover: " + getName() + "\n";
		coverString += "Graph: " + getGraph().getName() + "\n";
		coverString += "Algorithm: " + getCreationMethod().getType().toString() + "\n" + "params:" + "\n";
		for (Map.Entry<String, String> entry : getCreationMethod().getParameters().entrySet()) {
			coverString += entry.getKey() + " = " + entry.getValue() + "\n";
		}
		coverString += "Community Count: " + communityCount() + "\n";
		OcdMetricLog metric;
		for (int i = 0; i < metrics.size(); i++) {
			metric = metrics.get(i);
			coverString += metric.getType().toString() + " = ";
			coverString += metric.getValue() + "\n" + "params:" + "\n";
			for (Map.Entry<String, String> entry : metric.getParameters().entrySet()) {
				coverString += entry.getKey() + " = " + entry.getValue() + "\n";
			}
			coverString += "\n";
		}
		coverString += "Membership Matrix\n";
		coverString += getMemberships().toString();
		return coverString;
	}

}
