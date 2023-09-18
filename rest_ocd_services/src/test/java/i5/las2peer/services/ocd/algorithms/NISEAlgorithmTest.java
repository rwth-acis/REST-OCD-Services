package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

//TODO: GRAPHSTREAM UPGRADE: Check with further graphs, the tests here give every node the same belonging to every community. This is intended but not exhaustive enough
public class NISEAlgorithmTest implements UndirectedGraphTestReq {


	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new NISEAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}

	@Disabled //TODO: remove 555
	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algorithm = new NISEAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(NISEAlgorithm.ACCURACY_NAME, "0.0000001");
		algorithm.setParameters(parameters);
		Cover cover = algorithm.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}


	@Disabled //TODO: remove 555
	@Test
	public void testOnSignedLfrMadeUndirectedGraph() throws OcdAlgorithmException, InterruptedException, OcdMetricException, FileNotFoundException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrMadeUndirectedGraph();
		OcdAlgorithm algorithm = new NISEAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		algorithm.setParameters(parameters);
		Cover cover = algorithm.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

}
