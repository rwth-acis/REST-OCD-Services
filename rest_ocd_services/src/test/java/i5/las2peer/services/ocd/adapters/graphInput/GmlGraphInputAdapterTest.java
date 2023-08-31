package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileReader;
import java.io.IOException;

import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;

public class GmlGraphInputAdapterTest {

	@Test
	public void testOnDolphins() throws AdapterException, IOException, OcdAlgorithmException, InterruptedException {
		GraphInputAdapter inputAdapter = new GmlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.dolphinsGmlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());
		assertEquals(62, graph.getNodeCount());
		assertEquals(159, graph.getEdgeCount());
		assertEquals("Beak", graph.getNodeName(graph.nodes().toArray(Node[]::new)[0]));
	}
	
	@Test
	public void testOnZachary() throws AdapterException, IOException, OcdAlgorithmException, InterruptedException {
		GraphInputAdapter inputAdapter = new GmlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.zacharyGmlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());
		assertEquals(34, graph.getNodeCount());
		assertEquals(78, graph.getEdgeCount());
		assertEquals("1", graph.getNodeName(graph.nodes().toArray(Node[]::new)[0]));
	}

}
