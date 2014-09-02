package i5.las2peer.services.ocd.evaluation;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.metrics.ExtendedModularity;
import i5.las2peer.services.ocd.metrics.MetricException;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EvaluationRealWorldRunTest {

	private OcdAlgorithm algo;
	private String algoFileNameExtension;
	
	@Before
	public void before() {
		algo = new RandomWalkLabelPropagationAlgorithm(0.05, 1000, 0.001);
		algoFileNameExtension = algo.getAlgorithmType().name() + "05" + ".txt";
	}
	
	@Test
	public void testOnSawmill() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getSawmillGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.sawmillCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
}
