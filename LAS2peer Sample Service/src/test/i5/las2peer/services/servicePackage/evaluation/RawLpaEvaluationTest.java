package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.GraphAnalyzer;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class RawLpaEvaluationTest {
	
	/**
	 * Tests the Random Walk Label Propagation Algorithm on Siam
	 * @throws IOException
	 * @throws OcdAlgorithmException
	 */
	@Ignore
	@Test
	public void testRawLpaOnSiamDm() throws IOException, OcdAlgorithmException
	{
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		GraphAnalyzer analyzer = new GraphAnalyzer();
		List<CustomGraph> graphs = new ArrayList<CustomGraph>();
		graphs.add(graph);
		List<OcdAlgorithm> algorithms = new ArrayList<OcdAlgorithm>();
		double[] profitabililtyDeltas = new double[11];
		profitabililtyDeltas[0] = 0.050;
		profitabililtyDeltas[1] = 0.075;
		profitabililtyDeltas[2] = 0.100;
		profitabililtyDeltas[3] = 0.125;
		profitabililtyDeltas[4] = 0.150;
		profitabililtyDeltas[5] = 0.175;
		profitabililtyDeltas[6] = 0.200;
		profitabililtyDeltas[7] = 0.225;
		profitabililtyDeltas[8] = 0.250;
		profitabililtyDeltas[9] = 0.275;
		profitabililtyDeltas[10] = 0.300;
		for(int i=0; i<profitabililtyDeltas.length; i++) {
			algorithms.add(new RandomWalkLabelPropagationAlgorithm(profitabililtyDeltas[i], 1000, 0.001));
		}
		List<StatisticalMeasure> statisticalMeasures = new ArrayList<StatisticalMeasure>();
		// statisticalMeasures.add(new ExtendedModularityMetric());
		List<Cover> covers = analyzer.analyze(graphs, algorithms, statisticalMeasures, 8);
		Cover cover = covers.get(0);
		System.out.println(cover.toString());
	}
	
}
