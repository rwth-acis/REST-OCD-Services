package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestConstants;

import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class GmlGraphInputAdapterTest {

	@Test
	public void test() throws AdapterException, IOException, OcdAlgorithmException, InterruptedException {
		GraphInputAdapter inputAdapter = new GmlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.dolphinsGmlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println("Nodes: " + graph.nodeCount());
		System.out.println("Edges: " + graph.edgeCount());
		assertEquals(62, graph.nodeCount());
		assertEquals(159, graph.edgeCount());
		assertEquals("Beak", graph.getNodeName(graph.getNodeArray()[0]));
	}

}
