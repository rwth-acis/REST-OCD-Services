package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;

import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.SelfLoopsGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.ZeroWeightsGraphTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.Ant;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.ModularityMetric;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.graphstream.graph.Node;

public class AntColonyOptimizationTest implements  UndirectedGraphTestReq, ZeroWeightsGraphTestReq, SelfLoopsGraphTestReq, OCDAParameterTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new AntColonyOptimizationAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}
	
	/**
	 * tests the function CzechkanowskiDice() which determinates the CD-distance between two nodes 
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testCDDistanceTest() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		
		Node[] nmap =  graph.nodes().toArray(Node[]::new);
		Node v1 = nmap[1]; 
		Node v2 = nmap[7]; 
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		double cddistance = ACO.CzechkanowskiDice(graph, v1, v2);
//		System.out.println("CD-Distance:");
//		System.out.println(cddistance);
	}
	
	/**
	 * tests the function LinkStrength() which determinates the interconectivity in between the cliques
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testLinkStrenghTest() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getLinkgraph();
		MaximalCliqueSearch MCl = new MaximalCliqueSearch();
		HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Matrix linkStr = ACO.linkStrength(graph, maxClq);
//		System.out.println("Link Strength Matrix:");
//		System.out.println(linkStr);
//		System.out.println(maxClq);
	}

	@Disabled //TODO: remove 555
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

	@Disabled //TODO: remove 555
	@Test
	public void testACO() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getDolphinsGraph();
		AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
		Cover c = ACO.detectOverlappingCommunities(graph);
		//ClizzAlgorithm CA = new ClizzAlgorithm();
		//Cover c1 = CA.detectOverlappingCommunities(graph);
		//System.out.print(c);
		ModularityMetric mod = new ModularityMetric();
		//System.out.println(mod.measure(c1));
		//System.out.println(mod.measure(c));
	}


}