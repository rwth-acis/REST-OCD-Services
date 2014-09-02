package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

public class MetricException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	private static final String errorMessage = "Metric could not be calculated.";
	
	public MetricException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	public MetricException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	public MetricException() {
		super(errorMessage);
	}
	
	public MetricException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
