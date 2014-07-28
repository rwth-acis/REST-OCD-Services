package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class EvaluationGraphFactory {
	
	public static CustomGraph getAcmSigmodGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.acmSigmodUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getCikmGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.cikmUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getIcdeGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.icdeUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getIcdmGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.icdmUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getKddGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.kddUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getPodsGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.podsUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getSiamDmGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.siamDmUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
	public static CustomGraph getVldbGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(EvaluationConstants.vldbUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
}
