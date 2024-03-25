package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;

/**
 * The common interface of all multiplex graph output adapters.
 * @author Maren
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
