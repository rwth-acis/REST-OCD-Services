package i5.las2peer.services.ocd.adapters.coverInput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CommunityMemberListsInputAdapterTest {

	@Test
	public void testOnSawmill() throws AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Cover cover;
		CoverInputAdapter adapter = new CommunityMemberListsCoverInputAdapter(new FileReader(OcdTestConstants.sawmillGroundTruthCommunityMemberListxInputPath));
		cover = adapter.readCover(graph);
		assertEquals(3, cover.communityCount());
		System.out.println(cover);
	}
	
	@Disabled
	@Test
	public void testOnFacebook() throws AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getFacebookGraph();
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());
		Cover cover;
		CoverInputAdapter adapter = new CommunityMemberListsCoverInputAdapter(new FileReader(OcdTestConstants.facebookGroundTruthCommunityMemberListxInputPath));
		cover = adapter.readCover(graph);
		assertEquals(193, cover.communityCount());
	}

}
