package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Node;

@RunWith(MockitoJUnitRunner.class)
public class GraphPropertiesTest {

	public CustomGraph getTestGraph() {

		CustomGraph graph = new CustomGraph();
		Node n1 = graph.createNode();
		Node n2 = graph.createNode();
		Node n3 = graph.createNode();
		Node n4 = graph.createNode();

		graph.createEdge(n1, n2);
		graph.createEdge(n2, n3);
		graph.createEdge(n2, n4);

		return graph;
	}

	@Test
	public void getPropertyDensity() {

		CustomGraphProperties properties = new CustomGraphProperties(getTestGraph());
		double result = properties.getProperty(GraphProperty.DENSITY);
		assertEquals(0.25, result, 0.000001);

	}

	@Test
	public void getPropertyAverageDegree() {

		CustomGraphProperties properties = new CustomGraphProperties(getTestGraph());
		double result = properties.getProperty(GraphProperty.AVERAGE_DEGREE);
		assertEquals(1.5, result, 0.000001);

	}
	
	@Test
	public void initialize() {

		CustomGraphProperties properties = new CustomGraphProperties(getTestGraph());
		List<Double> list = properties.getProperties();
		assertNotNull(list);
		assertEquals(GraphProperty.values().length, list.size());

	}

}
