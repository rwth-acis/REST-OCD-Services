package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.NodeWeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

public class NodeWeightedEdgeListGraphInputAdapterTest {

	@Test
	public void test() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter =
				new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(graph.nodeCount(), 36);
		assertEquals(graph.edgeCount(), 62);
		NodeCursor nodes = graph.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			assertEquals(graph.getNodeName(node), Integer.toString(node.index()+1));
			nodes.next();
		}
	}

}
