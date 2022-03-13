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
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import org.graphstream.graph.Node;

public class MergingOfOverlappingCommunitiesTest {

	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

	@Test
	public void testOnKnowResultGraph() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		Node node2 = graph.addNode("2");
		Node node3 = graph.addNode("3");
		Node node4 = graph.addNode("4");
		Node node5 = graph.addNode("5");
		graph.addEdge(node0.getId()+node1.getId(), node0, node1);
		graph.addEdge(node0.getId()+node2.getId(), node0, node2);
		graph.addEdge(node0.getId()+node3.getId(), node0, node3);
		graph.addEdge(node1.getId()+node2.getId(), node1, node2);
		graph.addEdge(node3.getId()+node4.getId(), node3, node4);
		graph.addEdge(node3.getId()+node5.getId(), node3, node5);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Ignore
	@Test
	public void testOnSiam() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, algo, 0);
		System.out.println(cover.toString());
	}
	
}
