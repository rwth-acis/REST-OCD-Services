package i5.las2peer.services.ocd.adapters.centralityOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;

/**
 * The common interface of all centrality output adapters.
 * @author Tobias
 *
 */
public interface CentralityOutputAdapter extends OutputAdapter {

	/**
	 * Writes a CentralityMap and closes the writer.
	 * @param map The CentralityMap.
	 * @throws AdapterException
	 */
	public void writeCentralityMap(CentralityMap map) throws AdapterException;
	
}
