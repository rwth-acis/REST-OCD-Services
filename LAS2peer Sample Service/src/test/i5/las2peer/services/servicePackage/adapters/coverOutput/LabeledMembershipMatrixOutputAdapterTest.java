package i5.las2peer.services.servicePackage.adapters.coverOutput;

import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SSKAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import java.io.IOException;

import org.junit.Test;

public class LabeledMembershipMatrixOutputAdapterTest {

	@Test
	public void test() throws OcdAlgorithmException, IOException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new SSKAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(OcdTestConstants.sawmillLabeledMembershipMatrixOutputPath);
		adapter.writeCover(cover);
		System.out.println(cover.toString());
	}

}
