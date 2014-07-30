package i5.las2peer.services.servicePackage.adapters.coverInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.InputAdapter;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;


public interface CoverInputAdapter extends InputAdapter {
	
	/**
	 * Reads the cover and closes the reader.
	 * @return The read cover.
	 * @throws AdapterException
	 */
	public Cover readCover(CustomGraph graph) throws AdapterException;
	
}
