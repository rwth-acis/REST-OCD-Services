package i5.las2peer.services.ocd.testsUtil;

import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.LabeledMembershipMatrixInputAdapter;
import i5.las2peer.services.ocd.algorithms.AlgorithmLog;
import i5.las2peer.services.ocd.algorithms.AlgorithmType;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.graph.GraphType;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

public class OcdTestCoverFactory {
	
	public static Cover getSawmillGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter(new FileReader(OcdTestConstants.sawmillGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Cover cover = adapter.readCover(graph);
		cover.setAlgorithm(new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanClizzGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter(new FileReader(OcdTestConstants.newmanClizzGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanClizzGraph();
		Cover cover = adapter.readCover(graph);
		cover.setAlgorithm(new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanLinkGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter(new FileReader(OcdTestConstants.newmanLinkGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanLinkGraph();
		Cover cover = adapter.readCover(graph);
		cover.setAlgorithm(new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanLinkCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter(new FileReader(OcdTestConstants.newmanLinkCoverLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanLinkGraph();
		Cover cover = adapter.readCover(graph);
		cover.setAlgorithm(new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanClizzCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter(new FileReader(OcdTestConstants.newmanClizzCoverLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanLinkGraph();
		Cover cover = adapter.readCover(graph);
		cover.setAlgorithm(new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
}