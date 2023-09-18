package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;


import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class FuzzyCMeansSpectralClusteringTest implements UndirectedGraphTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new FuzzyCMeansSpectralClusteringAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}

	/*
	 * Run the algorithm on sawmill grpah
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {
		
		// Instantiate the algorithm
		FuzzyCMeansSpectralClusteringAlgorithm fuzzy = new FuzzyCMeansSpectralClusteringAlgorithm(); // instance of fuzzy algorithm
		
		// Set parameters
		Map<String, String> inputParams = new HashMap<String, String>();
		inputParams.put("K", "8");
		inputParams.put("optimizeClusterQuantity", "true");
		inputParams.put("fuzziness", "1.7");
		inputParams.put("customThreshold", "0.2");
		fuzzy.setParameters(inputParams);
		
		CustomGraph sawmill = OcdTestGraphFactory.getSawmillGraph();
		
		try {
			Cover c = fuzzy.detectOverlappingCommunities(sawmill);
			//System.out.println(c.toString());
		} catch (OcdAlgorithmException | OcdMetricException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Test Modularity function on a very simple graph
	 */
	@Disabled //TODO: remove 555
	@Test
	public void ModularityFunctionTest() {
		
		FuzzyCMeansSpectralClusteringAlgorithm fuzzy = new FuzzyCMeansSpectralClusteringAlgorithm(); // instance of fuzzy algorithm

		
		Matrix membership_matrix = new Basic2DMatrix(2,2);
		membership_matrix.set(0, 0, 1);
		membership_matrix.set(0, 1, 0);	
		membership_matrix.set(1, 0, 1);
		membership_matrix.set(1, 1, 0);
	
		
		Matrix Weights = new Basic2DMatrix(2,2);
		
		for(int i =0; i<2; i++) {
			
			for(int j =0; j<2;j++) {
				
				Weights.set(i,j,2.0);
				
			}
		}
		
		double real_modularity = fuzzy.modularityFunction(membership_matrix, Weights,0.0);
		//assertEquals(0.25, real_modularity);
		assertEquals(0.25, real_modularity, 0.01);
	}
}
