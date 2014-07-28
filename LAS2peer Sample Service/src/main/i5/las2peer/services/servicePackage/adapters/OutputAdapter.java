package i5.las2peer.services.servicePackage.adapters;

import java.io.Writer;

public interface OutputAdapter {
	
	/**
	 * Returns the writer.
	 * @return The writer.
	 */
	public Writer getWriter();
	
	/**
	 * Sets the writer.
	 * @param writer The writer.
	 */
	public void setWriter(Writer writer);
	
}
