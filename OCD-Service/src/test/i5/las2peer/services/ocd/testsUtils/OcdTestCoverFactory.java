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
	
}
