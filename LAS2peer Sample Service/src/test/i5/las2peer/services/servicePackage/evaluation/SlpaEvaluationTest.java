package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularity;
import i5.las2peer.services.servicePackage.metrics.MetricException;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/*
 * Evaluation Tasks for the Speaker Listener Label Propagation Algorithm
 */
@Ignore
public class SlpaEvaluationTest {

	private OcdAlgorithm algo;
	
	@Before
	public void before() {
		algo = new SpeakerListenerLabelPropagationAlgorithm();
	}
	
	/*
	 * Evaluates on ACM SIGMOD
	 */
	@Ignore
	@Test
	public void testOnAercs() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getAercsGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.aercsCoverOutputPath + algo.getAlgorithmType().name()));
		adapter.writeCover(cover);
	}
	
}
