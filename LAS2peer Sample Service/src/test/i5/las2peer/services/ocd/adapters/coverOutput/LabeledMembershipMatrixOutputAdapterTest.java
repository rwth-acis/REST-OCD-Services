package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class LabeledMembershipMatrixOutputAdapterTest {

	@Test
	public void test() throws OcdAlgorithmException, IOException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new SskAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(OcdTestConstants.sawmillLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}

}
