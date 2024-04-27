/*
package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.Database;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class DynamicGraphDatabaseTest {

    private static final String userName1 = "testUser1";
    private static final String graphName1 = "persistenceTestGraph1";
    private static final String invalidGraphName = "invalidGraphName";
    private static Database database;

    @BeforeClass
    public static void clearDatabase() {
        database = new Database(true);
    }

    @AfterClass
    public static void deleteDatabase() {
        database.deleteDatabase();
    }

    @Test
    public void testPersist() {
        DynamicGraph graph = new DynamicGraph();
        graph.setUserName(userName1);
        graph.setName(graphName1);
        Node nodeA = graph.addNode("A");
        Node nodeB = graph.addNode("B");
        Node nodeC = graph.addNode("C");
        graph.setNodeName(nodeA, "A");
        graph.setNodeName(nodeB, "B");
        graph.setNodeName(nodeC, "C");
        Edge edgeAB = graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
        graph.addDynamicInteraction(edgeAB,"0","+");
        graph.setEdgeWeight(edgeAB, 5);
        Edge edgeBC = graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeC);
        graph.addDynamicInteraction(edgeBC, "1","-");
        graph.setEdgeWeight(edgeBC, 2.5);
        graph.addType(GraphType.DYNAMIC);

        database.storeGraph(graph);


    }

}*/
