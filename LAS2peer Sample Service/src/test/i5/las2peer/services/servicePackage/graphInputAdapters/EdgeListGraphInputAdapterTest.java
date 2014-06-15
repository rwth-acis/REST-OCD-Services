package i5.las2peer.services.servicePackage.graphInputAdapters;

import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.TestConstants;

import org.junit.Test;

public class EdgeListGraphInputAdapterTest {
	
	@Test
	public void test() {
		EdgeListGraphInputAdapter inputAdapter =
				new EdgeListGraphInputAdapter(TestConstants.getSawmillTxtInputFileName());
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println("Nodes: " + graph.nodeCount());
		System.out.println("Edges: " + graph.edgeCount());
	}

}
