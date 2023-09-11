package i5.las2peer.services.ocd.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import org.graphstream.graph.Node;


public class SSKAlgorithmTest implements DirectedGraphTestReq {


	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new SskAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}


	/*
	 * Tests the influence calculation random walk on directed aperiodic two communities.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testExecuteRandomWalkOnDirectedAperiodicTwoCommunities() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SskAlgorithm algo = new SskAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		//System.out.println("Random Walk Vector Directed Aperiodic Communities:");
		//System.out.println(totalInfluences);
	}

	/*
	 * Tests the global leader detection on aperiodic two communities.
	 * Node 0 is the only leader detected.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testDetermineGlobalLeadersOnAperiodicTwoCommunities() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		SskAlgorithm algo = new SskAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		assertEquals(1, globalLeaders.size());
		assertTrue(globalLeaders.keySet().contains(nodes[0]));
		//System.out.println("Global Leaders:");
		//System.out.println(globalLeaders);
	}
	
	/*
	 * Tests the global leader detection on sawmill.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testDetermineGlobalLeadersOnSawmill() throws AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SskAlgorithm algo = new SskAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		assertEquals(3, globalLeaders.size());
		assertEquals(3, globalLeaders.values().size());
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		assertTrue(globalLeaders.keySet().contains(nodes[11]));
		assertTrue(globalLeaders.keySet().contains(nodes[26]));
		assertTrue(globalLeaders.keySet().contains(nodes[30]));
//		assertTrue(globalLeaders.keySet().contains(nodes[35]));
		//System.out.println("Global Leaders:");
		//System.out.println(globalLeaders);
	}
	
	/*
	 * Tests the global leader detection on directed aperiodic two communities.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testDetermineGlobalLeadersOnDirectedAperiodicTwoCommunities() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SskAlgorithm algo = new SskAlgorithm();
		Matrix transitionMatrix = algo.calculateTransitionMatrix(graph);
		Vector totalInfluences = algo.executeRandomWalk(transitionMatrix);
		Map<Node, Integer> globalLeaders = algo.determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		assertEquals(7, globalLeaders.size());
		Node[] nodes = graph.nodes().toArray(Node[]::new);
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
	@Disabled //TODO: remove 555
	@Test
	public void testInitMembershipMatrixOnDirectedAperiodicTwoCommunities() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SskAlgorithm algo = new SskAlgorithm();
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
				assertEquals(1d/initialMemberships.columns(), initialMemberships.get(i, j), 0.0001);
			}
		}
		System.out.println("Initial Memberships");
		System.out.println(initialMemberships);
	}
	
	/*
	 * Tests the membership calculation coefficient matrix initialization on directed aperiodic two communities.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testInitMembershipCoefficientMatrixOnDirectedAperiodicTwoCommunities() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SskAlgorithm algo = new SskAlgorithm();
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
	@Disabled //TODO: remove 555
	@Test
	public void testSSKALgorithmOnDirectedAperiodicTwoCommunitiesGraph() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		SskAlgorithm algo = new SskAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	/*
	 * Test the community detection on sawmill.
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testSSKALgorithmOnSawmill() throws AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SskAlgorithm algo = new SskAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

}
