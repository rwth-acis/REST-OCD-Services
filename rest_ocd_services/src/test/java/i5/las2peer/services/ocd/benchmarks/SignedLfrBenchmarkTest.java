package i5.las2peer.services.ocd.benchmarks;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestCoverFactory;

import y.base.Edge;
import y.base.EdgeCursor;

/* 
 * @author YLi
 */
public class SignedLfrBenchmarkTest {
	@Test
	public void testCreateGroundTruth() throws OcdBenchmarkException, InterruptedException {
		SignedLfrBenchmark model = new SignedLfrBenchmark(100, 3, 6, 0.1, -2.0, -1.0, 10, 40, 5, 2, 0.1, 0.1);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(100, graph.nodeCount());
		System.out.println(cover.toString());
		int nodeCount = graph.nodeCount();
		int overlappingNodeCount = 0;
		int communityCount = cover.communityCount();
		Matrix membership = cover.getMemberships();
		for (int i = 0; i < nodeCount; i++) {
			int belongingCommunity = 0;
			for (int j = 0; j < communityCount; j++) {
				if (membership.get(i, j) != 0) {
					belongingCommunity++;
				}
			}
			if (belongingCommunity == 2) {
				overlappingNodeCount++;
			}
		}
		assertEquals(5, overlappingNodeCount);
	}

	@Test
	public void testSetWeightSign() throws Exception {
		SignedLfrBenchmark model = new SignedLfrBenchmark();
		Cover cover = OcdTestCoverFactory.getLfrUnweightedCover();
		Cover signedCover = model.setWeightSign(cover, 0.2, 0.2);
		CustomGraph graph = signedCover.getGraph();
		Matrix membership = cover.getMemberships();
		System.out.println(cover.toString());
		System.out.println(signedCover.toString());
		int communityCount = membership.columns();
		EdgeCursor edges = graph.edges();
		Edge edge;
		int negIntraEdgeCount = 0;
		int posInterEdgeCount = 0;
		while (edges.ok()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.edge();
			double weight = graph.getEdgeWeight(edge);
			if (weight < 0) {
				for (int i = 0; i < communityCount; i++) {
					if (membership.get(edge.source().index(), i) * membership.get(edge.target().index(), i) != 0) {
						negIntraEdgeCount++;
						break;
					}
				}
			} else if (weight > 0) {
				double sum = 0;
				for (int i = 0; i < communityCount; i++) {
					sum = sum + membership.get(edge.source().index(), i) * membership.get(edge.target().index(), i);
				}
				if (sum == 0) {
					posInterEdgeCount++;
				}

			}
			edges.next();
		}
		System.out.println("neg. intra-edges count: " + negIntraEdgeCount);
		System.out.println("pos. inter-edges count: " + posInterEdgeCount);
		assertEquals(6, negIntraEdgeCount);
		assertEquals(1, posInterEdgeCount);
	}

}
