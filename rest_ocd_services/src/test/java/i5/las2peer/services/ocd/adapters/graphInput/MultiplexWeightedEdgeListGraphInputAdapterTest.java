package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class MultiplexWeightedEdgeListGraphInputAdapterTest {
	
	@Test
	public void testWithStringReader2Layers() throws AdapterException {
		MultiplexGraphInputAdapter inputAdapter = new MultiplexWeightedEdgeListGraphInputAdapter();
		String graphStr = new String(
			"1 0 1 1\n"
			+ "1 0 2 2\n"
			+ "1 0 3 3\n"
			+ "2 0 3 1\n"
			+ "2 1 3 0.5\n"
			+ "2 1 2 10\n"
			+ "2 2 3 1"
		);
		Reader reader = new StringReader(graphStr);
		inputAdapter.setReader(reader);
		MultiplexGraph multiplexGraph = inputAdapter.readGraph();
		CustomGraph customGraph1 = multiplexGraph.getMultiplexCustomGraphs().get("1");
		CustomGraph customGraph2 = multiplexGraph.getMultiplexCustomGraphs().get("2");

		assertEquals(2, multiplexGraph.getLayerCount());
		assertEquals(4, customGraph1.getNodeCount());
		assertEquals(3, customGraph1.getEdgeCount());
		assertEquals(4, customGraph2.getNodeCount());
		assertEquals(4, customGraph2.getEdgeCount());
	}

	@Test
	public void testWithStringReader1Layer() throws AdapterException {
		MultiplexGraphInputAdapter inputAdapter = new MultiplexWeightedEdgeListGraphInputAdapter();
		String graphStr = new String(
				"1 0 1 1\n"
						+ "1 0 2 2\n"
						+ "1 0 3 3"
		);
		Reader reader = new StringReader(graphStr);
		inputAdapter.setReader(reader);
		MultiplexGraph multiplexGraph = inputAdapter.readGraph();
		CustomGraph customGraph1 = multiplexGraph.getMultiplexCustomGraphs().get("1");

		assertEquals(1, multiplexGraph.getLayerCount());
		assertEquals(4, customGraph1.getNodeCount());
		assertEquals(3, customGraph1.getEdgeCount());
	}

}
