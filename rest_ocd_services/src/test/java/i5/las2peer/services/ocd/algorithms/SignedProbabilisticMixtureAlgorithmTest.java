package i5.las2peer.services.ocd.algorithms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import y.base.Node;

public class SignedProbabilisticMixtureAlgorithmTest {	
	@Test
	public void testSmall()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedWeighted();
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Detected Cover:");
		System.out.println(cover.toString());
	}
	
	@Test
	public void testLfrSmall()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrSixNodesGraph();
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Detected Cover:");
		System.out.println(cover.toString());
	}
	
	@Ignore
	@Test
	public void testWikiElec()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getWikiElecGraph();
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Detected Cover:");
		System.out.println(cover.toString());
	}
}
