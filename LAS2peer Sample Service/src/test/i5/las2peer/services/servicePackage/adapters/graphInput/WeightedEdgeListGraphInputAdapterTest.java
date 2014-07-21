package i5.las2peer.services.servicePackage.adapters.graphInput;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;

import org.junit.Test;

public class WeightedEdgeListGraphInputAdapterTest {
	
	@Test
	public void test() throws AdapterException {
		GraphInputAdapter inputAdapter =
				new WeightedEdgeListGraphInputAdapter(OcdTestConstants.sawmillWeightedEdgeListInputPath);
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.nodeCount());
		assertEquals(62, graph.edgeCount());
	}

}
