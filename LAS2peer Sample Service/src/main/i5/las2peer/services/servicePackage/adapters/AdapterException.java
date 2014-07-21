package i5.las2peer.services.servicePackage.adapters;

public class AdapterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	public AdapterException() {
		super("File could not be read or written correctly.");
	}

}
