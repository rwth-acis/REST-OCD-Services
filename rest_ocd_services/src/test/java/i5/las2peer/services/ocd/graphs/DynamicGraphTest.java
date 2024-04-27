package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamicGraphTest {

    @Test
    public void testSuperCopyConstructor() {
        DynamicGraph graph = new DynamicGraph();
        Node node0 = graph.addNode("0");
        Node node1 = graph.addNode("1");
        Edge edge0 = graph.addEdge(UUID.randomUUID().toString(), node0, node1);
        graph.addDynamicInteraction(edge0, "0", "+");

        Edge edge1 = graph.addEdge(UUID.randomUUID().toString(), node1, node0);
        graph.addDynamicInteraction(edge1, "1", "+");

        Edge edge2 = graph.addEdge(UUID.randomUUID().toString(), node0, node0);
        graph.addDynamicInteraction(edge2, "2", "+");

        Node node2 = graph.addNode("2");

        Edge edge3 = graph.addEdge(UUID.randomUUID().toString(), node0, node2);
        graph.addDynamicInteraction(edge3, "3", "+");

        Edge edge4 = graph.addEdge(UUID.randomUUID().toString(), node1, node2);
        graph.addDynamicInteraction(edge4, "4", "+");

        Edge edge5 = graph.addEdge(UUID.randomUUID().toString(), node2, node1);
        graph.addDynamicInteraction(edge5, "5", "+");

        graph.setEdgeWeight(edge5, 5);
        DynamicGraph copy = new DynamicGraph(graph);

        assertEquals(graph.getNodeCount(), copy.getNodeCount());
        assertEquals(graph.getEdgeCount(), copy.getEdgeCount());
        Edge copyEdge5 = copy.getEdge(5);
        assertEquals(5, copy.getEdgeWeight(copyEdge5), 0);
        graph.setEdgeWeight(edge5, 2);
        assertEquals(5, copy.getEdgeWeight(copyEdge5), 0);
        copy.removeEdge(copyEdge5);
        assertEquals(graph.getEdgeCount() - 1, copy.getEdgeCount());
        assertEquals(graph.getDynamicInteractions().size(), graph.getEdgeCount());
        System.out.println(graph.getDynamicInteractions());
        assertEquals(graph.getDynamicInteractions(), copy.getDynamicInteractions());
    }

    @Test(expected = NullPointerException.class)
    public void testEdgeRemoval() {
        DynamicGraph graph = new DynamicGraph();
        Node node0 = graph.addNode("0");
        Node node1 = graph.addNode("1");
        Edge edge0 = graph.addEdge(UUID.randomUUID().toString(), node0, node1);
        graph.addDynamicInteraction(edge0,"0", "+");
        Edge edge1 =graph.addEdge(UUID.randomUUID().toString(), node1, node0);
        graph.addDynamicInteraction(edge1,"0","+");
        Edge edge2 = graph.addEdge(UUID.randomUUID().toString(), node0, node0);
        graph.addDynamicInteraction(edge2,"0","+");
        Node node2 = graph.addNode("2");
        Edge edge3 = graph.addEdge(UUID.randomUUID().toString(), node0, node2);
        graph.addDynamicInteraction(edge3,"0","+");
        Edge edge4 = graph.addEdge(UUID.randomUUID().toString(), node1, node2);
        graph.addDynamicInteraction(edge4,"0","+");
        Edge edge5 = graph.addEdge(UUID.randomUUID().toString(), node2, node1);
        graph.addDynamicInteraction(edge5,"0","+");
        graph.setEdgeWeight(edge5, 5);
        assertEquals(6, graph.getEdgeCount());
        graph.removeEdge(edge5);
        assertEquals(graph.getDynamicInteractions().size(),6);
        assertEquals(5, graph.getEdgeCount());
        graph.getEdgeWeight(edge5);
    }

    @Test
    public void getProperties() throws InterruptedException {

        DynamicGraph graph = new DynamicGraph();
        graph.addType(GraphType.DIRECTED);
        Node n1 = graph.addNode("1");
        Node n2 = graph.addNode("2");
        Node n3 = graph.addNode("3");
        Node n4 = graph.addNode("4");

        graph.addEdge(UUID.randomUUID().toString(), n1, n2);
        graph.addEdge(UUID.randomUUID().toString(), n2, n3);
        graph.addEdge(UUID.randomUUID().toString(), n2, n4);

        graph.initProperties();
        List<Double> list = graph.getProperties();
        assertNotNull(list);
        assertEquals(GraphProperty.values().length, list.size());

        double result = graph.getProperty(GraphProperty.DENSITY);
        assertEquals(0.25, result, 0.000001);

        result = graph.getProperty(GraphProperty.AVERAGE_DEGREE);
        assertEquals(1.5, result, 0.000001);
    }

}
