package i5.las2peer.services.ocd.graphs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

import org.junit.Test;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

public class CustomGraphTest {

	@Test
	public void testCopyConstructor() {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		Edge edge = graph.addEdge(UUID.randomUUID().toString(), node0, node1);

		graph.addEdge(UUID.randomUUID().toString(), node1, node0);
		graph.addEdge(UUID.randomUUID().toString(), node0, node0);
		Node node2 = graph.addNode("2");
		graph.addEdge(UUID.randomUUID().toString(), node0, node2);
		graph.addEdge(UUID.randomUUID().toString(), node1, node2);
		Edge edge5 = graph.addEdge(UUID.randomUUID().toString(), node2, node1);
		graph.setEdgeWeight(edge5, 5);
		CustomGraph copy = new CustomGraph(graph);
		assertEquals(graph.getNodeCount(), copy.getNodeCount());
		assertEquals(graph.getEdgeCount(), copy.getEdgeCount());
		Edge copyEdge5 = copy.getEdge(5);
		assertEquals(5, copy.getEdgeWeight(copyEdge5), 0);
		graph.setEdgeWeight(edge5, 2);
		assertEquals(5, copy.getEdgeWeight(copyEdge5), 0);
		copy.removeEdge(copyEdge5);
		assertEquals(graph.getEdgeCount() - 1, copy.getEdgeCount());
	}

	@Test(expected = NullPointerException.class)
	public void testEdgeRemoval() {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		graph.addEdge(UUID.randomUUID().toString(), node0, node1);
		graph.addEdge(UUID.randomUUID().toString(), node1, node0);
		graph.addEdge(UUID.randomUUID().toString(), node0, node0);
		Node node2 = graph.addNode("2");
		graph.addEdge(UUID.randomUUID().toString(), node0, node2);
		graph.addEdge(UUID.randomUUID().toString(), node1, node2);
		Edge edge5 = graph.addEdge(UUID.randomUUID().toString(), node2, node1);
		graph.setEdgeWeight(edge5, 5);
		assertEquals(6, graph.getEdgeCount());
		graph.removeEdge(edge5);
		assertEquals(5, graph.getEdgeCount());
		graph.getEdgeWeight(edge5);
	}
	
	@Test
	public void getProperties() throws InterruptedException {

		CustomGraph graph = new CustomGraph();
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
	
	@Test
	public void getSubGraph() {
		
		CustomGraph graph = new CustomGraph();
		CustomGraph result;
		Node n0 = graph.addNode("0");
		Node n1 = graph.addNode("1");
		Node n2 = graph.addNode("2");
		Node n3 = graph.addNode("3");

		graph.addEdge(UUID.randomUUID().toString(), n0, n1);
		graph.addEdge(UUID.randomUUID().toString(), n0, n2);
		graph.addEdge(UUID.randomUUID().toString(), n3, n0);
		graph.addEdge(UUID.randomUUID().toString(), n3, n2);

		List<Integer> nodeIds = new ArrayList<>();

		nodeIds.add(0);
		result = graph.getSubGraph(nodeIds);
		assertEquals(1, result.getNodeCount());
		assertEquals(0, result.getEdgeCount());
		
		nodeIds.add(3);
		result = graph.getSubGraph(nodeIds);
		assertEquals(2, result.getNodeCount());
		assertEquals(1, result.getEdgeCount());
		
		nodeIds.add(2);
		result = graph.getSubGraph(nodeIds);
		assertEquals(3, result.getNodeCount());
		assertEquals(3, result.getEdgeCount());
	}
	
	
	
}
