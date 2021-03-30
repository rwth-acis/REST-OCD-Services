package i5.las2peer.services.ocd.algorithms;

import static org.junit.Assert.*;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.*;

import y.base.Node;

public class LocalSpectralClusteringAlgorithmTest {

	/*
	 * Tests the algorithm on the sawmill graph
	 */
	@Test
	//@Ignore
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new LocalSpectralClusteringAlgorithm();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(LocalSpectralClusteringAlgorithm.COMMA_SEPARATED_SEED_SET_NAME, "2,34");
		algo.setParameters(parameters);
				
		Cover cover = algo.detectOverlappingCommunities(graph);
		
		System.out.println(cover.toString());
		
		parameters = new HashMap<String, String>();
		parameters.put(LocalSpectralClusteringAlgorithm.COMMA_SEPARATED_SEED_SET_NAME, "9,11");
		algo.setParameters(parameters);
				
		cover = algo.detectOverlappingCommunities(graph);
		
		System.out.println(cover.toString());
		
		parameters = new HashMap<String, String>();
		parameters.put(LocalSpectralClusteringAlgorithm.COMMA_SEPARATED_SEED_SET_NAME, "13,14,21");
		algo.setParameters(parameters);
				
		cover = algo.detectOverlappingCommunities(graph);
		
		System.out.println(cover.toString());
	}
	
}
