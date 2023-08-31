package i5.las2peer.services.ocd.adapters.coverInput;

import static org.junit.jupiter.api.Assertions.*;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import org.graphstream.graph.Node;

public class LabeledMembershipMatrixCoverInputAdapterTest {

	/*
	 * Tests the cover input on sawmill.
	 */
	@Test
	public void testReadCoverOnSawmill() throws AdapterException, FileNotFoundException {
		Cover cover;
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(new FileReader(OcdTestConstants.sawmillArbitraryLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		cover = adapter.readCover(graph);
		assertEquals(4, cover.communityCount());
		assertEquals(graph, cover.getGraph());
		Iterator<Node> nodes = graph.iterator();
		Node node14 = null;
		while(nodes.hasNext()) {
			Node node = nodes.next();
			if(graph.getNodeName(node).equals("14")) {
				node14 = node;
			}
		}
		assertNotNull(node14);
		assertEquals(0.629, cover.getBelongingFactor(node14, 0), 0.001);
		assertEquals(0.371, cover.getBelongingFactor(node14, 1), 0.001);
		assertEquals(0.0, cover.getBelongingFactor(node14, 2), 0.001);
		assertEquals(0.0, cover.getBelongingFactor(node14, 3), 0.001);
		System.out.println(cover.toString());
	}
}
