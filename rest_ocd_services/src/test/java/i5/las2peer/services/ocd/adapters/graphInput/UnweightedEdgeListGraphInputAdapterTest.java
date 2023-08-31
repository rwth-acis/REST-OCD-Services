package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;


public class UnweightedEdgeListGraphInputAdapterTest {

	@Test
	public void testOnSawmill() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new UnweightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillUnweightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.getNodeCount());
		assertEquals(62, graph.getEdgeCount());
	}
	
	@Test
	public void testOnSiam() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new UnweightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.siamDmUnweightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(1219, graph.getNodeCount());
		assertEquals(3777, graph.getEdgeCount());
	}
	
	@Test
	public void testWithStringReader() throws AdapterException {
		GraphInputAdapter inputAdapter = new UnweightedEdgeListGraphInputAdapter();
		String graphStr = new String(
			"0 1\n"
			+ "0 2\n"
			+ "0 3"
		);
		Reader reader = new StringReader(graphStr);
		inputAdapter.setReader(reader);
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(4, graph.getNodeCount());
		assertEquals(3, graph.getEdgeCount());
	}

}
