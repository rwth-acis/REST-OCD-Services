package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;


public class WeightedEdgeListGraphInputAdapterTest {
	
	@Test
	public void test() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new WeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.getNodeCount());
		assertEquals(62, graph.getEdgeCount());
	}

}
