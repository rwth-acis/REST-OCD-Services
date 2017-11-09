package i5.las2peer.services.ocd.adapters.centralityInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * The common interface of all centrality input adapters.
 * @author Tobias
 *
 */
public interface CentralityInputAdapter extends InputAdapter {
	
	/**
	 * Reads centrality input and closes the reader.
	 * @param graph The graph which the centrality values are based on.
	 * @return The read CentralityMap.
	 * @throws AdapterException
	 */
	public CentralityMap readCentrality(CustomGraph graph) throws AdapterException;
	
}
