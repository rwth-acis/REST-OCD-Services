package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.*;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphMlGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

public class GraphMlGraphInputAdapterTest {

	@Test
	public void test() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new GraphMlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.sawmillGraphMlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.nodeCount());
		assertEquals(124, graph.edgeCount());
		assertEquals(2, graph.getEdgeWeight(graph.getEdgeArray()[0]), 0);
		assertEquals("1", graph.getNodeName(graph.getNodeArray()[0]));
	}
	
	@Test
	public void testOnJungOutput() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new GraphMlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.fitnessGraphMlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
	}

}
