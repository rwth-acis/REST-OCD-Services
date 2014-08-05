package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;

import y.base.Node;

public class MergingOfOverlappingCommunitiesTest {

	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunities();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunities();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

	@Test
	public void testOnKnowResultGraph() throws OcdAlgorithmException {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.createNode();
		Node node1 = graph.createNode();
		Node node2 = graph.createNode();
		Node node3 = graph.createNode();
		Node node4 = graph.createNode();
		Node node5 = graph.createNode();
		graph.createEdge(node0, node1);
		graph.createEdge(node0, node2);
		graph.createEdge(node0, node3);
		graph.createEdge(node1, node2);
		graph.createEdge(node3, node4);
		graph.createEdge(node3, node5);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		OcdAlgorithm algo = new MergingOfOverlappingCommunities();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Ignore
	@Test
	public void testOnSiam() throws OcdAlgorithmException, AdapterException, FileNotFoundException {
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunities();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 0);
		System.out.println(cover.toString());
	}
	
}
