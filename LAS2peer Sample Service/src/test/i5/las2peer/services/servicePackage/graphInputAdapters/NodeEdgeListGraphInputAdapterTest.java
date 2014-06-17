package i5.las2peer.services.servicePackage.graphInputAdapters;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;

import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

public class NodeEdgeListGraphInputAdapterTest {

	@Test
	public void test() {
		GraphInputAdapter inputAdapter =
				new NodeEdgeListGraphInputAdapter(OcdTestConstants.sawmillNodeEdgeListInputPath);
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
