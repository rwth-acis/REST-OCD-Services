package i5.las2peer.services.servicePackage.adapters;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AdapterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7785040553039784645L;
	
	public AdapterException(Exception e) {
		super("File could not be read or written correctly." + "\nInternal Exception:\n" + AdapterException.getInternalExceptionString(e));
	}
	
	private static String getInternalExceptionString(Exception e) {
		StringWriter exceptionWriter = new StringWriter();
		PrintWriter exceptionPrinter = new PrintWriter (exceptionWriter);
		e.printStackTrace(exceptionPrinter);
		return exceptionWriter.toString();
	}
	
}
