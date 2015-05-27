package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.List;

import y.base.Node;

/**
 * Abstract Class for the Speaker Rule used by the Speaker Listener
 * Label Propagation Algorithm. Is part of the command pattern.
 * @author Sebastian
 *
 */
public interface SlpaSpeakerRuleCommand {
	
	/**
	 * Determines the label which a speaker node will send.
	 * @param graph The graph that the algorithm is executed on.
	 * @param speaker The speaker node.
	 * @param memory The memory of the speaker node, i.e. the labels it received so far.
	 * @return The label to send.
	 */
	public abstract int getLabel(CustomGraph graph, Node speaker, List<Integer> memory);
	
}
