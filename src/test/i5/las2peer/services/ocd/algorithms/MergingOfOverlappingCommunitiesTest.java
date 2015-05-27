package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.MergingOfOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import y.base.Node;

public class MergingOfOverlappingCommunitiesTest {

	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

	@Test
	public void testOnKnowResultGraph() throws OcdAlgorithmException, InterruptedException {
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
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Ignore
	@Test
	public void testOnSiam() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 0);
		System.out.println(cover.toString());
	}
	
}
