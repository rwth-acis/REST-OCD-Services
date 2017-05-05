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
	public void testWriteNetworkFile() throws AdapterException, IOException, InterruptedException {
		CustomGraph graph = new CustomGraph();
		Node n[] = new Node[4];
		for (int i = 0; i < 4; i++) {
			n[i] = graph.createNode();
		}
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[2], n[1]);
		graph.createEdge(n[1], n[3]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[2]);
		graph.setEdgeWeight(graph.getEdgeArray()[0], 1);
		graph.setEdgeWeight(graph.getEdgeArray()[1], -1);
		graph.setEdgeWeight(graph.getEdgeArray()[2], 1);
		graph.setEdgeWeight(graph.getEdgeArray()[3], -1);
		graph.setEdgeWeight(graph.getEdgeArray()[4], 1);
		graph.setEdgeWeight(graph.getEdgeArray()[5], 1);
		graph.setEdgeWeight(graph.getEdgeArray()[6], -1);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		algo.writeNetworkFile(graph);
	}
	
	@Ignore
	@Test
	public void testGetMembershipMatrix() throws IOException, InterruptedException {
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		/*
		 * The result file of running SPM on the party network with 10 nodes
		 */
		File resultFile = new File(OcdTestConstants.spmResultFilePath);
		Matrix membershipMatrix = algo.getMembershipMatrix(resultFile, 10);
		System.out.println("Test result: membership");
		System.out.println(membershipMatrix.toString());
		Matrix expectedMatrix = new Basic2DMatrix(10, 2);
		expectedMatrix.set(0, 0, 0);
		expectedMatrix.set(0, 1, 1);
		expectedMatrix.set(1, 0, 0);
		expectedMatrix.set(1, 1, 1);
		expectedMatrix.set(2, 0, 0);
		expectedMatrix.set(2, 1, 1);
		expectedMatrix.set(3, 0, 0);
		expectedMatrix.set(3, 1, 1);
		expectedMatrix.set(4, 0, 0.0171821306);
		expectedMatrix.set(4, 1, 0.9828178694);
		expectedMatrix.set(5, 0, 1);
		expectedMatrix.set(5, 1, 0);
		expectedMatrix.set(6, 0, 1);
		expectedMatrix.set(6, 1, 0);
		expectedMatrix.set(7, 0, 1);
		expectedMatrix.set(7, 1, 0);
		expectedMatrix.set(8, 0, 1);
		expectedMatrix.set(8, 1, 0);
		expectedMatrix.set(9, 0, 1);
		expectedMatrix.set(9, 1, 0);
		assertEquals(expectedMatrix, membershipMatrix);
	}
	
	@Ignore
	@Test
	public void testDetectOverlappingCommunities()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		SignedProbabilisticMixtureAlgorithm algo = new SignedProbabilisticMixtureAlgorithm();
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Detected Cover:");
		System.out.println(cover.toString());
	}
}
