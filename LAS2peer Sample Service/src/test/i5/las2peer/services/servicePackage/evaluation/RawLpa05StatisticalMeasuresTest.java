package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.XmlCoverOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.servicePackage.algorithms.RandomWalkLabelPropagationAlgorithm;
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
public class RawLpa05StatisticalMeasuresTest {

	private OcdAlgorithm algo;
	private final String algoFileNameExtension = algo.getAlgorithmType().name() + "05";
	
	@Before
	public void before() {
		algo = new RandomWalkLabelPropagationAlgorithm(0.05, 1000, 0.001);
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
		CoverOutputAdapter adapter = new XmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(EvaluationConstants.aercsCoverOutputPath + algoFileNameExtension));
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
		CoverOutputAdapter adapter = new XmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(EvaluationConstants.coraCoverOutputPath + algoFileNameExtension));
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
		CoverOutputAdapter adapter = new XmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(EvaluationConstants.emailCoverOutputPath + algoFileNameExtension));
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
		CoverOutputAdapter adapter = new XmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(EvaluationConstants.internetCoverOutputPath + algoFileNameExtension));
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
		CoverOutputAdapter adapter = new XmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(EvaluationConstants.jazzCoverOutputPath + algoFileNameExtension));
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
		CoverOutputAdapter adapter = new XmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(EvaluationConstants.pgpCoverOutputPath + algoFileNameExtension));
		adapter.writeCover(cover);
	}
	
	
}
