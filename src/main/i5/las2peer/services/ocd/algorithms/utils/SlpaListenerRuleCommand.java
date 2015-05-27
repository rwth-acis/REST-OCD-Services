package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.Map;

import y.base.Node;

/**
 * Abstract Class for the Listener Rule used by the Speaker Listener
 * Label Propagation Algorithm. Is part of the command pattern.
 * @author Sebastian
 *
 */
public interface SlpaListenerRuleCommand {
	
	/**
	 * Determines the label which the listener node will accept.
	 * @param graph The graph that the algorithm is executed on.
	 * @param listener The listener node.
	 * @param receivedLabels A mapping from each speaker to the label received from that speaker.
	 * @return The accepted label.
	 */
	public abstract int getLabel(CustomGraph graph, Node listener, Map<Node, Integer> receivedLabels);

}
