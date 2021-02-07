package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

public class DetectingOverlappingCommunitiesAlgorithmTest {

	@Test
	public void test() throws OcdAlgorithmException, InterruptedException, FileNotFoundException, AdapterException {
		if (SystemUtils.IS_OS_WINDOWS) { //TODO: Remove if clause once algorithm implemented for Linux
			OcdAlgorithm algo = new DetectingOverlappingCommunitiesAlgorithm();
			CustomGraph graph = OcdTestGraphFactory.getDocaTestGraph();
			System.out.println("Nodes " + graph.nodeCount() + " Edges " + graph.edgeCount());
			Cover cover = algo.detectOverlappingCommunities(graph);
			System.out.println(cover.toString());
		}
	}

}
