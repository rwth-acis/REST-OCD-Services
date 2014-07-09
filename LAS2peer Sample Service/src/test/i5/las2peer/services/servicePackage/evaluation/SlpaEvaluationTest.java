package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SlpaEvaluationTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 * @throws IOException 
	 * @throws OcdAlgorithmException 
	 */
	@Ignore
	@Test
	public void testSleaperListenerLabelPropagationAlgo() throws IOException, OcdAlgorithmException
	{
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		OcdAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
		System.out.println();
		System.out.println("Overlapping Nodes:");
		CustomGraph coverGraph = cover.getGraph();
		NodeCursor nodes = coverGraph.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			List<Integer> communities = cover.getCommunityIndices(node);
			if(communities.size() > 1) {
				System.out.println(coverGraph.getNodeName(node) + " " + communities);
			}
			nodes.next();
		}
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(EvaluationConstants.slpaSiamDmLabeledMembershipMatrixOutputPath);
		adapter.writeCover(cover);
	}
}
