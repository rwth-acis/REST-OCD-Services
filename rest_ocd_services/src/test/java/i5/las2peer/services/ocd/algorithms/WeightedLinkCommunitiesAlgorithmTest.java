package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.WeightedLinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import org.junit.Test;

import org.graphstream.graph.Edge;

import java.io.IOException;
import java.util.Iterator;

public class WeightedLinkCommunitiesAlgorithmTest {

	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException, OcdMetricException, IOException {
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new WeightedLinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

	/*
	 * Tests link communities on the test graph given in the original paper.
	 */
	@Test
	public void testOnLinkCommunitiesTestGraph() throws OcdAlgorithmException, InterruptedException, OcdMetricException, IOException {
		CustomGraph graph = OcdTestGraphFactory.getLinkCommunitiesTestGraph();
		Iterator<Edge> edges = graph.edges().iterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			Edge reverseEdge = edge.getTargetNode().getEdgeToward(edge.getSourceNode());
			if (reverseEdge == null || edge.getIndex() < reverseEdge.getIndex()) {
				System.out.println("Edge " + edge.getIndex() + ": "
						+ edge.getSourceNode() + " -> " + edge.getTargetNode());
			}
		}
		OcdAlgorithm algo = new WeightedLinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

}
