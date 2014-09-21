package i5.las2peer.services.ocd.utils;

import java.util.Map;

/**
 * Common interface for any classes instantiated by a factory
 * that may customize the created objects with different parameters.
 */
public interface Parameterizable {

	/**
	 * Sets the concrete parameters of an instance.
	 * @param parameters A mapping from the parameter names to the actual parameter values.
	 * The mapping is not necessarily complete, i.e. some parameters might be missing and should be assigned 
	 * (or should maintain) their default value.
	 */
	public void setParameters(Map<String, String> parameters);
	
	/**
	 * Returns the concrete parameters of an instance, including any default values which were not explicitly set.
	 * @return A mapping from the name of each parameter to the actual parameter value in string format.
	 * An empty map if the algorithm does not take any parameters.
	 */
	public Map<String, String> getParameters();
	
}
