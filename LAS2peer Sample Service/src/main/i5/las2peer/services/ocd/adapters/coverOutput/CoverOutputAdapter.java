package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;


public interface CoverOutputAdapter extends OutputAdapter {
	
	/**
	 * Writes the cover and closes the writer.
	 * @throws AdapterException
	 */
	public void writeCover(Cover cover) throws AdapterException;
	
}
