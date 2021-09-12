package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import y.view.Graph2D;

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
	public void writeGraph(Graph2D graph) throws AdapterException;
	
}
