package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * The common interface of all visual output adapters.
 * @author Sebastian
 *
 */
public interface VisualOutputAdapter extends OutputAdapter {
	
	/**
	 * Writes a graph and closes the writer.
	 * @param graph The graph to write.
	 * @throws AdapterException if the adapter failed
	 */
	public void writeGraph(CustomGraph graph) throws AdapterException;
	
}
