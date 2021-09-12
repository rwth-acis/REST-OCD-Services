package i5.las2peer.services.ocd.centrality.utils;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

/**
 * An exception that indicates the failure of the execution of a CentralityAlgorithm.
 *
 */
public class CentralityAlgorithmException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7862401734412480848L;

	/**
	 * A standard message for all algorithm exceptions.
	 */
	private static final String errorMessage = "Centrality algorithm could not be executed.";
	
	/**
	 * Creates an exception whose error message includes detailed information
	 * from an additional exception.
	 * @param e The additional exception.
	 */
	public CentralityAlgorithmException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	/**
	 * Creates an exception whose message includes an additional string.
	 * @param s The additional string.
	 */
	public CentralityAlgorithmException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	/**
	 * Creates a standard exception.
	 */
	public CentralityAlgorithmException() {
		super(errorMessage);
	}
	
	/**
	 * Creates an exception whose message includes detailed information from an additional exception
	 * and an additional string.
	 * @param e The additional exception.
	 * @param s The additional string.
	 */
	public CentralityAlgorithmException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
