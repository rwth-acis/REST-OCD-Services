package i5.las2peer.services.ocd.adapters;

import java.io.Writer;

/**
 * Abstract super class for output adapters.
 * @author Sebastian
 *
 */
public abstract class AbstractOutputAdapter implements OutputAdapter {

	/**
	 * The writer which is used for output.
	 */
	protected Writer writer;
	
	/**
	 * Returns the writer.
	 */
	@Override
	public Writer getWriter() {
		return writer;
	}
	
	/**
	 * Sets the writer.
	 */
	@Override
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
}
