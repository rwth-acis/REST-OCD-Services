package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.graphstream.graph.Node;

public class MergingOfOverlappingCommunitiesTest implements UndirectedGraphTestReq, WeightedGraphTestReq
		, OCDAParameterTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new MergingOfOverlappingCommunitiesAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}

	@Disabled //TODO: remove 555
	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

	@Disabled //TODO: remove 555
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

	@Disabled //TODO: remove 555
	@Test
	public void testOnKnowResultGraph() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		CustomGraph graph = new CustomGraph();
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		Node node2 = graph.addNode("2");
		Node node3 = graph.addNode("3");
		Node node4 = graph.addNode("4");
		Node node5 = graph.addNode("5");
		graph.addEdge(UUID.randomUUID().toString(), node0, node1);
		graph.addEdge(UUID.randomUUID().toString(), node0, node2);
		graph.addEdge(UUID.randomUUID().toString(), node0, node3);
		graph.addEdge(UUID.randomUUID().toString(), node1, node2);
		graph.addEdge(UUID.randomUUID().toString(), node3, node4);
		graph.addEdge(UUID.randomUUID().toString(), node3, node5);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		OcdAlgorithm algo = new MergingOfOverlappingCommunitiesAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

	
}
