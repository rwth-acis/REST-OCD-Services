package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SskAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularity;
import i5.las2peer.services.servicePackage.metrics.MetricException;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class EvaluationLabeledMembershipMatrixOutputAdapterTest {

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
