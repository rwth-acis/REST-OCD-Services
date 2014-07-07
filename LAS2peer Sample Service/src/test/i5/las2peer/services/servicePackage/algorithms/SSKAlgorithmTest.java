package i5.las2peer.services.servicePackage.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import y.base.Node;

public class SSKAlgorithmTest {
	
	/*
	 * Tests the transition matrix calculation on directed aperiodic two communities.
	 */
	@Ignore
	@Test
	public void testCalculateTransitionMatrixOnDirectedAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		System.out.println("Transition Matrix:");
		System.out.println(transitionMatrix);
	}
	
	/*
	 * Tests the transition matrix calculation on aperiodic two communities.
	 */
	@Ignore
	@Test
	public void testCalculateTransitionMatrixOnAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		System.out.println("Transition Matrix:");
		System.out.println(transitionMatrix);
	}
	
	/*
	 * Tests the influence calculation random walk on sawmill.
	 */
	@Ignore
	@Test
	public void testExecuteRandomWalkOnSawmill() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		System.out.println("Random Walk Vector:");
		System.out.println(totalInfluences);
	}
	
	/*
	 * Tests the influence calculation random walk on directed aperiodic two communities.
	 */
	@Test
	public void testExecuteRandomWalkOnDirectedAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		System.out.println("Random Walk Vector Directed Aperiodic Communities:");
		System.out.println(totalInfluences);
	}

	/*
	 * Tests the global leader detection on aperiodic two communities.
	 * Node 0 is the only leader detected.
	 */
	@Test
	public void testDetermineGlobalLeadersOnAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		Node[] nodes = graph.getNodeArray();
		assertEquals(1, globalLeaders.size());
		assertTrue(globalLeaders.keySet().contains(nodes[0]));
		System.out.println("Global Leaders:");
		System.out.println(globalLeaders);
	}
	
	/*
	 * Tests the global leader detection on aperiodic two communities.
	 */
	@Test
	public void testDetermineGlobalLeadersOnSawmill() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		assertEquals(4, globalLeaders.size());
		assertEquals(4, globalLeaders.values().size());
		Node[] nodes = graph.getNodeArray();
		assertTrue(globalLeaders.keySet().contains(nodes[11]));
		assertTrue(globalLeaders.keySet().contains(nodes[26]));
		assertTrue(globalLeaders.keySet().contains(nodes[30]));
		assertTrue(globalLeaders.keySet().contains(nodes[35]));
		System.out.println("Global Leaders:");
		System.out.println(globalLeaders);
	}
	
	/*
	 * Tests the global leader detection on directed aperiodic two communities.
	 */
	@Test
	public void testDetermineGlobalLeadersOnDirectedAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		assertEquals(7, globalLeaders.size());
		Node[] nodes = graph.getNodeArray();
		assertTrue(globalLeaders.keySet().contains(nodes[0]));
		assertTrue(globalLeaders.keySet().contains(nodes[5]));
		assertTrue(globalLeaders.keySet().contains(nodes[6]));
		assertTrue(globalLeaders.keySet().contains(nodes[7]));
		assertTrue(globalLeaders.keySet().contains(nodes[8]));
		assertTrue(globalLeaders.keySet().contains(nodes[9]));
		assertTrue(globalLeaders.keySet().contains(nodes[10]));
		assertTrue(globalLeaders.get(nodes[5]) == globalLeaders.get(nodes[10]));
		System.out.println("Global Leaders:");
		System.out.println(globalLeaders);
	}
	
	/*
	 * Tests the membership matrix initialization on directed aperiodic two communities.
	 */
	@Test
	public void testInitMembershipMatrixOnDirectedAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		Matrix initialMemberships = algo.initMembershipMatrix(graph, globalLeaders);
		/*
		 * Nodes 0 and 5 to 10 are leaders.
		 */
		assertEquals(1, initialMemberships.get(0, 0), 0);
		assertEquals(1, initialMemberships.get(5, 1), 0);
		assertEquals(1, initialMemberships.get(6, 2), 0);
		assertEquals(1, initialMemberships.get(7, 3), 0);
		assertEquals(1, initialMemberships.get(8, 4), 0);
		assertEquals(1, initialMemberships.get(9, 5), 0);
		assertEquals(1, initialMemberships.get(10, 1), 0);
		/*
		 * The non leaders nodes must have uniform entries.
		 */
		for(int i=1; i<5; i++) {
			for(int j=0; j<initialMemberships.columns(); j++) {
				assertEquals(1d/(double)initialMemberships.columns(), initialMemberships.get(i, j), 0.0001);
			}
		}
		System.out.println("Initial Memberships");
		System.out.println(initialMemberships);
	}
	
	/*
	 * Tests the membership calculation coefficient matrix initialization on directed aperiodic two communities.
	 */
	@Test
	public void testInitMembershipCoefficientMatrixOnDirectedAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		Matrix membershipCoefficients = algo.initMembershipCoefficientMatrix(graph, globalLeaders);
		/*
		 * Nodes 6 to 9 have no out edges
		 */
		for(int i=6; i<= 9; i++) {
			assertEquals(0, membershipCoefficients.getColumn(i).fold(Vectors.mkInfinityNormAccumulator()), 0);
		}
		/*
		 * Node 5 has no in edges
		 */
		assertEquals(0, membershipCoefficients.getRow(5).fold(Vectors.mkInfinityNormAccumulator()), 0);
		/*
		 * Node 5 has out edges to nodes 6 to 10
		 */
		for(int i=6; i<= 10; i++) {
			assertEquals(1d/5d, membershipCoefficients.get(i, 5), 0.0001);
		}
		System.out.println("Membership Calculation Coefficients");
		System.out.println(membershipCoefficients);
	}
	
	/*
	 * Test the community detection on aperiodic two communities.
	 */
	@Test
	public void testSSKALgorithmOnDirectedAperiodicTwoCommunitiesGraph() {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	/*
	 * Test the community detection on sawmill.
	 */
	@Test
	public void testSSKALgorithmOnSawmill() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SSKAlgorithm algo = new SSKAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
}
