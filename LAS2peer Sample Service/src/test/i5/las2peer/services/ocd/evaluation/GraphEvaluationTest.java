package i5.las2peer.services.ocd.evaluation;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.graph.GraphProcessor;
import i5.las2peer.services.ocd.utils.Pair;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import y.base.Node;

@Ignore
public class GraphEvaluationTest {

	@Test
	public void testAercsGraph() throws AdapterException, FileNotFoundException {
		System.out.println("Aercs Components:");
		CustomGraph graph = EvaluationGraphFactory.getAercsGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
			System.out.println("Component Edges " + i + ": " + components.get(i).getFirst().edgeCount());
		}
	}

	@Test
	public void testCoraGraph() throws AdapterException, FileNotFoundException {
		System.out.println("Cora Components:");
		CustomGraph graph = EvaluationGraphFactory.getCoraGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
			System.out.println("Component Edges " + i + ": " + components.get(i).getFirst().edgeCount());
		}
	}
	
	@Test
	public void testEmailGraph() throws AdapterException, FileNotFoundException {
		System.out.println("Email Components:");
		CustomGraph graph = EvaluationGraphFactory.getEmailGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
			System.out.println("Component Edges " + i + ": " + components.get(i).getFirst().edgeCount());
		}
	}
	
	@Test
	public void testInternetGraph() throws AdapterException, FileNotFoundException {
		System.out.println("Internet Components:");
		CustomGraph graph = EvaluationGraphFactory.getInternetGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
			System.out.println("Component Edges " + i + ": " + components.get(i).getFirst().edgeCount());
		}
	}
	
	@Test
	public void testJazzGraph() throws AdapterException, FileNotFoundException {
		System.out.println("Jazz Components:");
		CustomGraph graph = EvaluationGraphFactory.getJazzGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
			System.out.println("Component Edges " + i + ": " + components.get(i).getFirst().edgeCount());
		}
	}
	
	@Test
	public void testPgpGraph() throws AdapterException, FileNotFoundException {
		System.out.println("Pgp Components:");
		CustomGraph graph = EvaluationGraphFactory.getPgpGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		for(int i=0; i<components.size(); i++) {
			System.out.println("Component Nodes " + i + ": " + components.get(i).getFirst().nodeCount());
			System.out.println("Component Edges " + i + ": " + components.get(i).getFirst().edgeCount());
		}
	}
	
}
