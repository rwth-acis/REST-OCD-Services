package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import org.junit.Test;

public class LinkCommunitiesAlgorithmTest {

	@Test
	public void testOnSawmill() throws OcdAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new LinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

}
