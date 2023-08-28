package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;

public class AlgorithmsBoundaryTest {

	@Test
	public void testAlgorithmsBoundaries() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		List<OcdAlgorithm> algos = new ArrayList<OcdAlgorithm>();
		algos.add(new ClizzAlgorithm());
		algos.add(new RandomWalkLabelPropagationAlgorithm());
		algos.add(new ExtendedSpeakerListenerLabelPropagationAlgorithm());
		algos.add(new LinkCommunitiesAlgorithm());
		algos.add(new NISEAlgorithm());
		algos.add(new SpeakerListenerLabelPropagationAlgorithm());
		algos.add(new SskAlgorithm());
		algos.add(new WeightedLinkCommunitiesAlgorithm());
		List<CustomGraph> graphs = new ArrayList<CustomGraph>();
		CustomGraph graph = new CustomGraph();
		graphs.add(graph);
		graph = new CustomGraph();
		graph.addNode("firstNode");
		graphs.add(graph);
		graph = new CustomGraph();
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		Edge edge = graph.addEdge(UUID.randomUUID().toString(), node0, node1);
		graph.setEdgeWeight(edge, 2);
		graph.addType(GraphType.DIRECTED);
		graphs.add(graph);
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		for(OcdAlgorithm currentAlgo : algos) {
			for(CustomGraph currentGraph : graphs) {
				Cover cover = executor.execute(currentGraph, currentAlgo, 0);
				System.out.println("Algo: " + currentAlgo.getAlgorithmType().name() 
						+ ", Node Count: " + currentGraph.getNodeCount());
				System.out.println(cover + "\n");
			}
		}
	}

}
