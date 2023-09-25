package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;

import jnr.ffi.annotations.Direct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LouvainAlgorithmTest implements DirectedGraphTestReq, UndirectedGraphTestReq,
		WeightedGraphTestReq, OCDAParameterTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new LouvainAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}

	/*
	 * Tests the algorithm on the sawmill graph
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new LouvainAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}
	
	/*
	 * Tests the algorithm on the sawmill graph with maximum layers
	 */
	@Disabled //TODO: remove 555
	@Test
	public void testOnSawmillMaxLayers() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new LouvainAlgorithm(Integer.MAX_VALUE);
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}
}