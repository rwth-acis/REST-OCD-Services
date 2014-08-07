package i5.las2peer.services.servicePackage.adapters.coverInput;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

public class LabeledMembershipMatrixInputAdapterTest {

	/*
	 * Tests the cover input on sawmill.
	 */
	@Test
	public void testReadCoverOnSawmill() throws AdapterException, FileNotFoundException {
		Cover cover;
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter(new FileReader(OcdTestConstants.sawmillArbitraryLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		cover = adapter.readCover(graph);
		assertEquals(4, cover.communityCount());
		assertEquals(graph, cover.getGraph());
		NodeCursor nodes = graph.nodes();
		Node node14 = null;
		while(nodes.ok()) {
			Node node = nodes.node();
			if(graph.getNodeName(node).equals("14")) {
				node14 = node;
			}
			nodes.next();
		}
		assertNotNull(node14);
		assertEquals(0.629, cover.getBelongingFactor(node14, 0), 0.001);
		assertEquals(0.371, cover.getBelongingFactor(node14, 1), 0.001);
		assertEquals(0.0, cover.getBelongingFactor(node14, 2), 0.001);
		assertEquals(0.0, cover.getBelongingFactor(node14, 3), 0.001);
		System.out.println(cover.toString());
	}
}
