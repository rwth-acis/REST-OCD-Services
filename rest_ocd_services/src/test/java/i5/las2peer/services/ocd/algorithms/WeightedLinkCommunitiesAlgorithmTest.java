package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import org.graphstream.graph.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class WeightedLinkCommunitiesAlgorithmTest implements UndirectedGraphTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new WeightedLinkCommunitiesAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}


	@Disabled //TODO: remove 555
	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new WeightedLinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

	/*
	 * Tests link communities on the test graph given in the original paper.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testOnLinkCommunitiesTestGraph() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getLinkCommunitiesTestGraph();
		Iterator<Edge> edges = graph.edges().iterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			Edge reverseEdge = edge.getTargetNode().getEdgeToward(edge.getSourceNode());
			if (reverseEdge == null || edge.getIndex() < reverseEdge.getIndex()) {
				//System.out.println("Edge " + edge.getIndex() + ":  + edge.getSourceNode() + " -> " + edge.getTargetNode());
			}
		}
		OcdAlgorithm algo = new WeightedLinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

}
