package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

public class OcdBenchmarkException extends AbstractCustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6700245209049455692L;
	
	private static final String errorMessage = "Failed in creating a benchmark graph.";
	
	public OcdBenchmarkException(Exception e) {
		super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
	public OcdBenchmarkException(String s) {
		super(errorMessage + "\n" + s);
	}
	
	public OcdBenchmarkException() {
		super(errorMessage);
	}
	
	public OcdBenchmarkException(Exception e, String s) {
		super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
	}
	
}
