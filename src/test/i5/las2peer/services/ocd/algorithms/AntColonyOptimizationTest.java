package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;

import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.Ant;
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
	public void testAllSteps() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		CustomGraph MCR = ACO.representationScheme(graph);
		List<Ant> ants = ACO.initialization(MCR);
		ACO.constructSolution(MCR, ants);
		ACO.updatePheromoneMatrix(MCR, ants);	
		ACO.updateCurrentSolution(ants);
	}
	
	@Test
	public void testACO() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getDolphinsGraph() ;
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Cover c = ACO.detectOverlappingCommunities(graph);
		System.out.print(c);
	}
	
	@Test
	public void testDecoding() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		CustomGraph MCR = ACO.representationScheme(graph);
		Vector cover = new BasicVector(5);
		cover.set(0, 0);
		cover.set(1, 0);
		cover.set(2, 1);
		cover.set(3, 1);
		cover.set(4, 1);
		ACO.decodeMaximalCliques(graph, cover);
	}
	
	@Test
	public void testNegativeRatioAssociation() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		
		Vector cover = new BasicVector(5);
		cover.set(0, 0);
		cover.set(1, 0);
		cover.set(2, 1);
		cover.set(3, 1);
		cover.set(4, 1);
		
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		double NRC = ACO.negativeRatioAssociation(graph, cover);
		System.out.println("Negative Ratio Association:");
		System.out.println(NRC);
		
	}
	
	@Test
	public void testCutRatio() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		Vector cover = new BasicVector(5);
		cover.set(0, 0);
		cover.set(1, 0);
		cover.set(2, 1);
		cover.set(3, 1);
		cover.set(4, 1);
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		double CR = ACO.cutRatio(graph, cover);
		System.out.println("Cut Ratio");
		System.out.println(CR);
		
	}
}