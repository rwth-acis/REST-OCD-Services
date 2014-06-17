package i5.las2peer.services.servicePackage.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

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
		cover = new Cover(graph, memberships);
	}
	
	/*
	 * Tests cover normalization.
	 */
	@Test
	public void testDoNormalize() {
		cover.doNormalize();
		Matrix memberships = cover.getMemberships();
		// First row must remain unchanged and sum up to zero precisely.
		double rowSum = 0;
		for(int j=0; j<memberships.columns(); j++) {
			rowSum += memberships.get(0, j);
		}
		// Other rows must sum up to one.
		assertEquals(rowSum, 0.0, 0.0);
		for(int i=1; i<memberships.rows(); i++) {
			rowSum = 0;
			for(int j=0; j<memberships.columns(); j++) {
				rowSum += memberships.get(i, j);
			}
			assertEquals(rowSum, 1.0, 0.00001);
		}
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
		List<Integer> indicesA = cover.getCommunityIndices(0);
		List<Integer> indicesB = cover.getCommunityIndices(nodes[0]);
		assertTrue(indicesA.isEmpty());
		assertTrue(indicesB.isEmpty());
		indicesA = cover.getCommunityIndices(1);
		indicesB = cover.getCommunityIndices(nodes[1]);
		assertTrue(indicesA.size() == 4);
		assertTrue(indicesB.size() == 4);
		for(int i=1; i<= 4; i++) {
			assertTrue(indicesA.contains(i));
			assertTrue(indicesB.contains(i));
		}
	}

}
