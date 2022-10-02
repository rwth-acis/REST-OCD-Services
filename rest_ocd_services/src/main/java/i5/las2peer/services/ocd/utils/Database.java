package i5.las2peer.services.ocd.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.graphs.*;


import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.DbName;
import com.arangodb.ArangoCollection;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.ArangoCursor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;


import org.apache.commons.io.FileUtils;


public class Database {
	
	/**
	 * l2p logger
	 */
	private final static L2pLogger logger = L2pLogger.getInstance(Database.class.getName());
	
	private static final DatabaseConfig DBC = new DatabaseConfig();
	private static String HOST;
	private static int PORT;
	private static String USER;
	private static String PASSWORD;
	public static String DBNAME_STRING;
	private static DbName DBNAME;
	private ArangoDB arangoDB;
	public ArangoDatabase db;
	
	private List<String> collectionNames =new ArrayList<String>(10);
	
	
	public Database() {
		Properties props = DBC.getConfigProperties();
		HOST = props.getProperty("HOST");
		String port = props.getProperty("PORT");
		PORT = Integer.parseInt(props.getProperty("PORT"));
		USER = props.getProperty("USER");
		PASSWORD = props.getProperty("PASSWORD");
		DBNAME_STRING = props.getProperty("DATABASENAME");
		DBNAME = DbName.of(DBNAME_STRING);
		
		arangoDB = new ArangoDB.Builder().host(HOST, PORT).password(PASSWORD).serializer(new ArangoJack()).build();
		db = arangoDB.db(DBNAME);
		init();
	}
	
	public void init() {	
		createDatabase();
		createCollections();
	}
	
	public void createDatabase() {
		if(!db.exists()) {
			System.out.println("Creating database...");
			db.create();
			System.out.println("Datenbank erfolgreich erstellt");
		}
	}
	
	public void deleteDatabase() {
		if(db.exists()) {
			db.drop();
			System.out.println("The database " + db.dbName() + " was deleted");
		}	
		else {
			System.out.println("No database was deleted");
		}
	}
	
