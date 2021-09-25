package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import y.base.Node;
import y.base.NodeCursor;

public class AdjacencyMatrixGraphInputAdapterTest {

	@Test
	public void readGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter inputAdapter =
				new AdjacencyMatrixGraphInputAdapter(new FileReader(OcdTestConstants.sawmillAdjacencyMatrixInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.nodeCount());
		assertEquals(62, graph.edgeCount());
		NodeCursor nodes = graph.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			assertEquals(graph.getNodeName(node), Integer.toString(node.index()+1));
			nodes.next();
		}
	}
	}

