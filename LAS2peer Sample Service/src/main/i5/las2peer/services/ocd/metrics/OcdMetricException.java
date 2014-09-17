package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

public class OcdMetricException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	private static final String errorMessage = "Metric could not be calculated.";
	
	public OcdMetricException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	public OcdMetricException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	public OcdMetricException() {
		super(errorMessage);
	}
	
	public OcdMetricException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
