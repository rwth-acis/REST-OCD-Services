package i5.las2peer.services.ocd.metrics;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.CostFunctionOptimizationClusteringAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class NewmanModularityCombinedTest {
	@Disabled
	@Test
	public void testOnJmol() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdAlgorithmException, InterruptedException, OcdMetricException{
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		OcdAlgorithm algo = new CostFunctionOptimizationClusteringAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.MAXIMUM_K_NAME, Integer.toString(5));
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.OVERLAPPING_THRESHOLD_NAME, Double.toString(0.3));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		
		NewmanModularityCombined metric = new NewmanModularityCombined();
		double value = metric.measure(cover);
		System.out.println(value);
	}
}
