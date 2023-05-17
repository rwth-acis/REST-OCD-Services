package i5.las2peer.services.ocd.adapters.graphInput;

import com.arangodb.ArangoCollection;
import com.arangodb.DbName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.graphs.OcdPersistenceLoadException;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.DatabaseConfig;
import i5.las2peer.services.ocd.utils.Error;
import org.apache.commons.lang3.time.DateUtils;
import org.json.simple.parser.JSONParser;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

public class ArangoDBGraphInputAdapterTest {
    @Test
    @Ignore //TODO: Unignore, create database before tests
    public void importNodesTest() throws AdapterException, ParseException, org.json.simple.parser.ParseException {
        ArangoDBGraphInputAdapter inputAdapter = new ArangoDBGraphInputAdapter();
        HashMap<String,String> paramMap = new HashMap<>();
        paramMap.put("databaseAddress","localhost:8531");//"http://127.0.0.1"); //TODO: Check if some machines require 127.0.0.1 instead of localhost
        paramMap.put("databaseCredentials","root,");
        paramMap.put("databaseName","TwitterWatcher");
        paramMap.put("nodeCollectionName","People");
        paramMap.put("edgeCollectionNames","Retweets");


        inputAdapter.setParameter(paramMap);
        CustomGraph graph = inputAdapter.readGraph();
        //System.out.println(graph.getNodeCount() + " " +  graph.getEdgeCount());
        //System.out.println(graph.getNodeName(graph.getNode(0)));
    }

    @Test
    @Ignore
    //TODO: Unignore, create database before tests
    public void getGraphTest() throws AdapterException, ParseException, org.json.simple.parser.ParseException {
        ArangoDBGraphInputAdapter inputAdapter = new ArangoDBGraphInputAdapter();
        HashMap<String,String> paramMap = new HashMap<>();
        paramMap.put("databaseAddress","localhost:8531");//"http://127.0.0.1"); //TODO: Check if some machines require 127.0.0.1 instead of localhost
        paramMap.put("databaseCredentials","root,");
        paramMap.put("databaseName","TestDB");
        paramMap.put("nodeCollectionName","NodeStuff");
        paramMap.put("edgeCollectionNames","EdgeStuff1");
        paramMap.put("dateAttributeName","");
        paramMap.put("startDate","");
        paramMap.put("endDate","");


        inputAdapter.setParameter(paramMap);
        CustomGraph graph = inputAdapter.readGraph();


        //DatabaseConfig.setConfigFile(false);		//TODO angeben ob test datenbank oder hauptdatenbank gewaehlt wird
        //Database database = new Database();

        //database.getGraph("maxkissgen","142924");

    }
    @Test
    @Ignore
    public void tmpTest() throws AdapterException, ParseException, org.json.simple.parser.ParseException, OcdPersistenceLoadException {
        ArangoDBGraphInputAdapter inputAdapter = new ArangoDBGraphInputAdapter();
        HashMap<String,String> paramMap = new HashMap<>();
        paramMap.put("databaseAddress","localhost:8529");//"http://127.0.0.1"); //TODO: Check if some machines require 127.0.0.1 instead of localhost
        paramMap.put("databaseCredentials","root,");
        paramMap.put("databaseName","TwitterWatcher");
        paramMap.put("nodeCollectionName","People");
        paramMap.put("involvedUsers","Q12325752,Q12301603,Q20199803,Q64415132,Q21208022,Q64442287,Q952472,Q64439937,Q12301637,Q12301892,Q64441438,Q12301898,Q64441411,Q463680,Q64439073,Q12303134,Q567098,Q64440032,Q64441613,Q12303652,Q64417294,Q12342520,Q20197574,Q64439019,Q47500034,Q64439137,Q12306031,Q64439851");
        paramMap.put("edgeCollectionNames","Retweets");
        paramMap.put("dateAttributeName","created_at");
        paramMap.put("startDate","2022-02-02");
        paramMap.put("endDate","2023-01-16");


        inputAdapter.setParameter(paramMap);
        CustomGraph graph = inputAdapter.readGraph();
        graph.setName("EXTRAINFOTEST");
        graph.setUserName("");

        GraphProcessor processor = new GraphProcessor();
        processor.determineGraphTypes(graph);
        graph.setNodeEdgeCountColumnFields(); // before persisting the graph, update node/edge count information
        //Try to make graph undirected
        Set<GraphType> graphTypes = graph.getTypes();
        if (graphTypes.remove(GraphType.DIRECTED)) {
            processor.makeCompatible(graph, graphTypes);
        }
        Database database = new Database(true); //TODO angeben ob test datenbank oder hauptdatenbank gewaehlt wird
        String key = database.storeGraph(graph);
        CustomGraph newGraph = database.getGraph("",key);
        System.out.println(newGraph.getExtraInfo());
        System.out.println(newGraph.getExtraInfo().get("startDate").getClass());
        System.out.println(newGraph.getExtraInfo().get("endDate").getClass());
        //DatabaseConfig.setConfigFile(false);		//TODO angeben ob test datenbank oder hauptdatenbank gewaehlt wird
        //Database database = new Database();

        //database.getGraph("maxkissgen","142924");

    }

    @Test
    @Ignore
    public void dateStuffTest() throws ParseException {
        Database db = new Database(false);
        ArangoCollection graphCollection = db.db.collection(CustomGraph.collectionName);

        ObjectMapper om = new ObjectMapper();
        ObjectNode nextGraphInListDoc = graphCollection.getDocument("734136", ObjectNode.class);
        String testDateString = "2022-04-30T22:00:00Z";
        System.out.println(DateUtils.parseDate(om.convertValue(nextGraphInListDoc.get(CustomGraph.extraInfoColumnName).get("endDate"),String.class), "yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd").toString());
    }
}
