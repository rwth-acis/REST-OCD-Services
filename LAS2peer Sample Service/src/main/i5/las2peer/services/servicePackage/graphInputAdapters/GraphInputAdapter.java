package i5.las2peer.services.servicePackage.graphInputAdapters;

import i5.las2peer.services.servicePackage.graph.CustomGraph;

public interface GraphInputAdapter {

	/**
	 * Returns the path of the input file
	 */
	public String getFilename();
	
	/**
	 * Sets the path for the input file.
	 */
	public void setFilename(String filename);
	
	/**
	 * Returns the graph derived from the input file.
	 */
	public abstract CustomGraph readGraph();
	
}
