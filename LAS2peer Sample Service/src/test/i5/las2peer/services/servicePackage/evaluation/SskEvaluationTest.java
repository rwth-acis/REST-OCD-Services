package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.GraphAnalyzer;
import i5.las2peer.services.servicePackage.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverInput.LabeledMembershipMatrixInputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SSKAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularityMetric;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	 */
	@Ignore
	@Test
	public void testSskAlgoOnSiamDm() throws IOException, OcdAlgorithmException
	{
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		GraphAnalyzer analyzer = new GraphAnalyzer();
		List<CustomGraph> graphs = new ArrayList<CustomGraph>();
		graphs.add(graph);
		List<OcdAlgorithm> algorithms = new ArrayList<OcdAlgorithm>();
		algorithms.add(new SSKAlgorithm());
		List<StatisticalMeasure> statisticalMeasures = new ArrayList<StatisticalMeasure>();
		statisticalMeasures.add(new ExtendedModularityMetric());
		List<Cover> covers = analyzer.analyze(graphs, algorithms, statisticalMeasures, 8);
		Cover cover = covers.get(0);
		System.out.println(cover.toString());
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(EvaluationConstants.sskSiamDmLabeledMembershipMatrixOutputPath);
		adapter.writeCover(cover);
	}
	
	@Ignore
	@Test
	public void testCoverModularity() {
		CoverInputAdapter adapter = new LabeledMembershipMatrixInputAdapter();
		Cover cover;
		try {
			CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
			Algorithm algorithm = Algorithm.UNDEFINED;
			cover = adapter.readCover(EvaluationConstants.sskSiamDmLabeledMembershipMatrixOutputPath, graph, algorithm);
			cover.filterMembershipsbyThreshold(0.2);
			StatisticalMeasure metric = new ExtendedModularityMetric();
			metric.measure(cover);
			System.out.println(cover.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
