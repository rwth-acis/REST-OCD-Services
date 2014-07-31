package i5.las2peer.services.servicePackage.adapters;

import i5.las2peer.services.servicePackage.utils.AbstractCustomException;

/**
 * An exception that indicates the failed input or output of an adapter.
 * @author Sebastian
 *
 */
public class AdapterException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	/**
	 * A standard message for all adapter exceptions.
	 */
	private static final String errorMessage = "File could not be read or written correctly.";
	
	/**
	 * Creates an exception whose error message includes detailed information
	 * from an additional exception.
	 * @param e The additional exception.
	 */
	public AdapterException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	/**
	 * Creates an exception whose message includes an additional string.
	 * @param s The additional string.
	 */
	public AdapterException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	/**
	 * Creates a standard exception.
	 */
	public AdapterException() {
		super(errorMessage);
	}
	
	/**
	 * Creates an exception whose message includes detailed information from an additional exception
	 * and an additional string.
	 * @param e The additional exception.
	 * @param s The additional string.
	 */
	public AdapterException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
