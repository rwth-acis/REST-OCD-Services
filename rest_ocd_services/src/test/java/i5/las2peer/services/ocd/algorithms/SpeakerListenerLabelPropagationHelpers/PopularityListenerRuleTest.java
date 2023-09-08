package i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationHelpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import i5.las2peer.services.ocd.algorithms.utils.SlpaListenerRuleCommand;
import i5.las2peer.services.ocd.algorithms.utils.SlpaPopularityListenerRule;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.util.HashMap;
import java.util.Map;

import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;

public class PopularityListenerRuleTest {

	@Test
	public void test() throws InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		Node listener = graph.nodes().toArray(Node[]::new)[0];
		Node[] successors = graph.getSuccessorNeighbours(listener).toArray(new Node[0]);
		Map<Node, Integer> receivedLabels = new HashMap<Node, Integer>();
		for (Node successor : successors) {
			receivedLabels.put(successor, 0);
		}
		receivedLabels.put(successors[0], 1);
		receivedLabels.put(successors[successors.length == 0 ? 0 : successors.length-1], 1);
		//System.out.println("Labels:");
		//System.out.println(receivedLabels);
		SlpaListenerRuleCommand listenerRule = new SlpaPopularityListenerRule();
		int chosenLabel = listenerRule.getLabel(graph, listener, receivedLabels);
		//System.out.println("Chosen: " + chosenLabel);
		assertEquals(chosenLabel, 0);
	}

}
