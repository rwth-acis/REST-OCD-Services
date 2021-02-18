package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;

import java.util.HashSet;
import java.util.HashMap;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.AntColonyOptimizationAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueGraphRepresentation;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import y.base.Node;


public class AntColonyOptimizationTest {
	
	/**
	 * tests the function CzechkanowskiDice() which determinates the CD-distance between two nodes 
	 */
	@Test
	public void testCDDistanceTest() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		
		Node[] nmap = graph.getNodeArray();
		Node v1 = nmap[1]; 
		Node v2 = nmap[7]; 
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		double cddistance = ACO.CzechkanowskiDice(graph, v1, v2);
		System.out.println("CD-Distance:");
		System.out.println(cddistance);
	}
	
	/**
	 * tests the function LinkStrength() which determinates the interconectivity in between the cliques
	 */
	@Test
	public void testLinkStrenghTest() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		MaximalCliqueGraphRepresentation MCl = new MaximalCliqueGraphRepresentation();
		HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);

		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Matrix linkStr = ACO.linkStrength(graph, maxClq);
		System.out.println("Link Strength Matrix:");
		System.out.println(linkStr);
	}
	
	@Test
	public void testInitialization() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Cover cover = ACO.detectOverlappingCommunities(graph);
	}
	
	@Test
	public void testNegativeRatioAssociation() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		Cover cover = new Cover(graph);
		Matrix memberships = new Basic2DMatrix(9,5); 
		memberships.set(0, 0, 1);
		memberships.set(1, 0, 1);
		memberships.set(8, 0, 1);
		memberships.set(1, 1, 1);
		memberships.set(2, 1, 1);
		memberships.set(8, 1, 1);
		memberships.set(2, 2, 1);
		memberships.set(3, 2, 1);
		memberships.set(7, 2, 1);
		memberships.set(3, 3, 1);
		memberships.set(5, 3, 1);
		memberships.set(6, 3, 1);
		memberships.set(7, 3, 1);
		memberships.set(3, 4, 1);
		memberships.set(4, 4, 1);
		memberships.set(5, 4, 1);
		
		
		cover.setMemberships(memberships);
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		double NRC = ACO.negativeRatioAssociation(graph, cover);
		System.out.println("Negative Ratio Association:");
		System.out.println(NRC);
		
	}
	
	@Test
	public void testCutRatio() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		Cover cover = new Cover(graph);
		Matrix memberships = new Basic2DMatrix(9,5); 
		memberships.set(0, 0, 1);
		memberships.set(1, 0, 1);
		memberships.set(8, 0, 1);
		memberships.set(1, 1, 1);
		memberships.set(2, 1, 1);
		memberships.set(8, 1, 1);
		memberships.set(2, 2, 1);
		memberships.set(3, 2, 1);
		memberships.set(7, 2, 1);
		memberships.set(3, 3, 1);
		memberships.set(5, 3, 1);
		memberships.set(6, 3, 1);
		memberships.set(7, 3, 1);
		memberships.set(3, 4, 1);
		memberships.set(4, 4, 1);
		memberships.set(5, 4, 1);
		
		
		cover.setMemberships(memberships);
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		double CR = ACO.cutRatio(graph, cover);
		System.out.println("Cut Ratio");
		System.out.println(CR);
		
	}
}