package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;

import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

import org.junit.Ignore;
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
import i5.las2peer.services.ocd.metrics.ExtendedModularityMetric;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import y.base.Edge;
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
		CustomGraph graph = OcdTestGraphFactory.getLinkgraph();
		MaximalCliqueGraphRepresentation MCl = new MaximalCliqueGraphRepresentation();
		HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Matrix linkStr = ACO.linkStrength(graph, maxClq);
		System.out.println("Link Strength Matrix:");
		System.out.println(linkStr);
		System.out.println(maxClq);
	}
	
	@Test
	public void testAllSteps() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		CustomGraph MCR = ACO.representationScheme(graph);
		List<Ant> ants = ACO.initialization(MCR, 5);
		for(Ant a: ants) {
			ACO.constructSolution(MCR, a, 5);
		}
		ACO.update(MCR, ants, 5);	
	}
	
	@Test
	public void testACO() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getDolphinsGraph();
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Cover c = ACO.detectOverlappingCommunities(graph);
		//ClizzAlgorithm CA = new ClizzAlgorithm(); 
		//Cover c1 = CA.detectOverlappingCommunities(graph);
		System.out.print(c);
		ExtendedModularityMetric mod = new ExtendedModularityMetric();
		//System.out.println(mod.measure(c1)); 
		System.out.println(mod.measure(c));
	}
	
	@Ignore
	@Test
	public void testDecoding() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		CustomGraph MCR = ACO.representationScheme(graph);
		List<Ant> ants = ACO.initialization(MCR, 5);
		Vector cover = new BasicVector(5);
		cover.set(0, 0);
		cover.set(1, 0);
		cover.set(2, 1);
		cover.set(3, 1);
		cover.set(4, 1);
		ACO.decodeMaximalCliques(graph, 5);
	}
	
	@Ignore
	@Test
	public void testFitnessComputations() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		
		Vector cover = new BasicVector(5);
		cover.set(0, 0);
		cover.set(1, 0);
		cover.set(2, 1);
		cover.set(3, 1);
		cover.set(4, 1);
		
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Vector NRC = ACO.fitnessCalculations(graph, cover, 5);
		System.out.println("Fitness values: ");
		System.out.println(NRC);
		
	}

}