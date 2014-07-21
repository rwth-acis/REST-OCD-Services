package i5.las2peer.services.servicePackage.benchmarkModels;

import i5.las2peer.services.servicePackage.graph.Cover;

/**
 * A common interface for all benchmark models which also provide ground truth covers.
 * @author Sebastian
 *
 */
public interface GroundTruthBenchmarkModel {
	
	/**
	 * Returns a ground truth cover with the corresponding graph created according to the benchmark model.
	 * @return The ground truth cover. The graph can be obtained via the cover.
	 * @throws BenchmarkException 
	 */
	public Cover createGroundTruthCover() throws BenchmarkException;
	
}
