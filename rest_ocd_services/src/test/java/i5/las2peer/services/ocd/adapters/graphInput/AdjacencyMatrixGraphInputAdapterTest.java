package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;

public class AdjacencyMatrixGraphInputAdapterTest {

	@Test
	public void readGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter inputAdapter =
				new AdjacencyMatrixGraphInputAdapter(new FileReader(OcdTestConstants.sawmillAdjacencyMatrixInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		assertEquals(36, graph.getNodeCount());
		assertEquals(62, graph.getEdgeCount());
		Iterator<Node> nodes = graph.iterator();
		while(nodes.hasNext()) {
			Node node = nodes.next();
			assertEquals(graph.getNodeName(node), Integer.toString(node.getIndex()+1));
		}
	}
}

