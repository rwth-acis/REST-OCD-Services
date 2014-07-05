package i5.las2peer.services.servicePackage.adapters.coverOutput;

import i5.las2peer.services.servicePackage.adapters.Adapter;
import i5.las2peer.services.servicePackage.graph.Cover;

import java.io.IOException;


public interface CoverOutputAdapter extends Adapter {
	
	/**
	 * Writes the memberships given by the cover into the input file.
	 */
	public void writeCover(Cover cover) throws IOException;
	
}
