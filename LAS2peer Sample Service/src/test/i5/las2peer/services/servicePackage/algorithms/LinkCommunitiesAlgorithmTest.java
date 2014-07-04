package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;
import org.la4j.matrix.Matrix;

public class LinkCommunitiesAlgorithmTest {

	@Test
	public void testOnSawmill() {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		OverlappingCommunityDetectionAlgorithm algo = new LinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Memberships:");
		Matrix memberships = cover.getMemberships();
		System.out.println(memberships);
	}

}
