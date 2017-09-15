package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Node;

@RunWith(MockitoJUnitRunner.class)
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
		Node n1 = graph.createNode();
		Node n2 = graph.createNode();
		Node n3 = graph.createNode();
		Node n4 = graph.createNode();
		graph.createEdge(n1, n2);
		graph.createEdge(n2, n1);
		graph.createEdge(n2, n3);
		graph.createEdge(n3, n2);
		graph.createEdge(n3, n4);
		graph.createEdge(n4, n3);

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
		Node n1 = graph.createNode();
		Node n2 = graph.createNode();
		Node n3 = graph.createNode();
		Node n4 = graph.createNode();
		graph.createEdge(n1, n2);
		graph.createEdge(n2, n3);
		graph.createEdge(n3, n4);

		double result;
		result = property.calculate(graph);
		Mockito.verify(property, Mockito.times(1))
				.calculate(Matchers.eq(new double[] { 1.0, 2.0, 2.0, 1.0 }));
		assertEquals(0.57735, result, 0.00001);
	}

}
