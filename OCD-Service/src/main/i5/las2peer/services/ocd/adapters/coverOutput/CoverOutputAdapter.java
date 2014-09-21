package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;

/**
 * The common interface of all cover output adapters.
 * @author Sebastian
 *
 */
public interface CoverOutputAdapter extends OutputAdapter {

	/**
	 * Writes a cover and closes the writer.
	 * @param cover The cover.
	 * @throws AdapterException
	 */
	public void writeCover(Cover cover) throws AdapterException;
	
}
