package i5.las2peer.services.servicePackage.adapters.coverInput;

import i5.las2peer.services.servicePackage.adapters.Adapter;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;


public interface CoverInputAdapter extends Adapter {
	
	/**
	 * Reads the cover from the input file.
	 * @throws AdapterException
	 */
	public Cover readCover(CustomGraph graph, Algorithm algorithm) throws AdapterException;
	
}
