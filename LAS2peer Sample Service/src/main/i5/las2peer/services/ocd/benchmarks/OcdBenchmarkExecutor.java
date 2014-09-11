package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.algorithms.AlgorithmLog;
import i5.las2peer.services.ocd.algorithms.AlgorithmType;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.HashSet;

public class OcdBenchmarkExecutor {
	
	public Cover calculateGroundTruthBenchmark(GroundTruthBenchmarkModel model) throws BenchmarkException {
		Cover cover = model.createGroundTruthCover();
		GraphProcessor processor = new GraphProcessor();
		processor.determineGraphTypes(cover.getGraph());
		cover.setAlgorithm(new AlgorithmLog(AlgorithmType.GROUND_TRUTH, new HashMap<String, String>(), new HashSet<GraphType>()));
		cover.getAlgorithm().setStatus(ExecutionStatus.COMPLETED);
		cover.getGraph().getBenchmark().setStatus(ExecutionStatus.COMPLETED);
		return cover;
	}

}
