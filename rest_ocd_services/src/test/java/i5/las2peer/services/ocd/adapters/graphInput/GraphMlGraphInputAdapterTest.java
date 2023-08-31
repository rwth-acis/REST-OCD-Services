package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.jupiter.api.Assertions.*;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import org.junit.jupiter.api.Test;

public class GraphMlGraphInputAdapterTest {

	@Test
	public void test() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new GraphMlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.sawmillGraphMlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.getNodeCount());
		assertEquals(124, graph.getEdgeCount());
		assertEquals(2, graph.getEdgeWeight(graph.edges().toArray(Edge[]::new)[0]), 0);
		assertEquals("n0", graph.getNodeName(graph.nodes().toArray(Node[]::new)[0]));
	}
	
	@Test
	public void testOnJungOutput() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new GraphMlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.fitnessGraphMlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
	}

}
