package i5.las2peer.services.ocd.adapters.coverInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverInput.CommunityMemberListsCoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Ignore;
import org.junit.Test;

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
	
	@Ignore
	@Test
	public void testOnFacebook() throws AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getFacebookGraph();
		System.out.println("Nodes: " + graph.nodeCount());
		System.out.println("Edges: " + graph.edgeCount());
		Cover cover;
		CoverInputAdapter adapter = new CommunityMemberListsCoverInputAdapter(new FileReader(OcdTestConstants.facebookGroundTruthCommunityMemberListxInputPath));
		cover = adapter.readCover(graph);
		assertEquals(193, cover.communityCount());
	}

}
