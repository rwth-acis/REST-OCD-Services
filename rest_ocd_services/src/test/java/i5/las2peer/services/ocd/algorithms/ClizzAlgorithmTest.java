package i5.las2peer.services.ocd.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.la4j.matrix.Matrix;

import org.graphstream.graph.Node;


public class ClizzAlgorithmTest {

	/*
	 * Tests the algorithm on the sawmill graph
	 */
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new ClizzAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmillbyFunctions() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		ClizzAlgorithm algo = new ClizzAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(ClizzAlgorithm.INFLUENCE_FACTOR_NAME, Double.toString(0.9));
		parameters.put(ClizzAlgorithm.MEMBERSHIPS_ITERATION_BOUND_NAME, Integer.toString(1000));
		parameters.put(ClizzAlgorithm.MEMBERSHIPS_PRECISION_FACTOR_NAME, Double.toString(0.001));
		algo.setParameters(parameters);
		Matrix distances = algo.calculateNodeDistances(graph);
		//System.out.println("Distances:");
		//System.out.println(distances);
		Map<Node, Double> leadershipValues = algo.calculateLeadershipValues(graph, distances);
		//System.out.println("Leaderships:");
		//System.out.println(leadershipValues);
		Map<Node, Integer> leaders = algo.determineCommunityLeaders(graph, distances, leadershipValues);
		//System.out.println("Leaders:");
		//System.out.println(leaders);
		for(Node leader : leaders.keySet()) {
			//System.out.println(graph.getNodeName(leader));
		}
		Matrix memberships = algo.calculateMemberships(graph, leaders);
		//System.out.println("Memberships:");
		//System.out.println(memberships);
		Cover cover = new Cover(graph, memberships);
		cover.setCreationMethod(new CoverCreationLog(algo.getAlgorithmType(), algo.getParameters(), algo.compatibleGraphTypes()));
		System.out.println(cover);
	}

	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		ClizzAlgorithm algo = new ClizzAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(ClizzAlgorithm.INFLUENCE_FACTOR_NAME, Double.toString(0.9));
		parameters.put(ClizzAlgorithm.MEMBERSHIPS_ITERATION_BOUND_NAME, Integer.toString(1000));
		parameters.put(ClizzAlgorithm.MEMBERSHIPS_PRECISION_FACTOR_NAME, Double.toString(0.001));
		algo.setParameters(parameters);
		Matrix distances = algo.calculateNodeDistances(graph);
		System.out.println("Distances:");
		System.out.println(distances);
		Map<Node, Double> leadershipValues = algo.calculateLeadershipValues(graph, distances);
		System.out.println("Leaderships:");
		System.out.println(leadershipValues);
		Map<Node, Integer> leaders = algo.determineCommunityLeaders(graph, distances, leadershipValues);
		System.out.println("Leaders:");
		System.out.println(leaders);
		Matrix memberships = algo.calculateMemberships(graph, leaders);
		System.out.println("Memberships:");
		System.out.println(memberships);
		Cover cover = new Cover(graph, memberships);
		cover.setCreationMethod(new CoverCreationLog(algo.getAlgorithmType(), algo.getParameters(), algo.compatibleGraphTypes()));
		assertEquals(2, cover.communityCount());
		for(int i=0; i<graph.getNodeCount(); i++) {
			double belongingFac;
			switch(i) {
				case 0:
					belongingFac = 0;
					break;
				case 1:
					belongingFac = 0.126;
					break;
				case 2:
					belongingFac = 0.064;
					break;
				case 3:
					belongingFac = 0.064;
					break;
				case 4:
					belongingFac = 0.126;
					break;
				case 5: 
					belongingFac = 1;
					break;
				case 6:
					belongingFac = 1;
					break;
				case 7:
					belongingFac = 1;
					break;
				case 8:
					belongingFac = 1;
					break;
				case 9:
					belongingFac = 1;
					break;
				case 10:
					belongingFac = 0.314;
					break;
				default:
					belongingFac = -1;
					break;
			}
			assertTrue(Math.abs(belongingFac - cover.getBelongingFactor(graph.nodes().toArray(Node[]::new)[i], 0)) < 0.003
					|| Math.abs(belongingFac - cover.getBelongingFactor(graph.nodes().toArray(Node[]::new)[i], 1)) < 0.003);
			System.out.println(cover);
		}
		
	}
	
}
