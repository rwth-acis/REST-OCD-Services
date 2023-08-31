package i5.las2peer.services.ocd.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.graphstream.graph.Edge;


public class EvolutionaryAlgorithmBasedOnSimilarityTest {
	/*
	 * Test whether a graph can be translated into the paj format.
	 */

	@Test
	public void testWriteNetworkFile()
			throws AdapterException, IOException, InterruptedException, OcdAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getFiveNodesGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		EvolutionaryAlgorithmBasedOnSimilarity algo = new EvolutionaryAlgorithmBasedOnSimilarity();
		Iterator<Edge> edges = graph.edges().iterator();
		Edge edge;
		System.out.println("Graph:");
		while (edges.hasNext()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.next();
			System.out.println(edge + " " + graph.getEdgeWeight(edge));
		}
		algo.writeNetworkFile(graph);
	}

	@Test
	public void testTranslateCommunityFile() throws IOException, InterruptedException {
		EvolutionaryAlgorithmBasedOnSimilarity algo = new EvolutionaryAlgorithmBasedOnSimilarity();
		String LastResultPath = OcdTestConstants.meaResultFilePath;
		/*
		 * translating the result of MEA running on the signedLfrNetwork with 12
		 * nodes.
		 */
		Matrix membership = algo.translateCommunityFile(LastResultPath, 12);
		Matrix expectedMembership = new Basic2DMatrix(12, 3);
		System.out.println("Test result:");
		System.out.println(membership);
		expectedMembership.set(0, 0, 0.5);
		expectedMembership.set(0, 1, 0.5);
		expectedMembership.set(0, 2, 0);
		expectedMembership.set(1, 0, 0.5);
		expectedMembership.set(1, 1, 0.5);
		expectedMembership.set(1, 2, 0);
		expectedMembership.set(2, 0, 0);
		expectedMembership.set(2, 1, 1);
		expectedMembership.set(2, 2, 0);
		expectedMembership.set(3, 0, 0);
		expectedMembership.set(3, 1, 1);
		expectedMembership.set(3, 2, 0);
		expectedMembership.set(4, 0, 1);
		expectedMembership.set(4, 1, 0);
		expectedMembership.set(4, 2, 0);
		expectedMembership.set(5, 0, 1);
		expectedMembership.set(5, 1, 0);
		expectedMembership.set(5, 2, 0);
		expectedMembership.set(6, 0, 1);
		expectedMembership.set(6, 1, 0);
		expectedMembership.set(6, 2, 0);
		expectedMembership.set(7, 0, 0.5);
		expectedMembership.set(7, 1, 0);
		expectedMembership.set(7, 2, 0.5);
		expectedMembership.set(8, 0, 1);
		expectedMembership.set(8, 1, 0);
		expectedMembership.set(8, 2, 0);
		expectedMembership.set(9, 0, 0);
		expectedMembership.set(9, 1, 0);
		expectedMembership.set(9, 2, 1);
		expectedMembership.set(10, 0, 0);
		expectedMembership.set(10, 1, 1);
		expectedMembership.set(10, 2, 0);
		expectedMembership.set(11, 0, 1);
		expectedMembership.set(11, 1, 0);
		expectedMembership.set(11, 2, 0);
		assertEquals(expectedMembership, membership);
	}

	@Test
	public void testDetectOverlappingCommunities()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrMadeUndirectedGraph();
		EvolutionaryAlgorithmBasedOnSimilarity algo = new EvolutionaryAlgorithmBasedOnSimilarity();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
}
