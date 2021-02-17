package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;

import java.util.HashSet;
import java.util.HashMap;

import org.junit.Test;
import org.la4j.matrix.Matrix;

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
		System.out.println(linkStr);
	}
	
	@Test
	public void testInitialization() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Cover cover = ACO.detectOverlappingCommunities(graph);
	}
	
	
}