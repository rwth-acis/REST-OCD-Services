package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmFactory;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.PopularityListenerRule;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.UniformSpeakerRule;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularityMetric;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SLPAEvaluationTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 * @throws IOException 
	 */
	@Ignore
	@Test
	public void testSleaperListenerLabelPropagationAlgo() throws IOException
	{
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		AlgorithmFactory factory = AlgorithmFactory.getAlgorithmFactory();
		SpeakerListenerLabelPropagationAlgorithm algo = 
				factory.getSpeakerListenerLabelPropagationAlgorithm(
						100, 0.04, new UniformSpeakerRule(), new PopularityListenerRule());
		Cover result = algo.detectOverlappingCommunities(graph);
		System.out.println(result.getMemberships());
		System.out.println();
		StatisticalMeasure extendedModularity = new ExtendedModularityMetric();
		double modularity = extendedModularity.getValue(result);
		System.out.println("Modularity:");
		System.out.println(modularity);
		System.out.println();
		System.out.println("Overlapping Nodes:");
		CustomGraph coverGraph = result.getGraph();
		NodeCursor nodes = coverGraph.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			List<Integer> communities = result.getCommunityIndices(node);
			if(communities.size() > 1) {
				System.out.println(coverGraph.getNodeName(node) + " " + communities);
			}
			nodes.next();
		}
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(OcdTestConstants.slpaSiamDmLabeledMembershipMatrixOutputPath);
		adapter.writeCover(result);
	}
}
