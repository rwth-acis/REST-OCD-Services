package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SpeakerListenerLabelPropagationAlgorithmTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 */
	@Test
	public void testSleaperListenerLabelPropagationAlgo()
	{
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		SpeakerListenerLabelPropagationAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
}
