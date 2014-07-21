package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.servicePackage.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class RawLpaEvaluationTest {
	
	/**
	 * Tests the Random Walk Label Propagation Algorithm on Siam
	 * @throws IOException
	 * @throws OcdAlgorithmException
	 * @throws AdapterException 
	 */
	@Ignore
	@Test
	public void testRawLpaOnSiamDm() throws IOException, OcdAlgorithmException, AdapterException
	{
		CustomGraph graph = EvaluationGraphFactory.getSiamDmGraph();
		OcdAlgorithmExecutor algoExecutor = new OcdAlgorithmExecutor();
		double[] profitabilityDeltas = new double[11];
		profitabilityDeltas[0] = 0.050;
		profitabilityDeltas[1] = 0.075;
		profitabilityDeltas[2] = 0.100;
		profitabilityDeltas[3] = 0.125;
		profitabilityDeltas[4] = 0.150;
		profitabilityDeltas[5] = 0.175;
		profitabilityDeltas[6] = 0.200;
		profitabilityDeltas[7] = 0.225;
		profitabilityDeltas[8] = 0.250;
		profitabilityDeltas[9] = 0.275;
		profitabilityDeltas[10] = 0.300;
		for(int i=0; i<profitabilityDeltas.length; i++) {
			OcdAlgorithm algorithm = new RandomWalkLabelPropagationAlgorithm(profitabilityDeltas[i], 1000, 0.001);
			Cover cover = algoExecutor.execute(graph, algorithm, 8);
			/* TODO let metric run
			OcdMetricExecutor metricExecutor = new OcdMetricExecutor();
			metricExecutor.executeStatisticalMeasure(cover, new ExtendedModularity());
			*/
			System.out.println("Profitability: " + profitabilityDeltas[i]);
			System.out.println(cover.toString());
			System.out.println();
		}

	}
	
}
