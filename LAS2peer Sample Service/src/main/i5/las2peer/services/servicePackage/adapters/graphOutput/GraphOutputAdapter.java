package i5.las2peer.services.servicePackage.adapters.graphOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.OutputAdapter;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

public interface GraphOutputAdapter extends OutputAdapter {
	
	/**
	 * Writes a graph and closes the writer.
	 * @param graph The graph to write.
	 * @throws AdapterException
	 */
	public void writeGraph(CustomGraph graph) throws AdapterException;
	
}
