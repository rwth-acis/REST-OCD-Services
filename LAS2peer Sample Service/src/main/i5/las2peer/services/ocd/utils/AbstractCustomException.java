package i5.las2peer.services.ocd.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class AbstractCustomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389006745204706662L;

	public AbstractCustomException() {
		super("Unspecified Custom Exception");
	}
	
	public AbstractCustomException(String s) {
		super(s);
	}
	
	public AbstractCustomException(Exception e) {
		super("Internal Exception:\n" + getInternalExceptionString(e));
	}
	
	public AbstractCustomException(Exception e, String s) {
		super(s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	protected static String getInternalExceptionString(Exception e) {
		StringWriter exceptionWriter = new StringWriter();
		PrintWriter exceptionPrinter = new PrintWriter (exceptionWriter);
		e.printStackTrace(exceptionPrinter);
		return exceptionWriter.toString();
	}
	
}
