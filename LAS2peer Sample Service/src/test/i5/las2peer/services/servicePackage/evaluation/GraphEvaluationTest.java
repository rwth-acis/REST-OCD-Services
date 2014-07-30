package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.servicePackage.algorithms.SskAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.utils.Pair;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import y.base.Node;

public class GraphEvaluationTest {

	@Ignore
	@Test
	public void testSiamComponents() throws AdapterException, FileNotFoundException {
		System.out.println("Siam Components:");
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
		}
	}

	@Ignore
	@Test
	public void testSiamAnalyzer() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		System.out.println("Siam Component Analyzer:");
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		OcdAlgorithmExecutor algoExecutor = new OcdAlgorithmExecutor();
		OcdAlgorithm algorithm = new SskAlgorithm();
		Cover cover = algoExecutor.execute(graph, algorithm, 1100);
		System.out.println(cover.toString());
	}
	
}
