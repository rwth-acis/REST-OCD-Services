package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.graphs.Cover;

import java.util.Map;

/**
 * A common interface for all benchmark models which also provide ground truth covers.
 * @author Sebastian
 *
 */
public interface GroundTruthBenchmarkModel {
	
	/**
	 * Returns a ground truth cover with the corresponding graph created according to the benchmark model.
	 * @return The ground truth cover holding also the benchmark graph.
	 * @throws BenchmarkException 
	 */
	public Cover createGroundTruthCover() throws BenchmarkException;
	
	/**
	 * Sets the concrete parameters for the benchmark cover.
	 * @param parameters A mapping from parameter names to the actual parameter values.
	 * The mapping is not necessarily complete, i.e. some parameters might be missing
	 * and should be assigned their default value.
	 */
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException;
	
	/**
	 * Returns the concrete parameters of the benchmark instance
	 * (including the default value for each parameter that was not explicitly set).
	 * @return A mapping from the name of each parameter to the actual parameter value in string format.
	 * An empty map if the algorithm does not take any parameters.
	 */
	public Map<String, String> getParameters();
}
