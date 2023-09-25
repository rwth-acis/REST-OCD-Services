package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;

import i5.las2peer.services.ocd.test_interfaces.ocda.NegativeWeightGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class SignedProbabilisticMixtureAlgorithmTest implements UndirectedGraphTestReq, WeightedGraphTestReq,
		NegativeWeightGraphTestReq, OCDAParameterTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new SignedProbabilisticMixtureAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}

	@Disabled //TODO: remove 555
	@Test
	public void testSmall()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedWeighted();
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println("Detected Cover:");
		//System.out.println(cover.toString());
	}

	@Disabled //TODO: remove 555
	@Test
	public void testLfrSmall()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrSixNodesGraph();
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println("Detected Cover:");
		//System.out.println(cover.toString());
	}
	

}
