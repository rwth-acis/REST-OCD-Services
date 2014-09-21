package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * The common interface of all graph output adapters.
 * @author Sebastian
 *
 */
public interface GraphOutputAdapter extends OutputAdapter {
	
	/**
	 * Writes a graph and closes the writer.
	 * @param graph The graph to write.
	 * @throws AdapterException
	 */
	public void writeGraph(CustomGraph graph) throws AdapterException;
	
}
