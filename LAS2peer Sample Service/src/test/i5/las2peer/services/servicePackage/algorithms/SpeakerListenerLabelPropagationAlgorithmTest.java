package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.PopularityListenerRule;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.UniformSpeakerRule;
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
		AlgorithmFactory factory = AlgorithmFactory.getAlgorithmFactory();
		SpeakerListenerLabelPropagationAlgorithm algo = 
				factory.getSpeakerListenerLabelPropagationAlgorithm(
						100, 0.04, new UniformSpeakerRule(), new PopularityListenerRule());
		Cover result = algo.detectOverlappingCommunities(graph);
		System.out.println(result.getMemberships());
	}
}
