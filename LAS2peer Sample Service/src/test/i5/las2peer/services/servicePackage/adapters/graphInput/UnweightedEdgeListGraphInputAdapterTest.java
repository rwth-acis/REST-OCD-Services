package i5.las2peer.services.servicePackage.adapters.graphInput;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;

import org.junit.Test;

public class UnweightedEdgeListGraphInputAdapterTest {

	@Test
	public void testOnSawmill() throws AdapterException {
		GraphInputAdapter inputAdapter =
				new UnweightedEdgeListGraphInputAdapter(OcdTestConstants.sawmillUnweightedEdgeListInputPath);
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.nodeCount());
		assertEquals(62, graph.edgeCount());
	}
	
	@Test
	public void testOnSiam() throws AdapterException {
		GraphInputAdapter inputAdapter =
				new UnweightedEdgeListGraphInputAdapter(OcdTestConstants.siamDmUnweightedEdgeListInputPath);
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(1219, graph.nodeCount());
		assertEquals(3777, graph.edgeCount());
	}

}
