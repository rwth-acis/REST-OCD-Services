package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

public class ClizzAlgorithmTest {

	/*
	 * Tests the algorithm on the sawmill graph
	 */
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new ClizzAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
}
