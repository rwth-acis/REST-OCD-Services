package i5.las2peer.services.servicePackage.adapters.coverInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Ignore;
import org.junit.Test;

public class CommunityMemberListsInputAdapterTest {

	@Test
	public void testOnSawmill() throws AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Cover cover;
		CoverInputAdapter adapter = new CommunityMemberListsInputAdapter(new FileReader(OcdTestConstants.sawmillGroundTruthCommunityMemberListxInputPath));
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
		CoverInputAdapter adapter = new CommunityMemberListsInputAdapter(new FileReader(OcdTestConstants.facebookGroundTruthCommunityMemberListxInputPath));
		cover = adapter.readCover(graph);
		assertEquals(193, cover.communityCount());
	}

}
