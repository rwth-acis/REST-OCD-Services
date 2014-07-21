package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;

public class EvaluationGraphFactory {
	
	public static CustomGraph getSiamDmGraph() throws AdapterException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(EvaluationConstants.siamDmUnweightedEdgeListInputPath);
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		return graph;
	}
	
}
