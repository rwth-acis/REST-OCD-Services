package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Node;

@RunWith(MockitoJUnitRunner.class)
public class ClusteringCoefficientTest {
	
	@Spy
	ClusteringCoefficient property;
	
	@Test
	public void calculateLocalUndirected() {

		ClusteringCoefficient property = new ClusteringCoefficient();
		double result;

		result = property.calculateLocalClusteringCoefficient(false, 1, 4);
		assertEquals(0.166666, result, 0.00001);

		result = property.calculateLocalClusteringCoefficient(false, 4, 5);
		assertEquals(0.4, result, 0.00001);

		result = property.calculateLocalClusteringCoefficient(false, 0, 3);
		assertEquals(0.0, result, 0.00001);

		result = property.calculateLocalClusteringCoefficient(false, 4, 0);
		assertEquals(0.0, result, 0.00001);
	}

	@Test
	public void calculateLocalDirected() {

		ClusteringCoefficient property = new ClusteringCoefficient();
		double result;

		result = property.calculateLocalClusteringCoefficient(true, 1, 4);
		assertEquals(0.083333, result, 0.00001);

		result = property.calculateLocalClusteringCoefficient(true, 4, 5);
		assertEquals(0.2, result, 0.00001);

		result = property.calculateLocalClusteringCoefficient(true, 0, 3);
		assertEquals(0.0, result, 0.00001);

		result = property.calculateLocalClusteringCoefficient(true, 4, 0);
		assertEquals(0.0, result, 0.00001);

	}
	
	@Test
	public void initialize() {

		CustomGraph graph = new CustomGraph();
		Node n1 = graph.createNode();
		Node n2 = graph.createNode();
		Node n3 = graph.createNode();
		Node n4 = graph.createNode();
		graph.createEdge(n1, n2);
		graph.createEdge(n2, n1);
		
		graph.createEdge(n1, n3);
		graph.createEdge(n3, n1);
		
		graph.createEdge(n2, n3);
		graph.createEdge(n3, n2);
		
		graph.createEdge(n3, n4);
		graph.createEdge(n4, n3);
				
		property.calculate(graph);
		Mockito.verify(property, Mockito.times(2)).calculateLocalClusteringCoefficient(false, 2, 4);
		Mockito.verify(property, Mockito.times(1)).calculateLocalClusteringCoefficient(false, 2, 6);
		Mockito.verify(property, Mockito.times(1)).calculateLocalClusteringCoefficient(false, 0, 2);
		
	}
}
