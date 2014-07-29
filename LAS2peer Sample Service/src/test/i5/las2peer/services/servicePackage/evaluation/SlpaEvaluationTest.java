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
	public void testOnAcmSigmod() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getAcmSigmodGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaAcmSigmodLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	/*
	 * Evaluates on CIKM
	 */
	@Ignore
	@Test
	public void testOnCikm() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getCikmGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaCikmLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	/*
	 * Evaluates on ICDE
	 */
	@Ignore
	@Test
	public void testOnIcde() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getIcdeGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaIcdeLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	/*
	 * Evaluates on ICDM
	 */
	@Ignore
	@Test
	public void testOnIcdm() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getIcdmGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaIcdmLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	/*
	 * Evaluates on KDD
	 */
	@Ignore
	@Test
	public void testOnKdd() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getKddGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaKddLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	/*
	 * Evaluates on PODS
	 */
	@Ignore
	@Test
	public void testOnPods() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getPodsGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaPodsLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	/*
	 * Evaluates on SIAM DM
	 */
	@Ignore
	@Test
	public void testOnSiamDm() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaSiamDmLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
	
	/*
	 * Evaluates on VLDB
	 */
	@Ignore
	@Test
	public void testOnVldb() throws IOException, OcdAlgorithmException, AdapterException, MetricException
	{
		CustomGraph graph = EvaluationGraphFactory.getVldbGraph();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 5);
		cover.filterMembershipsbyThreshold(0.15);
		StatisticalMeasure metric = new ExtendedModularity();
		metric.measure(cover);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter(new FileWriter(EvaluationConstants.slpaVldbLabeledMembershipMatrixOutputPath));
		adapter.writeCover(cover);
	}
	
}
