package i5.las2peer.services.servicePackage.algorithms;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;

import y.base.Node;

public class ClizzAlgorithmTest {

	/*
	 * Tests the algorithm on the sawmill graph
	 */
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new ClizzAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmillbyFunctions() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		ClizzAlgorithm algo = new ClizzAlgorithm(0.9, 1000, 0.001);
		Matrix distances = algo.calculateNodeDistances(graph);
		System.out.println("Distances:");
		System.out.println(distances);
		Map<Node, Double> leadershipValues = algo.calculateLeadershipValues(graph, distances);
		System.out.println("Leaderships:");
		System.out.println(leadershipValues);
		Map<Node, Integer> leaders = algo.determineCommunityLeaders(graph, distances, leadershipValues);
		System.out.println("Leaders:");
		System.out.println(leaders);
		for(Node leader : leaders.keySet()) {
			System.out.println(graph.getNodeName(leader));
		}
		Matrix memberships = algo.calculateMemberships(graph, leaders);
		System.out.println("Memberships:");
		System.out.println(memberships);
		Cover cover = new Cover(graph, memberships);
		cover.setAlgorithm(new AlgorithmLog(algo.getAlgorithmType(), algo.getParameters(), algo.compatibleGraphTypes()));
		System.out.println(cover);
	}
	
	@Ignore
	@Test
	public void testOnSiam() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		OcdAlgorithm algo = new ClizzAlgorithm(0.9, 1000, 0.001);
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 0);
		System.out.println(cover.toString());
	}
	
	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		ClizzAlgorithm algo = new ClizzAlgorithm(0.9, 1000, 0.001);
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
		cover.setAlgorithm(new AlgorithmLog(algo.getAlgorithmType(), algo.getParameters(), algo.compatibleGraphTypes()));
		assertEquals(2, cover.communityCount());
		for(int i=0; i<graph.nodeCount(); i++) {
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
			assertEquals(belongingFac, cover.getBelongingFactor(graph.getNodeArray()[i], 0), 0.002);
		}
		
	}
	
}
