package i5.las2peer.services.ocd.adapters;

import java.io.Reader;

/**
 * Abstract super class for input adapters.
 * @author Sebastian
 *
 */
public abstract class AbstractInputAdapter implements InputAdapter {

	/**
	 * The reader which is used for input. 
	 */
	protected Reader reader;
	
	/**
	 * Returns the reader.
	 */
	@Override
	public Reader getReader() {
		return reader;
	}

	/**
	 * Sets the reader.
	 */
	@Override
	public void setReader(Reader reader) {
		this.reader = reader;
	}
	
}
