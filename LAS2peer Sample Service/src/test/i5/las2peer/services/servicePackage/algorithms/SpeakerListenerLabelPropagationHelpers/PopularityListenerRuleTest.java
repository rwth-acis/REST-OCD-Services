package i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

public class PopularityListenerRuleTest {

	@Test
	public void test() {
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		Node listener = graph.getNodeArray()[0];
		NodeCursor successors = listener.successors();
		Map<Node, Integer> receivedLabels = new HashMap<Node, Integer>();
		while(successors.ok()) {
			receivedLabels.put(successors.node(), 0);
			successors.next();
		}
		successors.toFirst();
		receivedLabels.put(successors.node(), 1);
		successors.toLast();
		receivedLabels.put(successors.node(), 1);
		System.out.println("Labels:");
		System.out.println(receivedLabels);
		ListenerRuleCommand listenerRule = new PopularityListenerRule();
		int chosenLabel = listenerRule.getLabel(graph, listener, receivedLabels);
		System.out.println("Chosen: " + chosenLabel);
		assertEquals(chosenLabel, 0);
	}

}
