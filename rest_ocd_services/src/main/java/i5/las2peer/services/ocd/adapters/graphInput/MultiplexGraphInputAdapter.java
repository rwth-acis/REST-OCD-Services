package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;

import java.text.ParseException;
import java.util.Map;


/**
 * The common interface of all graph input adapters.
 * @author Maren
 *
 */
public interface MultiplexGraphInputAdapter extends CommonGraphInputAdapter<MultiplexGraph> {
	
	/**
	 * Reads the graph and closes the reader.
	 * Sets a unique name for each node of the graph.
	 * @return The read graph.
	 * @throws AdapterException If the input provided by the reader has invalid format.
	 */
	public MultiplexGraph readGraph() throws AdapterException;

}
