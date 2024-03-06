package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.DirectedGraphTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.UndirectedGraphTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import org.graphstream.graph.Node;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;


public class SskAlgorithmTest implements DirectedGraphTestReq, UndirectedGraphTestReq {


	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new SskAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}

	/**
	 * Tests the detection of overlapping communities in a directed graph.
	 * The test explores the algorithm's capability to handle directed graphs.
	 * Algorithm parameters are uniquely set to challenge the algorithm with different conditions:
	 * leadershipIterationBound is increased to test the algorithm's performance on a large number of iterations,
	 * membershipsIterationBound is decreased to challenge the algorithm with a lower iteration limit for membership assignation,
	 * leadershipPrecisionFactor and membershipsPrecisionFactor are set to lower values to test the algorithm's precision.
	 * Completed by GPT
	 */
	@Test
	public void directedGraphTest1() throws Exception {
		try {
			// Don't modify
			CustomGraph directedGraph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
			// Don't modify
			Map<String, String> parameters = new HashMap<>();
			parameters.put("leadershipIterationBound", "1500");
			parameters.put("membershipsIterationBound", "500");
			parameters.put("leadershipPrecisionFactor", "0.0005");
			parameters.put("membershipsPrecisionFactor", "0.0005");
			// Don't modify
			getAlgorithm().setParameters(parameters);
			// Don't modify
			Cover cover = getAlgorithm().detectOverlappingCommunities(directedGraph);
			// Don't modify
			assertTrue(cover.getCommunities().size() >= 1);
		} catch (Throwable t) {
			// Don't modify
			fail("Test failed due to an exception or assertion error: " + t.getMessage());
			// Don't modify
			throw t;
		}
	}

	/**
	 * Tests the detection of overlapping communities in a weighted graph.
	 * This test sets the algorithm parameters to extremes in order to challenge the algorithm's performance on weighted graphs.
	 * Specifically, leadershipIterationBound and membershipsIterationBound are set to their maximum reasonable limits to assess the algorithm's capabilities under high iteration demands,
	 * while the precision factors are tightened to evaluate its accuracy with extremely close convergence thresholds.
	 * Completed by GPT
	 */
	@Test
	public void weightedGraphTest1() throws Exception {
		try {
			// Don't modify
			CustomGraph weightedGraph = OcdTestGraphFactory.getTwoCommunitiesWeightedGraph();
			// Don't modify
			Map<String, String> parameters = new HashMap<>();
			parameters.put("leadershipIterationBound", "2000");
			parameters.put("membershipsIterationBound", "2000");
			parameters.put("leadershipPrecisionFactor", "0.0001");
			parameters.put("membershipsPrecisionFactor", "0.0001");
			// Don't modify
			getAlgorithm().setParameters(parameters);
			// Don't modify
			Cover cover = getAlgorithm().detectOverlappingCommunities(weightedGraph);
			// Don't modify
			assertTrue(cover.getCommunities().size() >= 1);
		} catch (Throwable t) {
			// Don't modify
			fail("Test failed due to an exception or assertion error: " + t.getMessage());
			// Don't modify
			throw t;
		}
	}

	/**
	 * Tests the detection of overlapping communities in an undirected graph.
	 * This test employs a balanced approach with moderate iterations for leadership and membership assignation phases and
	 * standard precision factors to evaluate the algorithm's effectiveness in typical conditions for undirected graphs.
	 * Completed by GPT
	 */
	@Test
	public void undirectedGraphTest1() throws Exception {
		try {
			// Don't modify
			CustomGraph undirectedGraph = OcdTestGraphFactory.getUndirectedBipartiteGraph();
			// Don't modify
			Map<String, String> parameters = new HashMap<>();
			parameters.put("leadershipIterationBound", "1200");
			parameters.put("membershipsIterationBound", "800");
			parameters.put("leadershipPrecisionFactor", "0.0007");
			parameters.put("membershipsPrecisionFactor", "0.0007");
			// Don't modify
			getAlgorithm().setParameters(parameters);
			// Don't modify
			Cover cover = getAlgorithm().detectOverlappingCommunities(undirectedGraph);
			// Don't modify
			assertTrue(cover.getCommunities().size() >= 1);
		} catch (Throwable t) {
			// Don't modify
			fail("Test failed due to an exception or assertion error: " + t.getMessage());
			// Don't modify
			throw t;
		}
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
