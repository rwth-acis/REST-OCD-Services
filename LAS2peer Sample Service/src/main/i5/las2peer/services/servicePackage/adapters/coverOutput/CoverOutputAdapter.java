package i5.las2peer.services.servicePackage.adapters.coverOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.OutputAdapter;
import i5.las2peer.services.servicePackage.graph.Cover;


public interface CoverOutputAdapter extends OutputAdapter {
	
	/**
	 * Writes the cover and closes the writer.
	 * @throws AdapterException
	 */
	public void writeCover(Cover cover) throws AdapterException;
	
}
