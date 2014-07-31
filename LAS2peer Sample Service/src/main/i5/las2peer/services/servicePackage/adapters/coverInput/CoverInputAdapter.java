package i5.las2peer.services.servicePackage.adapters.coverInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.InputAdapter;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

/**
 * The common interface of all cover input adapters.
 * @author Sebastian
 *
 */
public interface CoverInputAdapter extends InputAdapter {
	
	/**
	 * Reads a cover and closes the reader.
	 * @return The read cover.
	 * @throws AdapterException
	 */
	/**
	 * Reads a cover and closes the reader.
	 * @param graph The graph to which the cover belongs.
	 * @return The cover.
	 * @throws AdapterException
	 */
	public Cover readCover(CustomGraph graph) throws AdapterException;
	
}
