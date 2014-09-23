package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Manages the execution of an OcdBenchmark.
 * @author Sebastian
 *
 */
public class OcdBenchmarkExecutor {
	
	/**
	 * Executes a ground truth benchmark.
	 * @param benchmark The benchmark to execute.
	 * @return The ground truth cover calculated by the benchmark, also containing the corresponding benchmark graph.
	 * @throws OcdBenchmarkException In case of a benchmark failure.
	 * @throws InterruptedException In case of a benchmark interrupt.
	 */
	public Cover calculateGroundTruthBenchmark(GroundTruthBenchmark benchmark) throws OcdBenchmarkException, InterruptedException {
		Cover cover = benchmark.createGroundTruthCover();
		GraphProcessor processor = new GraphProcessor();
		processor.determineGraphTypes(cover.getGraph());
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.GROUND_TRUTH, new HashMap<String, String>(), new HashSet<GraphType>()));
		cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		cover.getGraph().getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		return cover;
	}

}
