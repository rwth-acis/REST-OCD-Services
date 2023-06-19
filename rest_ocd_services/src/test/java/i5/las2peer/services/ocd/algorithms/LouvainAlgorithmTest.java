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
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class LouvainAlgorithmTest {

	/*
	 * Tests the algorithm on the sawmill graph
	 */
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, IOException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new LouvainAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	/*
	 * Tests the algorithm on the sawmill graph with maximum layers
	 */
	@Test
	public void testOnSawmillMaxLayers() throws OcdAlgorithmException, AdapterException, IOException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new LouvainAlgorithm(Integer.MAX_VALUE);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
}