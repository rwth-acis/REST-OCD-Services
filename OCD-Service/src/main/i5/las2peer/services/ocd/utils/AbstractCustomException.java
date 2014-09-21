package i5.las2peer.services.ocd.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Abstract super class for all custom exceptions.
 * @author Sebastian
 *
 */
public abstract class AbstractCustomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389006745204706662L;

	/**
	 * Creates an exception with a standard error message.
	 */
	public AbstractCustomException() {
		super("Unspecified Custom Exception");
	}
	
	/**
	 * Creates an exception with a specified message.
	 * @param s The message.
	 */
	public AbstractCustomException(String s) {
		super(s);
	}
	
	/**
	 * Creates an exception whose error message includes detailed information
	 * from an additional exception.
	 * @param e The additional exception.
	 */
	public AbstractCustomException(Exception e) {
		super("Internal Exception:\n" + getInternalExceptionString(e));
	}
	
	/**
	 * Creates an exception whose message includes detailed information from an additional exception
	 * and an additional string.
	 * @param e The additional exception.
	 * @param s The additional string.
	 */
	public AbstractCustomException(Exception e, String s) {
		super(s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	/**
	 * Transforms the stack trace of a certain exception into a string.
	 * @param e The exception.
	 * @return The stack trace string.
	 */
	protected static String getInternalExceptionString(Exception e) {
		StringWriter exceptionWriter = new StringWriter();
		PrintWriter exceptionPrinter = new PrintWriter (exceptionWriter);
		e.printStackTrace(exceptionPrinter);
		return exceptionWriter.toString();
	}
	
}
