package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationMethod;

/**
 * A common interface for all ground truth benchmark models (which provide a graph including a corresponding ground truth cover).
 * @author Sebastian
 *
 */
public interface GroundTruthBenchmark extends OcdBenchmark, CoverCreationMethod {
	
	/**
	 * Returns a ground truth cover with the corresponding graph created according to the benchmark model.
	 * Implementations of this method must allow to be interrupted.
	 * I.e. they must periodically check the thread for interrupts
	 * and throw an InterruptedException if an interrupt was detected.
	 * @return The ground truth cover holding also the benchmark graph.
	 * @throws OcdBenchmarkException If the execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public Cover createGroundTruthCover() throws OcdBenchmarkException, InterruptedException;

}
