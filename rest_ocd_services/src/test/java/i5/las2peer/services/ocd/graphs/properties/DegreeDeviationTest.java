package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;

@ExtendWith(MockitoExtension.class)
public class DegreeDeviationTest {
	
	@Spy
	DegreeDeviation property;
	
	@Test
	public void calculate() {

		DegreeDeviation property = new DegreeDeviation();
		double result;

		result = property.calculate(new double[] { 1.0, 4.0, 3.0, 3.0 });
		assertEquals(1.2583, result, 0.00001);

		result = property.calculate(new double[] { 2.0, 2.0, 2.0, 2.0 });
		assertEquals(0.0, result, 0.00001);

	}

	@Test
	public void initializeUndirected() {

		CustomGraph graph = new CustomGraph();
		Node n1 = graph.addNode(Integer.toString(1));
		Node n2 = graph.addNode(Integer.toString(2));
		Node n3 = graph.addNode(Integer.toString(3));
		Node n4 = graph.addNode(Integer.toString(4));
		graph.addEdge(UUID.randomUUID().toString(), n1, n2);
		graph.addEdge(UUID.randomUUID().toString(), n2, n1);
		graph.addEdge(UUID.randomUUID().toString(), n2, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n2);
		graph.addEdge(UUID.randomUUID().toString(), n3, n4);
		graph.addEdge(UUID.randomUUID().toString(), n4, n3);

		double result;
		result = property.calculate(graph);
		Mockito.verify(property, Mockito.times(1))
				.calculate(Matchers.eq(new double[] { 1.0, 2.0, 2.0, 1.0 }));
		assertEquals(0.57735, result, 0.00001);
	}
	
	@Test
	public void initializeDirected() {

		CustomGraph graph = new CustomGraph();
		graph.addType(GraphType.DIRECTED);
		Node n1 = graph.addNode(Integer.toString(1));
		Node n2 = graph.addNode(Integer.toString(2));
		Node n3 = graph.addNode(Integer.toString(3));
		Node n4 = graph.addNode(Integer.toString(4));
		graph.addEdge(UUID.randomUUID().toString(), n1, n2);
		graph.addEdge(UUID.randomUUID().toString(), n2, n3);
		graph.addEdge(UUID.randomUUID().toString(), n3, n4);

		double result;
		result = property.calculate(graph);
		Mockito.verify(property, Mockito.times(1))
				.calculate(Matchers.eq(new double[] { 1.0, 2.0, 2.0, 1.0 }));
		assertEquals(0.57735, result, 0.00001);
	}

}
