package i5.las2peer.services.servicePackage.adapters;

public interface Adapter {

	/**
	 * Returns the path of the input file
	 */
	public String getFilename();
	
	/**
	 * Sets the path for the input file.
	 */
	public void setFilename(String filename);
	
}