	public void createCollections() {
		ArangoCollection collection;
		collectionNames.add(CustomGraph.collectionName);		//0
		collection = db.collection(CustomGraph.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CustomGraph.collectionName + " erstellt");
		}
		collectionNames.add(CustomNode.collectionName);			//1
		collection = db.collection(CustomNode.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CustomNode.collectionName + " erstellt");
		}
		collectionNames.add(CustomEdge.collectionName);			//2
		collection = db.collection(CustomEdge.collectionName);
		if(!collection.exists()) {
			db.createCollection(CustomEdge.collectionName, new CollectionCreateOptions().type(CollectionType.EDGES));
			System.out.println(CustomEdge.collectionName + " erstellt");
		}
		collectionNames.add(GraphCreationLog.collectionName);	//3
		collection = db.collection(GraphCreationLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(GraphCreationLog.collectionName + " erstellt");
		}
		
		collectionNames.add(Cover.collectionName);				//4
		collection = db.collection(Cover.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(Cover.collectionName + " erstellt");
		}
		collectionNames.add(CoverCreationLog.collectionName);	//5
		collection = db.collection(CoverCreationLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CoverCreationLog.collectionName + " erstellt");
		}
		collectionNames.add(OcdMetricLog.collectionName);		//6
		collection = db.collection(OcdMetricLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(OcdMetricLog.collectionName + " erstellt");
		}
		collectionNames.add(Community.collectionName);			//7
		collection = db.collection(Community.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(Community.collectionName + " erstellt");
		}
		
		collectionNames.add(CentralityMap.collectionName);		//8
		collection = db.collection(CentralityMap.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CentralityMap.collectionName + " erstellt");
		}
		collectionNames.add(CentralityCreationLog.collectionName);//9
		collection = db.collection(CentralityCreationLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CentralityCreationLog.collectionName + " erstellt");
		}
		
	}
	

	
	
	/////////////////////////// GRAPHS ///////////////////////////

	/**
	 * Persists a CustomGraph
	 * 
	 * @param graph
	 *            CustomGraph
	 * @return persistence key of the stored graph
	 */
	public String storeGraph(CustomGraph graph) {
		String transId = getTransactionId(CustomGraph.class, true);
		try {
			graph.persist(db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return graph.getKey();
	}
	
	public CustomGraph getGraph(String key) {
		String transId = getTransactionId(CustomGraph.class, false);
		CustomGraph graph;
		try {	
			graph = CustomGraph.load(key, db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return graph;
	}
	
	/**
	 * Returns a persisted CustomGraph if it has the right username
	 * 
	 * @param username
	 *            owner of the graph
	 * @param key
	 *            key of the graph
	 * @return the found CustomGraph instance or null if the CustomGraph does not exists or the username is wrong
	 */
	public CustomGraph getGraph(String username, String key) {
		CustomGraph g = getGraph(key);
		if (g == null) {
			logger.log(Level.WARNING, "user: " + username + " Graph does not exist: graph key " + key);
		}
		else if(!username.equals(g.getUserName())) {
			logger.log(Level.WARNING, "user: " + username + " is not allowed to use Graph: " + key + " with user: " + g.getUserName());
			g = null;
		}
		return g;
	}
	
	/**
	 * Return all graphs of a user
	 * 
	 * @param username
	 *            graphs owner
	 * @return graph list
	 */
	public List<CustomGraph> getGraphs(String username) {
		String transId = getTransactionId(CustomGraph.class, false);
		List<CustomGraph> queryResults = new ArrayList<CustomGraph>();
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			String queryStr = "FOR g IN " + CustomGraph.collectionName + " FILTER g." + CustomGraph.userColumnName + " == @username RETURN g._key";
			Map<String, Object> bindVars = Collections.singletonMap("username",username);
			ArangoCursor<String> graphKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			for(String key : graphKeys) {
				queryResults.add(CustomGraph.load(key,  db, transId));
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}

		return queryResults;
	}
	
	/**
	 * Return a list of specific graphs of a user
	 * 
	 * @param username
	 * 			  the users username
	 * @param firstIndex
	 *            id of the first graph
	 * @param length
	 *            number of graphs
	 * @param executionStatusIds
	 * 			  the execution status ids of the graphs
	 * @return the list of graphs
	 */	
	public List<CustomGraph> getGraphs(String username, int firstIndex, int length, List<Integer> executionStatusIds) {
		String transId = getTransactionId(CustomGraph.class, false);
		List<CustomGraph> queryResults = new ArrayList<CustomGraph>();
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			String queryStr = "FOR g IN " + CustomGraph.collectionName + " FOR gcl IN " + GraphCreationLog.collectionName +
					" FILTER g." + CustomGraph.userColumnName + " == @username AND gcl._key == g." + CustomGraph.creationMethodKeyColumnName +
					" AND gcl." + GraphCreationLog.statusIdColumnName +" IN " + 
					executionStatusIds + " LIMIT " + firstIndex + "," + length + " RETURN g._key";
			
			Map<String, Object> bindVars = Collections.singletonMap("username",username);
			ArangoCursor<String> graphKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			for(String key : graphKeys) {
				queryResults.add(CustomGraph.load(key,  db, transId));
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return queryResults;
	}
	
	/**
	 * Return all graphs with the right name
	 * 
	 * @param name
	 *            graphs name
	 * @return graph list
	 */
	public List<CustomGraph> getGraphsbyName(String name) {
		String transId = getTransactionId(CustomGraph.class, false);
		List<CustomGraph> queryResults = new ArrayList<CustomGraph>();
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			String queryStr = "FOR g IN " + CustomGraph.collectionName + " FILTER g." + CustomGraph.nameColumnName + " == @name RETURN g._key";
			Map<String, Object> bindVars = Collections.singletonMap("name",name);
			ArangoCursor<String> graphKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			for(String key : graphKeys) {
				queryResults.add(CustomGraph.load(key,  db, transId));
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}

		return queryResults;
	}	

	/**
	 * Updates a persisted graph by updating Attributes,nodes,edges and creationMethod
	 * does NOT update changes in the covers or CentralityMaps that run on the given graph
	 * 
	 * @param graph
	 *            the graph
	 */	
	public void updateGraph(CustomGraph graph) {	//existenz des graphen muss bereits herausgefunden worden sein TESTEN
		String transId = this.getTransactionId(CustomGraph.class, true);
		try {
			graph.updateDB(db, transId);
			db.commitStreamTransaction(transId);
		} catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	public void deleteGraph(String key) {
		String [] writeCollections = collectionNames.subList(0, 10).toArray(new String[10]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(writeCollections));
		String transId = tx.getId();
		
		try {	
			ArangoCollection graphCollection = db.collection(CustomGraph.collectionName);
			DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
			DocumentDeleteOptions deleteOpt = new DocumentDeleteOptions().streamTransactionId(transId);
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			
			BaseDocument bd = graphCollection.getDocument(key, BaseDocument.class, readOpt);
			String gclKey = bd.getAttribute(CustomGraph.creationMethodKeyColumnName).toString();
			
			ArangoCollection gclCollection = db.collection(GraphCreationLog.collectionName);
			gclCollection.deleteDocument(gclKey, null, deleteOpt);		//delete the GraphCreationLog
			String query = "FOR n IN " + CustomNode.collectionName + " FILTER n." +CustomNode.graphKeyColumnName 
					+ " == \"" + key +"\" REMOVE n IN " + CustomNode.collectionName + " RETURN OLD";
			db.query(query, queryOpt, BaseDocument.class);				//delete all nodes
			
			query = "FOR e IN " + CustomEdge.collectionName + " FILTER e." + CustomEdge.graphKeyColumnName 
					+ " == \"" + key +"\" REMOVE e IN " + CustomEdge.collectionName + " RETURN OLD";
			db.query(query, queryOpt, BaseDocument.class);				//delete all edges
			
			
			query = "FOR c IN " + Cover.collectionName + " FILTER c." + Cover.graphKeyColumnName 
					+ " == \"" + key +"\" RETURN c._key";
			ArangoCursor<String> coverKeys = db.query(query, queryOpt, String.class);
			for(String coverKey : coverKeys) {							//delete all covers
				deleteCover(coverKey);
			}
			query = "FOR cm IN " + CentralityMap.collectionName + " FILTER cm." + CentralityMap.graphKeyColumnName 
					+ " == \"" + key +"\" RETURN cm._key";
			ArangoCursor<String> centralityMapKeys = db.query(query, queryOpt, String.class);
			for(String centralityMapKey : centralityMapKeys) {			//delete all centrality Maps
				deleteCentralityMap(centralityMapKey);
			}
			
			graphCollection.deleteDocument(key, null, deleteOpt);		//delete the graph
			
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
	}

	
	/**
	 * Deletes a CustomGraph from the database
	 * 
	 * @param username
	 *            owner of the graph
	 * @param graphKey
	 *            key of the graph
	 * @param threadHandler
	 * 			  the threadhandler
	 * @throws Exception if cover deletion failed
	 */
	public void deleteGraph(String username, String graphKey, ThreadHandler threadHandler) throws Exception {	//TODO

		CustomGraphId id = new CustomGraphId(graphKey, username);

		synchronized (threadHandler) {
			threadHandler.interruptBenchmark(id);

			List<Cover> coverList = getCovers(username, graphKey);
			for (Cover cover : coverList) {
				try {
					deleteCover(cover, threadHandler);
				} catch (Exception e) {
					throw e;
				}
			}
			
			List<CentralityMap> centralityMapList = getCentralityMaps(username, graphKey);
			for (CentralityMap map : centralityMapList) {
				try {
					deleteCentralityMap(map, threadHandler);
				} catch (Exception e) {
					throw e;
				}
			}
			
			try {
				CustomGraph graph = getGraph(username, graphKey);
				if(graph.getPath() != "" && graph.getPath() != null) { // Delete index folder if graph is content graph
				    File file = new File(graph.getPath());
				    FileUtils.deleteDirectory(file);
				}
				deleteGraph(graphKey);

			} catch (IOException e) {		
				throw new RuntimeException("Could not delete folder of content graph");
			} catch(Exception e) {
				throw e;
			}
		}

	}
	
	
	/////////////////////////// COVERS ///////////////////////////
	public String storeCover(Cover cover) {
		String transId = this.getTransactionId(Cover.class, true);
		try {
			cover.persist(db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return cover.getKey();
	}

	public Cover getCover(String key, CustomGraph g) {
		String transId = this.getTransactionId(Cover.class, false);
		Cover cover;
		try {	
			cover = Cover.load(key, g, db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return cover;
	}
	
	/**
	 * Get a stored community-cover of a graph by its coverKey
	 *
	 * @param username
	 * 			  the name of the user
	 * @param graphKey
	 *            key of the graph
	 * @param coverKey
	 *            key of the cover
	 * @return the found Cover instance or null if the Cover does not exist
	 */
	public Cover getCover(String username, String graphKey, String coverKey) {
		CustomGraph graph = getGraph(username, graphKey);
		Cover cover = null;
		if(!(graph == null)) {
			cover = getCover(coverKey, graph);
		}
		if (cover == null) {
			logger.log(Level.WARNING,
					"user: " + username + ", " + "Cover does not exist: cover id " + coverKey + ", graph id " + graphKey);
		}
		return cover;
	}
	
	public List<Cover> getCoversByName(String name, CustomGraph g){
		String transId = getTransactionId(Cover.class, false);
		List<Cover> queryResults = new ArrayList<Cover>();
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			String queryStr = "FOR c IN " + Cover.collectionName + " FILTER c." + Cover.nameColumnName + " == @name RETURN c._key";
			Map<String, Object> bindVars = Collections.singletonMap("name",name);
			ArangoCursor<String> coverKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			for(String key : coverKeys) {
				queryResults.add(Cover.load(key, g,  db, transId));
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}

		return queryResults;
	}
	
	/**
	 * Returns all Covers corresponding to a CustomGraph
	 * 
	 * @param username
	 *            owner of the graph
	 * @param graphKey
	 *            key of the graph
	 * @return cover list
	 */
	public List<Cover> getCovers(String username, String graphKey) {	//TODO testen
		CustomGraph g = getGraph(username, graphKey);
		String transId = getTransactionId(Cover.class, false);
		List<Cover> covers = new ArrayList<Cover>();
		if(g == null) {
			return covers;
		}
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			String queryStr = "FOR c IN " + Cover.collectionName + " FILTER c." + Cover.graphKeyColumnName + " == @key RETURN c._key";
			Map<String, Object> bindVars = Collections.singletonMap("key", graphKey);
			ArangoCursor<String> coverKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			for(String key : coverKeys) {
				Cover cover = Cover.load(key, g,  db, transId);
				if(cover!= null) {
					covers.add(cover);
				}	
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return covers;
	}
	
	/**
	 * @param username
	 * 		  the name of the user
	 * @param graphKey
	 * 		  the key of the graph
	 * @param executionStatusIds
	 * 		  the ids of the execution statuses
	 * @param metricExecutionStatusIds
	 * 		  the ids of the metric execution statuses
	 * @param firstIndex
	 * 		  the first index
	 * @param length
	 * 		  the length of the result set
	 * @return a cover list
	 */
	public List<Cover> getCovers(String username, String graphKey, List<Integer> executionStatusIds,
			List<Integer> metricExecutionStatusIds, int firstIndex, int length) {	//TODO was soll die funktion überhaupt machen?/ TESTEN
		
		String transId = getTransactionId(null, false);
		List<Cover> covers = new ArrayList<Cover>();
		Map<String, CustomGraph> graphMap = new HashMap<String, CustomGraph>();
		Set<String> graphKeySet = new HashSet<String>();
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			Map<String, Object> bindVars;
			
			String queryStr = " FOR c IN " + Cover.collectionName + " FOR a IN " + CoverCreationLog.collectionName + " FOR m IN " + OcdMetricLog.collectionName + 
					" FILTER c." + Cover.creationMethodKeyColumnName + " == a._key AND a." + CoverCreationLog.statusIdColumnName + " IN " + executionStatusIds;
			if (metricExecutionStatusIds != null && metricExecutionStatusIds.size() > 0) {
				queryStr += " AND m." + OcdMetricLog.coverKeyColumnName + " == c._key AND " +"m." + OcdMetricLog.statusIdColumnName + " IN " + metricExecutionStatusIds;
			}
			if(!graphKey.equals("")) {		//es gibt einen graphKey
				queryStr += "AND c." + Cover.graphKeyColumnName  + " == @gKey";
				bindVars = Collections.singletonMap("gKey", graphKey);
			}
			else {			//es gibt keinen graphKey
				queryStr += "FOR g IN " + CustomGraph.collectionName + 
						"FILTER g." + CustomGraph.userColumnName + " == @user AND c." + Cover.graphKeyColumnName + " == g._key";
				 bindVars = Collections.singletonMap("user", username);
			}
			queryStr += " LIMIT " + firstIndex + "," + length + " RETURN DISTINCT c";
			
			ArangoCursor<BaseDocument> coverDocs = db.query(queryStr, bindVars, queryOpt, BaseDocument.class);
			//alle graphkeys in set einfügen
			for(BaseDocument bd : coverDocs) {
				String gKey = bd.getAttribute(Cover.graphKeyColumnName).toString();
				graphKeySet.add(gKey);
			}
			if(graphKeySet.size()==1) {
				CustomGraph g = CustomGraph.load(graphKeySet.iterator().next(), db, transId);
				if(username.equals(g.getUserName())) {
					for(BaseDocument bd : coverDocs) {
						String cKey = bd.getKey();
						covers.add(Cover.load(cKey, g, db, transId));
					}
				}	
			}
			else {	//cover mit zugehörigem graph laden
				for(String gKey : graphKeySet) {
					graphMap.put(gKey, getGraph(gKey));
				}
				for(BaseDocument bd : coverDocs) {
					String cKey = bd.getKey();
					String gKey = bd.getAttribute(Cover.graphKeyColumnName).toString();
					CustomGraph g = graphMap.get(gKey);
					covers.add(Cover.load(cKey, g, db, transId));
				}
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return covers;
	}
	/**
	 * Updates a persisted cover by first deleting the current one,
	 * then persisting the new one,
	 * then updating the key of the cover to the old one.
	 * 
	 * @param cover
	 *            the cover
	 */	
	public void updateCover(Cover cover) {	//existenz des covers muss bereits herausgefunden worden sein TESTEN
		String transId = this.getTransactionId(Cover.class, true);
		String aktualKey = cover.getKey();
		try {
			deleteCover(aktualKey);
			cover.persist(db, transId);
			cover.updateKey(aktualKey, db, transId);
			db.commitStreamTransaction(transId);
		} catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	public void deleteCover(String key) {
		String transId = this.getTransactionId(Cover.class, true);
		try {
			
			ArangoCollection coverCollection = db.collection(Cover.collectionName);
			ArangoCollection cclCollection = db.collection(CoverCreationLog.collectionName);
			
			DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
			DocumentDeleteOptions deleteOpt = new DocumentDeleteOptions().streamTransactionId(transId);
			BaseDocument coverDoc = coverCollection.getDocument(key, BaseDocument.class, readOpt);
				
			ObjectMapper om = new ObjectMapper();
			Object objCommunityKeys = coverDoc.getAttribute(Cover.communityKeysColumnName);
			List<String> communityKeys = om.convertValue(objCommunityKeys, List.class);
			for(String communityKey : communityKeys) {			//delete all communitys
				ArangoCollection communityCollection = db.collection(Community.collectionName);
				communityCollection.deleteDocument(communityKey, null, deleteOpt);
			}
			
			Object objMetricKeys = coverDoc.getAttribute(Cover.metricKeysColumnName);
			List<String> metricKeys = om.convertValue(objMetricKeys, List.class);
			for(String metricKey : metricKeys) {			//delete all metric logs
				ArangoCollection metricCollection = db.collection(OcdMetricLog.collectionName);
				metricCollection.deleteDocument(metricKey, null, deleteOpt);
			}
			
			String creationMethodKey = coverDoc.getAttribute(Cover.creationMethodKeyColumnName).toString();
			cclCollection.deleteDocument(creationMethodKey, null, deleteOpt);	//delete CoverCreationLog
			coverCollection.deleteDocument(key, null, deleteOpt);				//delete Cover
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	/**
	 * Deletes a persisted cover from the database
	 * 
	 * @param cover
	 *            the cover
	 * @param threadHandler
	 * 			  the threadhandler
	 */
	public void deleteCover(Cover cover, ThreadHandler threadHandler) {		//TODO tests

		synchronized (threadHandler) {
			threadHandler.interruptAll(cover);
			deleteCover(cover.getKey());
		}
	}
	
	/**
	 * Deletes a persisted cover from the database
	 * 
	 * Checks whether cover is being calculated by a ground truth benchmark and
	 * if so deletes the graph instead.
	 * 
	 * @param username
	 *            owner of the cover
	 * @param graphKey
	 *            key of the graph
	 * @param coverKey
	 *            key of the cover
	 * @param threadHandler
	 * 			  the thread handler
	 * @throws IllegalArgumentException
	 *             cover does not exist
	 * @throws Exception
	 * 			   if cover deletion failed
	 */
	public void deleteCover(String username, String graphKey, String coverKey, ThreadHandler threadHandler) throws Exception {	//TODO tests

		Cover cover = getCover(username, graphKey, coverKey);
		if (cover == null)
			throw new IllegalArgumentException("Cover not found");

		if (cover.getCreationMethod().getType().correspondsGroundTruthBenchmark()
				&& cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {

			this.deleteGraph(username, graphKey, threadHandler);		//TODO
		}
		this.deleteCover(cover, threadHandler);		// TODO muss übersprungen werden im else fall?
	}
	
	/////////////////////////// CENTRALITY MAPS ///////////////////////////

	
	public String storeCentralityMap(CentralityMap map) {
		String transId = this.getTransactionId(CentralityMap.class, true);
		try {
			map.persist(db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return map.getKey();
	}	
	
	public CentralityMap getCentralityMap(String key, CustomGraph g) {
		String transId = this.getTransactionId(CentralityMap.class, false);
		CentralityMap map;
		try {	
			map = CentralityMap.load(key, g, db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return map;
	}
	
	
	/**
	 * Get a stored community-cover of a graph by its index
	 *
	 * @param username
	 * 			  the name of the user
	 * @param graphKey
	 *            key of the graph
	 * @param mapKey
	 *            key of the centrality map
	 * @return the found Cover instance or null if the Cover does not exist
	 */
	public CentralityMap getCentralityMap(String username, String graphKey, String mapKey) {
		CustomGraph graph = getGraph(username, graphKey);
		CentralityMap map = null;
		if(!(graph == null)) {
			map = getCentralityMap(mapKey, graph);
		}
		if (map == null) {
			logger.log(Level.WARNING,
					"user: " + username + ", " + "CentralityMap does not exist: map key " + mapKey + ", graph key " + graphKey);
		}
		return map;
	}

	/**
	 * Returns all centrality maps corresponding to a CustomGraph
	 * 
	 * @param username
	 *            Owner of the graph
	 * @param graphKey
	 *            key of the graph
	 * @return A list of the corresponding centrality maps
	 */
	public List<CentralityMap> getCentralityMaps(String username, String graphKey) {
		CustomGraph g = getGraph(username, graphKey);
		String transId = getTransactionId(CentralityMap.class, false);
		List<CentralityMap> maps = new ArrayList<CentralityMap>();
		if(g == null) {
			return maps;
		}
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			String queryStr = "FOR c IN " + CentralityMap.collectionName + " FILTER c." + CentralityMap.graphKeyColumnName + " == @key RETURN c._key";
			Map<String, Object> bindVars = Collections.singletonMap("key", graphKey);
			ArangoCursor<String> mapKeys = db.query(queryStr, bindVars, queryOpt, String.class);
			for(String key : mapKeys) {
				CentralityMap map = CentralityMap.load(key, g,  db, transId);
				maps.add(map);
			}
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return maps;
	}
	
	/**
	 * @param username
	 * 		  the name of the user
	 * @param graphKey
	 * 		  the key of the graph
	 * @param executionStatusIds
	 * 		  the ids of the execution statuses
	 * @param firstIndex
	 * 		  the first index
	 * @param length
	 * 		  the length of the result set
	 * @return a centralityMap list
	 */
	public List<CentralityMap> getCentralityMaps(String username, String graphKey, List<Integer> executionStatusIds, int firstIndex, int length) {

		String transId = getTransactionId(null, false);
		List<CentralityMap> maps = new ArrayList<CentralityMap>();
		Map<String, CustomGraph> graphMap = new HashMap<String, CustomGraph>();
		Set<String> graphKeySet = new HashSet<String>();
		try {
			AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
			Map<String, Object> bindVars = Collections.singletonMap("user", username );
			
			String queryStr = " FOR c IN " + CentralityMap.collectionName + " FOR a IN " + CentralityCreationLog.collectionName + " FOR g IN " + CustomGraph.collectionName + 
					" FILTER g." + CustomGraph.userColumnName + " == @user AND c." + CentralityMap.graphKeyColumnName + " == g._key AND c." + 
					CentralityMap.creationMethodKeyColumnName + " == a._key AND a." + CentralityCreationLog.statusIdColumnName + " IN " + executionStatusIds;
			
			if(!graphKey.equals("")) {		
				queryStr += " AND g._key == @gKey " ;
				bindVars.put("gKey", graphKey);
			}
			
			queryStr += " LIMIT " + firstIndex + "," + length + " RETURN DISTINCT c";		//get each map only once
			ArangoCursor<BaseDocument> cmDocs = db.query(queryStr, bindVars, queryOpt, BaseDocument.class);
			
			for(BaseDocument bd : cmDocs) {
				String gKey = bd.getAttribute(CentralityMap.graphKeyColumnName).toString();
				graphKeySet.add(gKey);
			}
			//cm mit zugehörigem graph laden
			for(String gKey : graphKeySet) {
				graphMap.put(gKey, getGraph(gKey));
			}
			for(BaseDocument bd : cmDocs) {
				String cKey = bd.getKey();
				String gKey = bd.getAttribute(CentralityMap.graphKeyColumnName).toString();
				CustomGraph g = graphMap.get(gKey);
				maps.add(CentralityMap.load(cKey, g, db, transId));
			}
			
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return maps;
	}
	/**
	 * Updates a persisted centralityMap by first deleting the current one,
	 * then persisting the new one,
	 * then updating the key of the cover to the old one.
	 * 
	 * @param map
	 *            the centralityMap
	 */	
	public void updateCentralityMap(CentralityMap map) {	//existenz der map muss bereits herausgefunden worden sein TESTEN
		String transId = this.getTransactionId(CentralityMap.class, true);
		String aktualKey = map.getKey();
		try {
			deleteCentralityMap(map.getKey());
			map.persist(db, transId);
			map.updateKey(aktualKey, db, transId);
			db.commitStreamTransaction(transId);
		} catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	public void deleteCentralityMap(String key) {
		String transId = this.getTransactionId(CentralityMap.class, true);		
		try {
			
			ArangoCollection centralityMapCollection = db.collection(CentralityMap.collectionName);
			ArangoCollection cclCollection = db.collection(CentralityCreationLog.collectionName);
			
			DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
			DocumentDeleteOptions deleteOpt = new DocumentDeleteOptions().streamTransactionId(transId);
			BaseDocument centralityMapDoc = centralityMapCollection.getDocument(key, BaseDocument.class, readOpt);
				
			String cclKey = centralityMapDoc.getAttribute(CentralityMap.creationMethodKeyColumnName).toString();
			cclCollection.deleteDocument(cclKey, null, deleteOpt);		//delete the CentralityCreationLog
	
			centralityMapCollection.deleteDocument(key, null, deleteOpt);//delete CentralityMap
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	/**
	 * Deletes a persisted CentralityMap from the database
	 * 
	 * @param map
	 *            The CentralityMap
	 * @param threadHandler
	 *            The ThreadHandler for algorithm execution
	 */
	public void deleteCentralityMap(CentralityMap map, ThreadHandler threadHandler) {
		synchronized (threadHandler) {
			threadHandler.interruptAll(map);
			this.deleteCentralityMap(map.getKey());
		}
	}
	/**
	 * Deletes a persisted CentralityMap from the database
	 * 
	 * @param username
	 *            Owner of the CentralityMap
	 * @param graphKey
	 *            Key of the graph
	 * @param mapKey
	 *            Key of the CentralityMap
	 * @param threadHandler
	 *            The ThreadHandler for algorithm execution
	 * @throws IllegalArgumentException
	 *             centrality map does not exist
	 * @throws Exception
	 * 			   if centrality map deletion failed
	 */
	public void deleteCentralityMap(String username, String graphKey, String mapKey, ThreadHandler threadHandler) throws Exception{
		CentralityMap map = getCentralityMap(username, graphKey, mapKey);		
		if (map == null) {
			throw new IllegalArgumentException("Centrality map not found");
		}
		deleteCentralityMap(map, threadHandler);
	}
	
	/////////////////////////// OCDMETRICLOG ///////////////////////////
	
	public OcdMetricLog getOcdMetricLog(String key, Cover c) {
		String transId = this.getTransactionId(OcdMetricLog.class, false);
		DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
		OcdMetricLog metric;
		try {	
			metric = OcdMetricLog.load(key, c, db, readOpt);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
		return metric;
	}
	
	public OcdMetricLog getOcdMetricLog(String username, String graphKey, String coverKey, String metricKey) {
		Cover cover = getCover(username, graphKey, coverKey);
		
		OcdMetricLog metric = null;
		if(!(cover == null)) {
			metric = getOcdMetricLog(metricKey, cover);
		}
		if (metric == null) {
			logger.log(Level.WARNING,
					"user: " + username + ", " + "OcdMetricLog does not exist: cover id " + coverKey + ", graph id " + graphKey + ", metric id " + metricKey);
		}
		return metric;
	}
	
	public OcdMetricLog getOcdMetricLog(OcdMetricLogId logId) {
    	CoverId cId = logId.getCoverId();
    	CustomGraphId gId = cId.getGraphId();
    	String user = gId.getUser();
    	String gKey = gId.getKey();
    	String cKey = cId.getKey();
    	String mKey = logId.getKey();
    	return getOcdMetricLog(user, gKey, cKey, mKey);
	}
	/**
	 * Updates a persisted OcdMetricLog by first deleting the current one,
	 * then persisting the new one,
	 * then updating the key of the cover to the old one.
	 * 
	 * @param metricLog
	 *            the OcdMetricLog
	 */	
	public void updateOcdMetricLog(OcdMetricLog metricLog) {

		String transId = this.getTransactionId(OcdMetricLog.class, true);
		String aktualKey = metricLog.getKey();
		try {
			DocumentDeleteOptions deleteOpt = new DocumentDeleteOptions().streamTransactionId(transId);
			DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);		
			ArangoCollection metricCollection = db.collection(OcdMetricLog.collectionName);
			metricCollection.deleteDocument(aktualKey, null, deleteOpt);
			metricLog.persist(db, createOptions);
			metricLog.updateKey(aktualKey, db, transId);
			db.commitStreamTransaction(transId);
		} catch(Exception e) {
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	

	

	/**
	 * Get QueryResults as list if it is not an Edge.
	 * 
	 * @param query 	A String of the query that should be executed
	 * @param queryOpt	The queryOptions mainly used for the transaction
	 * 
	 * @return a list of all results of the query
	 */
	private List<String> getQueryResultKeys(String query, Map<String, Object> bindVars, AqlQueryOptions queryOpt){	
		List<String> results = new ArrayList<String>();
		
		ArangoCursor<String> docs = db.query(query, bindVars, queryOpt, String.class);
		while (docs.hasNext()) {
			String key = docs.next();
			results.add(key);		
		}
		this.getTransactionId(Cover.class,  true);
		return  results;
	}
	
	private String getTransactionId(Class c, boolean write) {
		String [] collections;
		if(c == CustomGraph.class) {
			collections = collectionNames.subList(0, 4).toArray(new String[4]);
		}
		else if(c == Cover.class) {
			collections = collectionNames.subList(4, 8).toArray(new String[4]);
		}
		else if(c == CentralityMap.class) {
			collections = collectionNames.subList(8, 10).toArray(new String[2]);
		}
		else if(c == OcdMetricLog.class) {
			collections = collectionNames.subList(6, 7).toArray(new String[1]);
		}
		else {
			collections = collectionNames.subList(0, 10).toArray(new String[10]);
		}
		StreamTransactionEntity tx;
		if(write) {
			tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(collections));
		}else {
			tx = db.beginStreamTransaction(new StreamTransactionOptions().readCollections(collections));
		}
		return tx.getId();
	}
	
	
	private void p(String s) {	//TODO entfernen
		System.out.println(s);
	}
}
