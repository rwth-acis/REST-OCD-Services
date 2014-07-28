package i5.las2peer.services.servicePackage.algorithms.utils;

import i5.las2peer.services.servicePackage.graph.CustomGraph;

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
	 */
	public abstract int getLabel(CustomGraph graph, Node speaker, List<Integer> memory);
	
}
