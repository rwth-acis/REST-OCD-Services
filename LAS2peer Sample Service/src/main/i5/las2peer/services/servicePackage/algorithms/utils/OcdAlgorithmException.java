package i5.las2peer.services.servicePackage.algorithms.utils;

import i5.las2peer.services.servicePackage.utils.AbstractCustomException;


public class OcdAlgorithmException extends AbstractCustomException {

	private static final long serialVersionUID = -7862401734412480848L;

	private static final String errorMessage = "Algorithm could not be executed.";
	
	public OcdAlgorithmException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	public OcdAlgorithmException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	public OcdAlgorithmException() {
		super(errorMessage);
	}
	
	public OcdAlgorithmException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
