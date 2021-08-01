package i5.las2peer.services.ocd.testsUtils;

import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.LabeledMembershipMatrixCoverInputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

public class OcdTestCoverFactory {
	
	public static Cover getSawmillGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(new FileReader(OcdTestConstants.sawmillGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanClizzGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(new FileReader(OcdTestConstants.newmanClizzGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanClizzGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanLinkGroundTruth() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(new FileReader(OcdTestConstants.newmanLinkGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanLinkGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanLinkCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(new FileReader(OcdTestConstants.newmanLinkCoverLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanLinkGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	
	public static Cover getNewmanClizzCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(new FileReader(OcdTestConstants.newmanClizzCoverLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getNewmanLinkGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>()));
		return cover;
	}
	

	//@author: YLi
	public static Cover getLfrUnweightedCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(
				new FileReader(OcdTestConstants.lfrGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getLfrGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
				new HashSet<GraphType>()));
		return cover;
	}

	//@author: YLi
	public static Cover getSignedLfrCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(
				new FileReader(OcdTestConstants.signedLfrGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
				new HashSet<GraphType>()));
		return cover;
	}

	//@author: YLi
	public static Cover getSignedLfrSixNodesCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(
				new FileReader(OcdTestConstants.signedLfrSixNodesGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrSixNodesGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
				new HashSet<GraphType>()));
		return cover;
	}

	//@author: YLi
	public static Cover getSignedLfrBlurredCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(
				new FileReader(OcdTestConstants.signedLfrBlurredGroundTruthLabeledMembershipMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrBlurredGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
				new HashSet<GraphType>()));
		return cover;
	}

	//@author: YLi
	public static Cover getgetSloveneParliamentaryPartyCover() throws Exception {
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter(
				new FileReader(OcdTestConstants.sloveneParliamentaryPartyCommunityMemberMatrixInputPath));
		CustomGraph graph = OcdTestGraphFactory.getSloveneParliamentaryPartyGraph();
		Cover cover = adapter.readCover(graph);
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
				new HashSet<GraphType>()));
		return cover;
	}
}
