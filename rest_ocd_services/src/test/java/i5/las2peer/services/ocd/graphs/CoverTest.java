package i5.las2peer.services.ocd.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.metrics.ExtendedModularityMetric;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.List;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.graph.Node;


public class CoverTest {

	private Cover cover;
	
	/*
	 * Creates a cover for the sawmill graph
	 */
	@BeforeEach
	public void sawmillCoverSetUp() throws AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Matrix memberships = new Basic2DMatrix(graph.getNodeCount(), 5);
		for(int i=0; i<memberships.rows(); i++) {
			for(int j=0; j<memberships.columns(); j++) {
				memberships.set(i, j, i*j);
			}
		}
		cover = new Cover(graph, memberships);
		System.out.println(cover);
	}
	
	/*
	 * Tests cover normalization.
	 */
	@Test
	public void testMembershipNormalization() {
		Matrix memberships = cover.getMemberships();
		double rowSum;
		for(int i=0; i<memberships.rows(); i++) {
			rowSum = 0;
			for(int j=0; j<memberships.columns(); j++) {
				//////////// TODO test
				System.out.println("(" + i + ", " + j + ") = " + memberships.get(i, j));
				/////////
				rowSum += memberships.get(i, j);
			}
			assertEquals(1.0, rowSum, 0.00001);
		}
	}
	
	/*
	 * Tests membership filtering.
	 */
	@Test
	public void testFilterMemberships() throws OcdMetricException, InterruptedException, OcdAlgorithmException {
		StatisticalMeasure metric = new ExtendedModularityMetric();
		metric.measure(cover);
		/*
		 * First row must remain unchanged and sum up to zero precisely.
		 */
		System.out.println(cover.toString());
		cover.filterMembershipsbyThreshold(0.25);
		System.out.println(cover.toString());
		assertEquals(0, cover.getMetrics().size());
		assertEquals(3, cover.communityCount());
	}
	
	/*
	 * Tests return values of the belonging factors.
	 */
	@Test
	public void testGetBelongingFactor() {
		CustomGraph graph = cover.getGraph();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		for(int i=0; i<graph.getNodeCount(); i++) {
			double rowSum = 0;
			for(int j=0; j<cover.communityCount() - 1; j++) {
				rowSum += i*j;
			}
			if(rowSum == 0) {
				rowSum = 1;
			}
			for(int j=0; j<cover.communityCount(); j++) {
				if(j<cover.communityCount() - 1) {
					assertEquals(i*j / rowSum, cover.getBelongingFactor(nodes[i], j), 0.001);
				}
				else if (i>0) {
					assertEquals(0, cover.getBelongingFactor(nodes[i], j), 0.001);
				}
				else {
					assertEquals(1, cover.getBelongingFactor(nodes[i], j), 0.001);
				}
			}
		}
	}
	
	/*
	 * Tests the return value of the community count.
	 */
	@Test
	public void testCommunityCount() {
		assertEquals(cover.communityCount(), 6);
	}
	
	/*
	 * Tests the return value of the community indices of a node.
	 */
	@Test
	public void testGetCommunityIndices() {
		CustomGraph graph = cover.getGraph();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		List<Integer> indices = cover.getCommunityIndices(nodes[0]);
		assertEquals(1, indices.size());
		assertEquals(5, (int)indices.get(0));
		indices = cover.getCommunityIndices(nodes[1]);
		assertEquals(4, indices.size());
		for(int i=1; i<= 4; i++) {
			assertTrue(indices.contains(i));
		}
	}
	
	@Test
	public void testSetEntriesBelowThresholdToZero() {
		Matrix memberships = new CCSMatrix(1, 5);
		memberships.set(0, 1, 1);
		memberships.set(0, 2, 2);
		memberships.set(0, 3, 3);
		memberships.set(0, 4, 3);
		CustomGraph graph = new CustomGraph();
		graph.addNode("1");
		Cover cover = new Cover(graph, memberships);
		System.out.println(cover);
		cover.setRowEntriesBelowThresholdToZero(memberships, 0, 0.3);
		assertEquals(0, memberships.get(0, 0), 0);
		assertEquals(0, memberships.get(0, 1), 0);
		assertEquals(0, memberships.get(0, 2), 0);
		assertEquals(0.3333, memberships.get(0, 3), 0.001);
		assertEquals(0.3333, memberships.get(0, 4), 0.001);
		System.out.println(memberships);
	}

}
