package i5.las2peer.services.servicePackage.adapters.graphInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

public class WeightedEdgeListGraphInputAdapterTest {
	
	@Test
	public void test() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new WeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.nodeCount());
		assertEquals(62, graph.edgeCount());
	}

}
