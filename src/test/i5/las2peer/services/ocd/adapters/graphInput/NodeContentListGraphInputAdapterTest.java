package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

public class NodeContentListGraphInputAdapterTest {
	
	@Test
	public void testOnUrchSmall() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new NodeContentListGraphIntputAdapter(new FileReader(OcdTestConstants.urchEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(5, graph.nodeCount());
	}
	
	@Test
	public void testOnUrchPosts() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new NodeContentListGraphIntputAdapter(new FileReader(OcdTestConstants.urchPostsEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.nodeCount());
	}

}
