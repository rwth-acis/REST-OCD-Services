package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

public class NodeContentListGraphIntputAdapterTest {
	
	@Test
	public void testOnUrch() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new NodeContentListGraphInputAdapter(new FileReader(OcdTestConstants.urchEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(5, graph.nodeCount());;
	}

}
