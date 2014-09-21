package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

/**
 * An exception that indicates the failure of the execution of an overlapping community detection metric.
 * @author Sebastian
 *
 */
public class OcdMetricException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	/**
	 * A standard message for all benchmark exceptions.
	 */
	private static final String errorMessage = "Metric could not be calculated.";
	
	/**
	 * Creates an exception whose error message includes detailed information
	 * from an additional exception.
	 * @param e The additional exception.
	 */
	public OcdMetricException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	/**
	 * Creates an exception whose message includes an additional string.
	 * @param s The additional string.
	 */
	public OcdMetricException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	/**
	 * Creates a standard exception.
	 */
	public OcdMetricException() {
		super(errorMessage);
	}
	
	/**
	 * Creates an exception whose message includes detailed information from an additional exception
	 * and an additional string.
	 * @param e The additional exception.
	 * @param s The additional string.
	 */
	public OcdMetricException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
