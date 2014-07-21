package i5.las2peer.services.servicePackage.adapters.coverInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

public class LabeledMembershipMatrixInputAdapterTest {

	/*
	 * Tests the cover input on sawmill.
	 */
	@Test
	public void testReadCoverOnSawmill() {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter();
		adapter.setFilename(OcdTestConstants.sawmillArbitraryLabeledMembershipMatrixInputPath);
		Cover cover;
		try {
			CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
			Algorithm algorithm = Algorithm.UNDEFINED;
			cover = adapter.readCover(graph, algorithm);
			assertEquals(4, cover.communityCount());
			assertEquals(algorithm, cover.getAlgorithm());
			assertEquals(graph, cover.getGraph());
			System.out.println(cover.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
