package i5.las2peer.services.servicePackage.adapters;

import java.io.Reader;

/**
 * The common interface of all input adapters.
 * @author Sebastian
 *
 */
public interface InputAdapter {

	/**
	 * Returns the reader used by the adapter.
	 * @return The reader.
	 */
	public Reader getReader();

	/**
	 * Sets the reader used by the adapter.
	 * @param The reader.
	 */
	public void setReader(Reader reader);
	
}
