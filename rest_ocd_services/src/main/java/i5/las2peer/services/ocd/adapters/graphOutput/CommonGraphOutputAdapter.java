package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;

import java.text.ParseException;
import java.util.Map;

/**
 * The common interface of all graph output adapters.
 * @author Sebastian
 *
 */
public interface CommonGraphOutputAdapter<T> extends OutputAdapter {
	
	/**
	 * Writes a graph and closes the writer.
	 * @throws AdapterException if the adapter failed
	 */
	public void writeGraph(T graph) throws AdapterException;
}
