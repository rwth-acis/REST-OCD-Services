package i5.las2peer.services.ocd.graphs;

import java.util.List;
import java.util.ArrayList;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.DbName;
import com.arangodb.ArangoCollection;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.ArangoCursor;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;


public class Database {
	
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 8529;
	private static final String USER = "root";
	private static final String PASSWORD = "password";
	private static final String DBNAME_STRING = "ocd_db";
	private static final DbName DBNAME = DbName.of(DBNAME_STRING);
	
	private List<String> collectionNames =new ArrayList<String>(10);
	
	
	
	private ArangoDB arangoDB = new ArangoDB.Builder().host(HOST, PORT).password(PASSWORD).serializer(new ArangoJack()).build();
	public ArangoDatabase db;
	
	public void init() {
		createDatabase();
		createCollections();
	}
	public void createDatabase() {
		db = arangoDB.db(DBNAME);
		if(!db.exists()) {
			System.out.println("Creating database...");
			db.create();
			System.out.println("Datenbank erfolgreich erstellt");
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
	
	public void deleteDatabase(String name) {
		DbName n = DbName.of(name);
		if(arangoDB.db(n).exists()) {
			arangoDB.db(n).drop();
			System.out.println("Datenbank gelöscht");
		}	
		else {
			System.out.println("keine db gelöscht");
		}
	}
	public void deleteDatabase() {
		this.deleteDatabase(DBNAME_STRING);
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
		String [] writeCollections = collectionNames.subList(0, 4).toArray(new String[4]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(writeCollections));
		String transId = tx.getId();
		try {
			graph.persist(db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			p("transaktion abgebrochen");
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
		return graph.getKey();
	}
	public CustomGraph getGraph(String key) {
		String [] readCollections = collectionNames.subList(0, 10).toArray(new String[10]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(readCollections));
		String transId = tx.getId();
		CustomGraph graph;
		try {	
			graph = CustomGraph.load(key, db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
		return graph;
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
			p("transaktion committed");
		}catch(Exception e) {
			p("transaktion abgebrochen");
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	
	public String storeCover(Cover cover) {
		String [] writeCollections = collectionNames.subList(4, 8).toArray(new String[4]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(writeCollections));
		String transId = tx.getId();
		try {
			cover.persist(db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
		return cover.getKey();
	}
	public Cover getCover(String key, CustomGraph g) {
		String [] readCollections = collectionNames.subList(4, 8).toArray(new String[4]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(readCollections));
		String transId = tx.getId();
		DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
		Cover cover;
		try {	
			cover = Cover.load(key, g, db, readOpt);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
		return cover;
	}
	public void deleteCover(String key) {
		
		String [] writeCollections = collectionNames.subList(4, 8).toArray(new String[4]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(writeCollections));
		String transId = tx.getId();	
		
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
			p("transaktion committed");
		}catch(Exception e) {
			p("transaktion abgebrochen");
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	public CentralityMap getCentralityMap(String key, CustomGraph g) {
		String [] readCollections = collectionNames.subList(8, 10).toArray(new String[4]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(readCollections));
		String transId = tx.getId();
		DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
		CentralityMap map;
		try {	
			map = CentralityMap.load(key, g, db, readOpt);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
		return map;
	}
	
	public String storeCentralityMap(CentralityMap map) {
		String [] writeCollections = collectionNames.subList(8, 10).toArray(new String[2]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(writeCollections));
		String transId = tx.getId();
		try {
			map.persist(db, transId);
			db.commitStreamTransaction(transId);
		}catch(Exception e) {
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
		return map.getKey();
	}
	
	public void deleteCentralityMap(String key) {
		
		String [] writeCollections = collectionNames.subList(8, 10).toArray(new String[2]);
		StreamTransactionEntity tx = db.beginStreamTransaction(new StreamTransactionOptions().writeCollections(writeCollections));
		String transId = tx.getId();	
		
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
			p("transaktion committed");
		}catch(Exception e) {
			p("transaktion abgebrochen");
			e.printStackTrace();
			db.abortStreamTransaction(transId);
			throw e;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void p(String s) {	//TODO entfernen
		System.out.println(s);
	}
}
