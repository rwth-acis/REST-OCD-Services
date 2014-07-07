package i5.las2peer.services.servicePackage.adapters.coverOutput;

import static org.junit.Assert.*;

import java.io.IOException;

import i5.las2peer.services.servicePackage.algorithms.OverlappingCommunityDetectionAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SSKAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

public class LabeledMembershipMatrixOutputAdapterTest {

	@Test
	public void test() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OverlappingCommunityDetectionAlgorithm algo = new SSKAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(OcdTestConstants.sawmillLabeledMembershipMatrixOutputPath);
		try {
			adapter.writeCover(cover);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		System.out.println(cover.toString());
	}

}
