package i5.las2peer.services.ocd.adapters.graphInput;

import com.arangodb.ArangoCollection;
import com.arangodb.DbName;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionCreateOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomEdge;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomNode;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.graphs.OcdPersistenceLoadException;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.DatabaseConfig;
import i5.las2peer.services.ocd.utils.Error;
import org.apache.commons.lang3.time.DateUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

public class ArangoDBGraphInputAdapterTest {
    private static Database database;

    @BeforeClass
	public static void initDatabase() {
		Database database = new Database(true);

        database.db.createCollection("TestNodes");
        for(int i=0; i<10; i++) {
            database.db.collection("TestNodes").insertDocument("{" + //
                "  \"_key\": \"Q" + i + "\"," + //
                "  \"object_a\": {" + //
                "    \"username\": \"someName_a\"," + //
                "    \"public_metrics\": {" + //
                "      \"followers_count\": 5787," + //
                "      \"following_count\": 1004" + //
                "    }" + //
                "  }," + //
                "  \"object_b\": {" + //
                "    \"name\": \"someName_b\"," + //
                "    \"instance of\": [" + //
                "      {" + //
                "        \"value\": \"human\"" + //
                "      }" + //
                "    ]" + //
                "  }" + //
                "}");
        }
        CollectionCreateOptions options = (new CollectionCreateOptions());
        options.type(CollectionType.EDGES);
        database.db.createCollection("TestEdges", options);
        for(int i=0; i<10; i++) {
            for(int j=0; j<10; j++) {
                if (i<3 && j>=3 && j<6) {
                    String edgeDate = (j == 5) ? " \"point_in_time\": \"2022-02-25T09:26:30.000Z\"" : " \"point_in_time\": \"2019-02-25T09:26:30.000Z\"";
                    
                    database.db.collection("TestEdges").insertDocument("{" + //
                        "  \"_from\": \"TestNodes/Q" + i + "\"," + //
                        "  \"_to\": \"TestNodes/Q"+ j + "\"," + //
                        " \"property_a\": {\"value\": 1}," + //
                        " \"property_b\": [\"A\", \"B\", \"C\"]," + //
                        edgeDate + //
                        "}");
                }
            }
        }
	}

    @AfterClass
    public static void deleteDatabase() {
        database.deleteDatabase();
    }

    @Test
    public void getGraphTest() throws AdapterException, ParseException, org.json.simple.parser.ParseException, OcdPersistenceLoadException {
        ArangoDBGraphInputAdapter inputAdapter = new ArangoDBGraphInputAdapter();
        HashMap<String,String> paramMap = new HashMap<>();

        Properties dbProps = (new DatabaseConfig()).getConfigProperties();

        paramMap.put("databaseAddress", dbProps.getProperty("HOST")+":" + dbProps.getProperty("PORT"));//"http://127.0.0.1"); //TODO: Check if some machines require 127.0.0.1 instead of localhost
        paramMap.put("databaseCredentials", dbProps.getProperty("USER") + "," + dbProps.getProperty("PASSWORD"));
        paramMap.put("databaseName", dbProps.getProperty("TESTDATABASENAME"));
        paramMap.put("nodeCollectionName","TestNodes");
        paramMap.put("edgeCollectionNames","TestEdges");
        paramMap.put("dateAttributeName","point_in_time");
        paramMap.put("startDate","2022-02-25T09:26:30.000Z");
        paramMap.put("endDate","2022-02-26T09:26:30.000Z");

        inputAdapter.setParameter(paramMap);
        CustomGraph graph = inputAdapter.readGraph();
        graph.setUserName("testuser");
        database = new Database(true);
        graph = database.getGraph("testuser", database.storeGraph(graph));
        Iterator<Edge> edgesIt = graph.edges().iterator();
        List<String> allowedSources = List.of("Q0","Q1","Q2");
        List<String> allowedTargets = List.of("Q5");
        int edgeCount = 0;
        while(edgesIt.hasNext()) {
            Edge edge = edgesIt.next();
            String cSourceName = graph.getNodeName(edge.getSourceNode());
            String cTargetName = graph.getNodeName(edge.getTargetNode());
            assertTrue(allowedSources.contains(cSourceName));
            assertTrue(allowedTargets.contains(cTargetName)); 
            edgeCount++;
        }
        assertEquals(edgeCount, 3); // Check if all edges toward node Q5 were counted and whether not more have been registered
    }
}
