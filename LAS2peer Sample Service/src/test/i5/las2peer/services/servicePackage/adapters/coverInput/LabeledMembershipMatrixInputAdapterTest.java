package i5.las2peer.services.servicePackage.adapters.coverInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

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
		System.out.println(cover.toString());
	}

}
