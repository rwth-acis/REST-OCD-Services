package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.DbName;
import com.arangodb.ArangoCollection;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.entity.BaseDocument;

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
	private ArangoDatabase db;

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
		String coverKey = "coverKey";
		log.persist("cover/"+coverKey, db);
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
	public OcdMetricLog loadOcdMetricLog(String key) {
		OcdMetricLog oml = OcdMetricLog.load(key,  db);
		return oml;
	}
	
	
}
