package i5.las2peer.services.ocd.evaluation;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.metrics.ExtendedModularity;
import i5.las2peer.services.ocd.metrics.MetricException;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class EvaluationLabeledMembershipMatrixOutputAdapterTest {

	@Ignore
	@Test
	public void test() throws OcdAlgorithmException, IOException, AdapterException, MetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new SskAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		System.out.println(cover.toString());
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.testSawmillEvaluationLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}

}
