package i5.las2peer.services.servicePackage.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularity;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Node;

public class CoverTest {

	private Cover cover;
	
	/*
	 * Creates a cover for the sawmill graph
	 */
	@Before 
	public void sawmillCoverSetUp() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Matrix memberships = new Basic2DMatrix(graph.nodeCount(), 5);
		for(int i=0; i<memberships.rows(); i++) {
			for(int j=0; j<memberships.columns(); j++) {
				memberships.set(i, j, i*j);
			}
		}
		cover = new Cover(graph, memberships, null);
	}
	
	/*
	 * Tests cover normalization.
	 */
	@Test
	public void testNormalizeMemberships() {
		cover.normalizeMemberships();
		Matrix memberships = cover.getMemberships();
		/*
		 * First row must remain unchanged and sum up to zero precisely.
		 */
		double rowSum = 0;
		for(int j=0; j<memberships.columns(); j++) {
			rowSum += memberships.get(0, j);
		}
		assertEquals(0.0, rowSum, 0.0);
		/*
		 * Other rows must sum up to one.
		 */
		for(int i=1; i<memberships.rows(); i++) {
			rowSum = 0;
			for(int j=0; j<memberships.columns(); j++) {
				rowSum += memberships.get(i, j);
			}
			assertEquals(1.0, rowSum, 0.00001);
		}
	}
	
	/*
	 * Tests membership filtering.
	 */
	@Test
	public void testFilterMemberships() {
		cover.normalizeMemberships();
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		/*
		 * First row must remain unchanged and sum up to zero precisely.
		 */
		System.out.println(cover.toString());
		cover.filterMembershipsbyThreshold(0.25);
		System.out.println(cover.toString());
		assertEquals(0, cover.getMetricResults().size());
		assertEquals(2, cover.communityCount());
	}
	
	/*
	 * Tests return values of the belonging factors.
	 */
	@Test
	public void testGetBelongingFactor() {
		CustomGraph graph = cover.getGraph();
		Node[] nodes = graph.getNodeArray();
		for(int i=0; i<graph.nodeCount(); i++) {
			for(int j=0; j<cover.communityCount(); j++) {
				assertEquals(cover.getBelongingFactor(nodes[i], j), i*j, 0.0);
			}
		}
	}
	
	/*
	 * Tests the return value of the community count.
	 */
	@Test
	public void testCommunityCount() {
		assertEquals(cover.communityCount(), 5);
	}
	
	/*
	 * Tests the return value of the community indices of a node.
	 */
	@Test
	public void testGetCommunityIndices() {
		CustomGraph graph = cover.getGraph();
		Node[] nodes = graph.getNodeArray();
		List<Integer> indices = cover.getCommunityIndices(nodes[0]);
		assertTrue(indices.isEmpty());
		indices = cover.getCommunityIndices(nodes[1]);
		assertTrue(indices.size() == 4);
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
		memberships.set(0, 4, 4);
		CustomGraph graph = new CustomGraph();
		graph.createNode();
		Cover cover = new Cover(graph, memberships, Algorithm.UNDEFINED);
		cover.setRowEntriesBelowThresholdToZero(0, 3d);
		memberships = cover.getMemberships();
		assertEquals(0, memberships.get(0, 0), 0);
		assertEquals(0, memberships.get(0, 1), 0);
		assertEquals(0, memberships.get(0, 2), 0);
		assertEquals(3, memberships.get(0, 3), 0);
		assertEquals(4, memberships.get(0, 4), 0);
		System.out.println(memberships);
	}

}
