package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

/**
 * An exception that indicates the failure of the execution of an overlapping community detection benchmark.
 * @author Sebastian
 *
 */
public class OcdBenchmarkException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6700245209049455692L;
	
	/**
	 * A standard message for all benchmark exceptions.
	 */
	private static final String errorMessage = "Failed in creating a benchmark graph.";
	
	/**
	 * Creates an exception whose error message includes detailed information
	 * from an additional exception.
	 * @param e The additional exception.
	 */
	public OcdBenchmarkException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	/**
	 * Creates an exception whose message includes an additional string.
	 * @param s The additional string.
	 */
	public OcdBenchmarkException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	/**
	 * Creates a standard exception.
	 */
	public OcdBenchmarkException() {
		super(errorMessage);
	}
	
	/**
	 * Creates an exception whose message includes detailed information from an additional exception
	 * and an additional string.
	 * @param e The additional exception.
	 * @param s The additional string.
	 */
	public OcdBenchmarkException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
}
