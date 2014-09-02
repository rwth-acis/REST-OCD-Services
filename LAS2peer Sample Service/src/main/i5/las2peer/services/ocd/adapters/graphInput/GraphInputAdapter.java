package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.graph.CustomGraph;

public interface GraphInputAdapter extends InputAdapter {
	
	/**
	 * Reads the graph and closes the reader.
	 * @return The read graph.
	 * Must set a name for each node (should default to the index if not explicitly specified).
	 * Must set a weight for each edge (should default to 1 if not explicitly specified).
	 * @throws AdapterException
	 */
	public CustomGraph readGraph() throws AdapterException;
	
}
