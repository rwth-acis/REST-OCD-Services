package i5.las2peer.services.ocd.evaluation.realWorldTests;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.evaluation.EvaluationConstants;
import i5.las2peer.services.ocd.evaluation.EvaluationGraphFactory;
import i5.las2peer.services.ocd.evaluation.EvaluationLabeledMembershipMatrixOutputAdapter;
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

/*
 * Evaluation Tasks for the Speaker Listener Label Propagation Algorithm
 */
@Ignore
public class RawLpa10StatisticalMeasuresTest {

	private OcdAlgorithm algo;
	private String algoFileNameExtension;
	
	@Before
	public void before() {
		algo = new RandomWalkLabelPropagationAlgorithm(0.1, 1000, 0.001);
		algoFileNameExtension = algo.getAlgorithmType().name() + "10" + ".txt";
	}
	
	@Test
	public void testOnAercs() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getAercsGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}

	@Test
	public void testOnCora() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getCoraGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
	@Test
	public void testOnEmail() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getEmailGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
	@Test
	public void testOnInternet() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getInternetGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
	@Test
	public void testOnJazz() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getJazzGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
	@Test
	public void testOnPgp() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getPgpGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
	@Test
	public void testOnFacebook() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getFacebookGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 6);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new EvaluationLabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.facebookCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
}
