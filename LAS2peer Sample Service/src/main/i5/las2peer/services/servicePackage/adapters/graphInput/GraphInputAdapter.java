package i5.las2peer.services.servicePackage.adapters.graphInput;

import i5.las2peer.services.servicePackage.adapters.Adapter;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

public interface GraphInputAdapter extends Adapter {
	
	/**
	 * Returns the graph derived from the input file.
	 * @throws AdapterException
	 */
	public CustomGraph readGraph() throws AdapterException;
	
}
