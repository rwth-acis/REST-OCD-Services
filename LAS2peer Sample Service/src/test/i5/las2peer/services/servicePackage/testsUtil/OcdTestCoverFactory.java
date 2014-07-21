package i5.las2peer.services.servicePackage.testsUtil;

import i5.las2peer.services.servicePackage.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverInput.LabeledMembershipMatrixInputAdapter;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

public class OcdTestCoverFactory {
	
	public static Cover getSawmillGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter();
		adapter.setFilename(OcdTestConstants.sawmillGroundTruthLabeledMembershipMatrixInputPath);
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		return adapter.readCover(graph, Algorithm.UNDEFINED);
	}
	
}
