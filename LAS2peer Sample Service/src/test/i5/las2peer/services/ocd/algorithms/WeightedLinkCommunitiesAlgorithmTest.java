package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.WeightedLinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

import y.base.Edge;
import y.base.EdgeCursor;

public class WeightedLinkCommunitiesAlgorithmTest {

	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException {
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
	public void testOnLinkCommunitiesTestGraph() throws OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getLinkCommunitiesTestGraph();
		EdgeCursor edges = graph.edges();
		while (edges.ok()) {
			Edge edge = edges.edge();
			Edge reverseEdge = edge.target().getEdgeTo(edge.source());
			if (reverseEdge == null || edge.index() < reverseEdge.index()) {
				System.out.println("Edge " + edge.index() + ": "
						+ edge.source() + " -> " + edge.target());
			}
			edges.next();
		}
		OcdAlgorithm algo = new WeightedLinkCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

}
