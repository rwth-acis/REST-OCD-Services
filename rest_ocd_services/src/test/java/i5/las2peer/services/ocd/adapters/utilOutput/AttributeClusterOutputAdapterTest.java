package i5.las2peer.services.ocd.adapters.utilOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphMlGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.visualOutput.JsonVisualOutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.OcdPersistenceLoadException;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.viewer.LayoutHandler;
import i5.las2peer.services.ocd.viewer.ViewerRequestHandler;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestConstants;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestGraphFactory;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.jena.atlas.json.JSON;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class AttributeClusterOutputAdapterTest {

    @Test
    @Ignore
    public void testOnTinyCircleGraph() throws ParseException {

    }

    @Test
    public void equalityOperatorTest() throws OcdPersistenceLoadException, IOException, ParserConfigurationException, SAXException, AdapterException, java.text.ParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, ParseException {
        Database db = new Database(true);
        GraphInputAdapter inputAdapter = new GraphMlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.twitterGraphExtraInfoGraphMlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
        graph.setUserName("testuser");
        graph = db.getGraph("testuser", db.storeGraph(graph));
        String content = "<Parameters><Parameter><Name>attributeValue</Name><Value>someInfo</Value></Parameter><Parameter><Name>attributeKeyNesting</Name><Value>info</Value></Parameter><Parameter><Name>operator</Name><Value>==</Value></Parameter></Parameters>";

        RequestHandler handler = new ViewerRequestHandler();
        Map<String,String> param = handler.parseParameters(content);
        ClusterCreationType clusterCreationType = ClusterCreationType.valueOf("BY_ATTRIBUTE");
        JSONObject clusterResults = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).parse(handler.writeClusters(graph, clusterCreationType, param));
        List<String> expectedClusters = List.of("Some info", "Some info2");
        assertTrue(expectedClusters.containsAll((JSONArray) clusterResults.get("clusters")));
    }

    @Test
    public void intervalOperatorTest() throws OcdPersistenceLoadException, IOException, ParserConfigurationException, SAXException, AdapterException, java.text.ParseException, InstantiationException, IllegalAccessException, IllegalArgumentException, ParseException {
        Database db = new Database(true);
        GraphInputAdapter inputAdapter = new GraphMlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.twitterGraphExtraInfoGraphMlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
        graph.setUserName("testuser");
        graph = db.getGraph("testuser", db.storeGraph(graph));
        String content = "<Parameters><Parameter><Name>attributeValue</Name><Value>3</Value></Parameter><Parameter><Name>attributeKeyNesting</Name><Value>number:::actual</Value></Parameter><Parameter><Name>operator</Name><Value>&gt;&lt;</Value></Parameter></Parameters>";

        RequestHandler handler = new ViewerRequestHandler();
        Map<String,String> param = handler.parseParameters(content);
        ClusterCreationType clusterCreationType = ClusterCreationType.valueOf("BY_ATTRIBUTE");
        JSONObject clusterResults = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).parse(handler.writeClusters(graph, clusterCreationType, param));
        List<String> expectedClusters = List.of("greater", "equal", "smaller");
        assertTrue(expectedClusters.containsAll((JSONArray) clusterResults.get("clusters")));
        HashMap<String, Integer> cluster_sizes = new HashMap<String, Integer>(Map.of("greater", 0, "equal", 0, "smaller", 0));
        for(String key : ((JSONObject) clusterResults.get("cluster_nodes")).keySet()) {
            String current_cluster = (String) ((JSONArray) ((JSONObject) clusterResults.get("cluster_nodes")).get(key)).get(0);
            cluster_sizes.put(current_cluster, cluster_sizes.get(current_cluster)+1);
        }
        assertTrue(cluster_sizes.get("greater") == 3 && cluster_sizes.get("smaller") == 2 && cluster_sizes.get("equal") == 1);
    }
}
