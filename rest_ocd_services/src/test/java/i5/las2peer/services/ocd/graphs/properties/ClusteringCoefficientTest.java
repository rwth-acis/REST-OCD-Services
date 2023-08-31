package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;


@ExtendWith(MockitoExtension.class)
public class ClusteringCoefficientTest {
	
	@Spy
	ClusteringCoefficient property;
	
	@Test
	public void localUndirected() {

		ClusteringCoefficient property = new ClusteringCoefficient();
		double result;

		result = property.localUndirected(1, 4);
		assertEquals(0.1666666, result, 0.00001);

		result = property.localUndirected(2, 5);
		assertEquals(0.2, result, 0.00001);
		
		result = property.localUndirected(4, 6);
		assertEquals(0.266666, result, 0.00001);
		
		result = property.localUndirected(3, 3);
		assertEquals(1.0, result, 0.00001);
		
		result = property.localUndirected(1, 2);
		assertEquals(1.0, result, 0.00001);

		result = property.localUndirected(0, 3);
		assertEquals(0.0, result, 0.00001);

		result = property.localUndirected(4, 0);
		assertEquals(0.0, result, 0.00001);

	}
	
	@Test
	public void localDirected() {

		ClusteringCoefficient property = new ClusteringCoefficient();
		double result;

		result = property.localDirected(1, 4);
		assertEquals(0.083333, result, 0.00001);

		result = property.localDirected(2, 5);
		assertEquals(0.1, result, 0.00001);
		
		result = property.localDirected(4, 6);
		assertEquals(0.1333333, result, 0.00001);
		
		result = property.localDirected(3, 3);
		assertEquals(0.5, result, 0.00001);

		result = property.localDirected(0, 3);
		assertEquals(0.0, result, 0.00001);

		result = property.localDirected(4, 0);
		assertEquals(0.0, result, 0.00001);

	}
	
	@Test
	public void calculateNodeLocalUndirected() throws InterruptedException {

		CustomGraph graph = new CustomGraph();
		Node n1 = graph.addNode(Integer.toString(1));
		Node n2 = graph.addNode(Integer.toString(2));
		Node n3 = graph.addNode(Integer.toString(3));
		Node n4 = graph.addNode(Integer.toString(4));
		Node n5 = graph.addNode(Integer.toString(5));
		Node n6 = graph.addNode(Integer.toString(6));
		
		graph.addEdge(UUID.randomUUID().toString(), n1, n2);
		graph.addEdge(UUID.randomUUID().toString(), n2, n1);
		graph.addEdge(UUID.randomUUID().toString(), n1, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n1);
		graph.addEdge(UUID.randomUUID().toString(), n1, n4);
		graph.addEdge(UUID.randomUUID().toString(), n4, n1);
		graph.addEdge(UUID.randomUUID().toString(), n1, n5);
		graph.addEdge(UUID.randomUUID().toString(), n5, n1);
		graph.addEdge(UUID.randomUUID().toString(), n1, n6);
		graph.addEdge(UUID.randomUUID().toString(), n6, n1);
		
		graph.addEdge(UUID.randomUUID().toString(), n2, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n2);
		graph.addEdge(UUID.randomUUID().toString(), n5, n4);
		graph.addEdge(UUID.randomUUID().toString(), n4, n5);
		
		property.calculateLocal(n1, graph);
		Mockito.verify(property, Mockito.times(1)).localUndirected(2, 5);

	}	
	
	@Test
	public void calculateNodeLocal() throws InterruptedException {

		CustomGraph graph = new CustomGraph();
		Node n1 = graph.addNode(Integer.toString(1));
		Node n2 = graph.addNode(Integer.toString(2));
		Node n3 = graph.addNode(Integer.toString(3));
		Node n4 = graph.addNode(Integer.toString(4));
		graph.addEdge(UUID.randomUUID().toString(), n1, n2);
		graph.addEdge(UUID.randomUUID().toString(), n2, n1);

		graph.addEdge(UUID.randomUUID().toString(), n1, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n1);

		graph.addEdge(UUID.randomUUID().toString(), n1, n4);
		graph.addEdge(UUID.randomUUID().toString(), n4, n1);

		graph.addEdge(UUID.randomUUID().toString(), n2, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n2);

		graph.addEdge(UUID.randomUUID().toString(), n3, n4);
		graph.addEdge(UUID.randomUUID().toString(), n4, n3);
		
		property.calculateLocal(n1, graph);
		Mockito.verify(property, Mockito.times(1)).localUndirected(2, 3);

	}
	
	@Test
	public void initialize() throws InterruptedException {

		CustomGraph graph = new CustomGraph();
		Node n1 = graph.addNode(Integer.toString(1));
		Node n2 = graph.addNode(Integer.toString(2));
		Node n3 = graph.addNode(Integer.toString(3));
		Node n4 = graph.addNode(Integer.toString(4));
		graph.addEdge(UUID.randomUUID().toString(), n1, n2);
		graph.addEdge(UUID.randomUUID().toString(), n2, n1);

		graph.addEdge(UUID.randomUUID().toString(), n1, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n1);

		graph.addEdge(UUID.randomUUID().toString(), n2, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n2);

		graph.addEdge(UUID.randomUUID().toString(), n3, n4);
		graph.addEdge(UUID.randomUUID().toString(), n4, n3);

		property.calculate(graph);
		Mockito.verify(property, Mockito.times(2)).localUndirected(1, 2);
		Mockito.verify(property, Mockito.times(1)).localUndirected(1, 3);
		Mockito.verify(property, Mockito.times(1)).localUndirected(0, 1);
		
	}
}
