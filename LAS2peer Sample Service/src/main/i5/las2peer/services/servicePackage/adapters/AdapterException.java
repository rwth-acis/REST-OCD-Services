package i5.las2peer.services.servicePackage.adapters;

import i5.las2peer.services.servicePackage.utils.AbstractCustomException;

public class AdapterException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	private static final String errorMessage = "File could not be read or written correctly.";
	
	public AdapterException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	public AdapterException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	public AdapterException() {
		super(errorMessage);
	}
	
	public AdapterException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	
}
