package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.CommonGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;

/**
 * The common interface of all graph output adapters.
 * @author Sebastian
 *
 */
public interface MultiplexGraphOutputAdapter extends CommonGraphOutputAdapter<MultiplexGraph> {
	
	/**
	 * Writes a graph and closes the writer.
	 * @param graph The graph to write.
	 * @throws AdapterException if the adapter failed
	 */
	public void writeGraph(MultiplexGraph graph) throws AdapterException;
	
}
