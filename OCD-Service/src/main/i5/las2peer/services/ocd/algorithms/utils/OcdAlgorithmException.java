package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

/**
 * An exception that indicates the failure of the execution of an overlapping community detection algorithm.
 * @author Sebastian
 *
 */
public class OcdAlgorithmException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7862401734412480848L;

	/**
	 * A standard message for all algorithm exceptions.
	 */
	private static final String errorMessage = "Algorithm could not be executed.";
	
	/**
	 * Creates an exception whose error message includes detailed information
	 * from an additional exception.
	 * @param e The additional exception.
	 */
	public OcdAlgorithmException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	/**
	 * Creates an exception whose message includes an additional string.
	 * @param s The additional string.
	 */
	public OcdAlgorithmException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	/**
	 * Creates a standard exception.
	 */
	public OcdAlgorithmException() {
		super(errorMessage);
	}
	
	/**
	 * Creates an exception whose message includes detailed information from an additional exception
	 * and an additional string.
	 * @param e The additional exception.
	 * @param s The additional string.
	 */
	public OcdAlgorithmException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
