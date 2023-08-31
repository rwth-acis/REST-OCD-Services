package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import org.graphstream.graph.Node;

public class NodeWeightedEdgeListGraphInputAdapterTest {

	@Test
	public void test() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(graph.getNodeCount(), 36);
		assertEquals(graph.getEdgeCount(), 62);
		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			Node node = nodesIt.next();
			assertEquals(graph.getNodeName(node), Integer.toString(node.getIndex()+1));
		}
	}

}
