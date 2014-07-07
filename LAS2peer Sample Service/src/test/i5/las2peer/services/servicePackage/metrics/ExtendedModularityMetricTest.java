package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.algorithms.AlgorithmFactory;
import i5.las2peer.services.servicePackage.algorithms.OverlappingCommunityDetectionAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.ListenerRuleCommand;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.PopularityListenerRule;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.SpeakerRuleCommand;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.UniformSpeakerRule;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

public class ExtendedModularityMetricTest {

	/*
	 * Assures that modularity is 0 if only one community exists.
	 */
	@Test
	public void testExtendedModularityWithOneCommunity() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Matrix memberships = new Basic2DMatrix(graph.nodeCount(), 1);
		for(int i=0; i<memberships.rows(); i++) {
			memberships.set(i, 0, 1);
		}
		Cover cover = new Cover(graph, memberships, null);
		ExtendedModularityMetric metric = new ExtendedModularityMetric();
		metric.measure(cover);
		System.out.println("1 Community");
		System.out.println(cover.getMetricResult(Metric.ExtendedModularityMetric));
	}
	
	@Test
	public void testExtendedModularityOnSawmillSLPA() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		AlgorithmFactory factory = AlgorithmFactory.getAlgorithmFactory();
		SpeakerRuleCommand speakerRule = new UniformSpeakerRule();
		ListenerRuleCommand listenerRule = new PopularityListenerRule();
		OverlappingCommunityDetectionAlgorithm algo = factory.getSpeakerListenerLabelPropagationAlgorithm(100, 0.04, speakerRule, listenerRule);
		Cover cover = algo.detectOverlappingCommunities(graph);
		ExtendedModularityMetric metric = new ExtendedModularityMetric();
		metric.measure(cover);
		System.out.println("Sawmill SLPA");
		System.out.println(cover.getMetricResult(Metric.ExtendedModularityMetric));
	}

}
