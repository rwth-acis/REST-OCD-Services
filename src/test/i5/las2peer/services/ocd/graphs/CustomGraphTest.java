package i5.las2peer.services.ocd.graphs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

import org.junit.Test;

import y.base.Edge;
import y.base.Node;

public class CustomGraphTest {

	@Test
	public void testCopyConstructor() {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.createNode();
		Node node1 = graph.createNode();
		graph.createEdge(node0, node1);
		graph.createEdge(node1, node0);
		graph.createEdge(node0, node0);
		Node node2 = graph.createNode();
		graph.createEdge(node0, node2);
		graph.createEdge(node1, node2);
		Edge edge5 = graph.createEdge(node2, node1);
		graph.setEdgeWeight(edge5, 5);
		CustomGraph copy = new CustomGraph(graph);
		assertEquals(graph.nodeCount(), copy.nodeCount());
		assertEquals(graph.edgeCount(), copy.edgeCount());
		Edge copyEdge5 = copy.getEdgeArray()[5];
		assertEquals(5, copy.getEdgeWeight(copyEdge5), 0);
		graph.setEdgeWeight(edge5, 2);
		assertEquals(5, copy.getEdgeWeight(copyEdge5), 0);
		copy.removeEdge(copyEdge5);
		assertEquals(graph.edgeCount() - 1, copy.edgeCount());
	}

	@Test(expected = NullPointerException.class)
	public void testEdgeRemoval() {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.createNode();
		Node node1 = graph.createNode();
		graph.createEdge(node0, node1);
		graph.createEdge(node1, node0);
		graph.createEdge(node0, node0);
		Node node2 = graph.createNode();
		graph.createEdge(node0, node2);
		graph.createEdge(node1, node2);
		Edge edge5 = graph.createEdge(node2, node1);
		graph.setEdgeWeight(edge5, 5);
		assertEquals(6, graph.edgeCount());
		graph.removeEdge(edge5);
		assertEquals(5, graph.edgeCount());
		graph.getEdgeWeight(edge5);
	}
	
	@Test
	public void getProperties() {

		CustomGraph graph = new CustomGraph();
		graph.addType(GraphType.DIRECTED);
		Node n1 = graph.createNode();
		Node n2 = graph.createNode();
		Node n3 = graph.createNode();
		Node n4 = graph.createNode();

		graph.createEdge(n1, n2);
		graph.createEdge(n2, n3);
		graph.createEdge(n2, n4);
		
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
		Node n0 = graph.createNode();
		Node n1 = graph.createNode();
		Node n2 = graph.createNode();
		Node n3 = graph.createNode();
		
		graph.createEdge(n0, n1);
		graph.createEdge(n0, n2);
		graph.createEdge(n3, n0);
		graph.createEdge(n3, n2);		

		List<Integer> nodeIds = new ArrayList<>();

		nodeIds.add(0);
		result = graph.getSubGraph(nodeIds);
		assertEquals(1, result.nodeCount());
		assertEquals(0, result.edgeCount());
		
		nodeIds.add(3);
		result = graph.getSubGraph(nodeIds);
		assertEquals(2, result.nodeCount());
		assertEquals(1, result.edgeCount());
		
		nodeIds.add(2);
		result = graph.getSubGraph(nodeIds);
		assertEquals(3, result.nodeCount());
		assertEquals(3, result.edgeCount());
	}
	
	
	
}
