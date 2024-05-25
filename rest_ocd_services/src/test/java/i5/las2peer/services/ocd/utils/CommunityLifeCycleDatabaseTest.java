package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import java.awt.*;
import java.util.*;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class CommunityLifeCycleDatabaseTest {
    private static final String userName = "clcPersistenceUser";

    private static final String graphName = "clcPersistenceGraph";

    private static final String coverName = "clcPersistenceCover";

    private static final String clcName = "clcPersistenceCLC";

    private static Database database;

    @BeforeClass
    public static void clearDatabase() {database = new Database(true);}

    @AfterClass
    public static void deleteDatabase() {database.deleteDatabase();}

    @Test
    public void testPersist() {
        DynamicGraph graph = new DynamicGraph();
        graph.setUserName(userName);
        graph.setName(graphName);

        Node nodeA = graph.addNode("nodeA");
        Node nodeB = graph.addNode("nodeB");
        Node nodeC = graph.addNode("nodeC");
        graph.setNodeName(nodeA, "A");
        graph.setNodeName(nodeB, "B");
        graph.setNodeName(nodeC, "C");
        Edge edgeAB = graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
        graph.addDynamicInteraction(edgeAB,"0","+");
        graph.setEdgeWeight(edgeAB, 5);
        Edge edgeBC = graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeC);
        graph.addDynamicInteraction(edgeBC, "1","-");
        graph.setEdgeWeight(edgeBC, 2.5);

        String gkey = database.storeGraph(graph);
        System.out.println("graph key:" + graph.getKey());

        Matrix memberships = new CCSMatrix(3, 2);
        memberships.set(0, 0, 1);
        memberships.set(1, 0, 0.5);
        memberships.set(1, 1, 0.5);
        memberships.set(2, 1, 1);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("param1", "val1");
        params.put("param2", "val2");
        CoverCreationLog algo = new CoverCreationLog(CoverCreationType.UNDEFINED, params, new HashSet<GraphType>());
        Cover cover = new Cover(graph, memberships);
        cover.setCreationMethod(algo);
        cover.setName(coverName);
        cover.setCommunityColor(1, Color.BLUE);
        cover.setCommunityName(1, "Community1");
        OcdMetricLog metric = new OcdMetricLog(OcdMetricType.EXECUTION_TIME, 3.55, params, cover);
        cover.addMetric(metric);

        String cKey = database.storeCover(cover);
        System.out.println("cover key: " + cKey);
        List<String> birth_nodes = new ArrayList<>();
        birth_nodes.add("A");
        birth_nodes.add("B");
        birth_nodes.add("C");
        CommunityLifeCycle clc = new CommunityLifeCycle(cover, graph);
        clc.setName(clcName);
        clc.handleBirth("0", 1, birth_nodes);
        clc.handleBirth("1", 2, birth_nodes);
        clc.handleGrowth("3", 1, "D");

        String clcKey = database.storeCLC(clc);
        System.out.println("clcKey: " + clcKey);

        CommunityLifeCycle result = database.getCLC(userName, clcKey, gkey, cKey);

        assertNotNull(result);

        System.out.println(result.toString());
    }
}
