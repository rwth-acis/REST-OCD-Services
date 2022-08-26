package i5.las2peer.services.ocd.graphs;



import i5.las2peer.services.ocd.metrics.OcdMetricLog;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.DbName;
import com.arangodb.ArangoCollection;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.entity.CollectionEntity;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;


public class Database {
	
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 8529;
	private static final String USER = "root";
	private static final String PASSWORD = "password";
	private static final String DBNAME_STRING = "ocd_db";
	private static final DbName DBNAME = DbName.of(DBNAME_STRING);
	
	//collection names
	private static final String NODE_NAME = "node";
	
	
	private ArangoDB arangoDB = new ArangoDB.Builder().host(HOST, PORT).password(PASSWORD).serializer(new ArangoJack()).build();
	public ArangoDatabase db;

	public void createDatabase() {
		db = arangoDB.db(DBNAME);
		if(!db.exists()) {
			System.out.println("Creating database...");
			db.create();
			System.out.println("Datenbank erfolgreich erstellt");
		}
	}
	
	public void createCollections() {
		ArangoCollection collection = db.collection(GraphCreationLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(GraphCreationLog.collectionName + " erstellt");
		}
		collection = db.collection(CoverCreationLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CoverCreationLog.collectionName + " erstellt");
		}
		collection = db.collection(OcdMetricLog.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(OcdMetricLog.collectionName + " erstellt");
		}
		collection = db.collection(CustomNode.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CustomNode.collectionName + " erstellt");
		}
		
		collection = db.collection(CustomEdge.collectionName);
		if(!collection.exists()) {
			db.createCollection(CustomEdge.collectionName, new CollectionCreateOptions().type(CollectionType.EDGES));
			System.out.println(CustomEdge.collectionName + " erstellt");
		}
		
		collection = db.collection(Community.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(Community.collectionName + " erstellt");
		}
		collection = db.collection(Cover.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(Cover.collectionName + " erstellt");
		}
		collection = db.collection(CustomGraph.collectionName);
		if(!collection.exists()) {
			collection.create();
			System.out.println(CustomGraph.collectionName + " erstellt");
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
	//persist
	public void persistGraphCreationLog(GraphCreationLog log) {
		log.persist(db);
	}
	public void persistCoverCreationLog(CoverCreationLog log) {
		log.persist(db);
	}
	public void persistOcdMetricLog(OcdMetricLog log) {
		String coverKey = "coverKey";	//TODO cover ordentlich einbinden
		log.persist("cover/"+coverKey, db);
	}
	public void persistCustomNode(CustomNode cn) {
		String graphKey = "graphKey";	//TODO graph ordentlich einbinden
		cn.persist("graph/"+graphKey, db);
	}
	public void persistCommunity(Community c) {
		c.persist(db);
	}
	public void persistCover(Cover cov) {
		String graphKey = "graphKey";
		cov.persist(graphKey, db);
	}
	public void persistGraph(CustomGraph graph) {
		graph.persist(db);
	}
	
	//load
	public GraphCreationLog loadGraphCreationLog(String key) {
		GraphCreationLog gcl = GraphCreationLog.load(key,  db);
		return gcl;
	}
	public CoverCreationLog loadCoverCreationLog(String key) {
		CoverCreationLog ccl = CoverCreationLog.load(key,  db);
		return ccl;
	}
	public OcdMetricLog loadOcdMetricLog(String key, Cover cover) {
		OcdMetricLog oml = OcdMetricLog.load(key, cover, db);
		return oml;
	}
	public CustomNode loadCustomNode(String key, CustomGraph g) {
		CustomNode cn = CustomNode.load(key, g, db);
		return cn;
	}
	public Community loadCommunity(String key, Cover cover) {
		Community c = Community.load(key, cover, db);
		return c;
	}
	public Cover loadCover(String key, CustomGraph g) {
		Cover cov = Cover.load(key, g, db);
		return cov;
	}
	public CustomGraph loadGraph(String key) {
		return CustomGraph.load(key, db);
	}
	
}
