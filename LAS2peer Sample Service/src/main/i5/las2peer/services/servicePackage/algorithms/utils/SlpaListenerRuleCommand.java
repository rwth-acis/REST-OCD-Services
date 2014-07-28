package i5.las2peer.services.servicePackage.algorithms.utils;

import i5.las2peer.services.servicePackage.graph.CustomGraph;

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
	 */
	public abstract int getLabel(CustomGraph graph, Node listener, Map<Node, Integer> receivedLabels);

}
