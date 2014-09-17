package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.algorithms.CoverCreationLog;
import i5.las2peer.services.ocd.algorithms.CoverCreationType;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.HashSet;

public class OcdBenchmarkExecutor {
	
	public Cover calculateGroundTruthBenchmark(GroundTruthBenchmark model) throws OcdBenchmarkException, InterruptedException {
		Cover cover = model.createGroundTruthCover();
		GraphProcessor processor = new GraphProcessor();
		processor.determineGraphTypes(cover.getGraph());
		cover.setCreationMethod(new CoverCreationLog(CoverCreationType.GROUND_TRUTH, new HashMap<String, String>(), new HashSet<GraphType>()));
		cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		cover.getGraph().getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		return cover;
	}

}
