package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;


public class CostFunctionOptimizationClusteringAlgorithmTest {
	
	@Ignore
	@Test
	public void testOnJmolTestGraph() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
    	CostFunctionOptimizationClusteringAlgorithm algo = new CostFunctionOptimizationClusteringAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.MAXIMUM_K_NAME, Integer.toString(5));
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.OVERLAPPING_THRESHOLD_NAME, Double.toString(0.3));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Ignore
	@Test
	public void testSVD()throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
    	CostFunctionOptimizationClusteringAlgorithm algo = new CostFunctionOptimizationClusteringAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.MAXIMUM_K_NAME, Integer.toString(5));
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.OVERLAPPING_THRESHOLD_NAME, Double.toString(0.3));
		parameters.put(CostFunctionOptimizationClusteringAlgorithm.SVD_NAME, Boolean.toString(true));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
}
