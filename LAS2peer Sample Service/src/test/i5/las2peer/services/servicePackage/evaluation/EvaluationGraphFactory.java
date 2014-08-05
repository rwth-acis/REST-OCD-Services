package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.graph.GraphType;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

public class EvaluationGraphFactory {
	
	public static CustomGraph getAercsGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.aercsUnweightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static CustomGraph getCoraGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.coraUnweightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static CustomGraph getEmailGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new WeightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.emailWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static CustomGraph getInternetGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.internetUnweightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static CustomGraph getJazzGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new WeightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.jazzWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static CustomGraph getPgpGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter inputAdapter = new WeightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.pgpWeightedEdgeListInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
}
