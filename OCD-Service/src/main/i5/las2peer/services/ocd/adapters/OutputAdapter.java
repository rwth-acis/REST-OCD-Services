package i5.las2peer.services.ocd.adapters;

import java.io.Writer;

/**
 * The common interface for all output adapters.
 * @author Sebastian
 *
 */
public interface OutputAdapter extends Adapter {
	
	/**
	 * Returns the writer used by the adapter.
	 * @return The writer.
	 */
	public Writer getWriter();
	
	/**
	 * Sets the writer used by the adapter.
	 * @param writer The writer.
	 */
	public void setWriter(Writer writer);
	
}
