package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverInput.LabeledMembershipMatrixInputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.servicePackage.algorithms.SskAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularity;
import i5.las2peer.services.servicePackage.metrics.OcdMetricExecutor;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SskEvaluationTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 * @throws IOException 
	 * @throws OcdAlgorithmException 
	 * @throws AdapterException 
	 */
	@Ignore
	@Test
	public void testSskAlgoOnSiamDm() throws IOException, OcdAlgorithmException, AdapterException
	{
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		OcdAlgorithmExecutor algoExecutor = new OcdAlgorithmExecutor();
		OcdAlgorithm algorithm = new SskAlgorithm();
		Cover cover = algoExecutor.execute(graph, algorithm, 8);
		OcdMetricExecutor metricExecutor = new OcdMetricExecutor();
		metricExecutor.executeStatisticalMeasure(cover, new ExtendedModularity());
		System.out.println(cover.toString());
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(EvaluationConstants.sskSiamDmLabeledMembershipMatrixOutputPath);
		adapter.writeCover(cover);
	}
	
	@Ignore
	@Test
	public void testCoverModularity() {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter();
		adapter.setFilename(EvaluationConstants.sskSiamDmLabeledMembershipMatrixOutputPath);
		Cover cover;
		try {
			CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
			Algorithm algorithm = Algorithm.UNDEFINED;
			cover = adapter.readCover(graph, algorithm);
			cover.filterMembershipsbyThreshold(0.2);
			StatisticalMeasure metric = new ExtendedModularity();
			metric.measure(cover);
			System.out.println(cover.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
