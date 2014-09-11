package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.List;
import java.util.Random;

import y.base.Node;

/**
 * Implements a concrete Speaker Rule for the Speaker Listener
 * Label Propagation Algorithm. The label is chosen through a uniform distribution.
 * @author Sebastian
 *
 */
public class SlpaUniformSpeakerRule implements SlpaSpeakerRuleCommand {

	/**
	 * Determines the label which a speaker node will send.
	 */
	@Override
	public int getLabel(CustomGraph graph, Node speaker, List<Integer> memory) {
		/*
		 * Selects a random label from the node memory.
		 */
		Random rand = new Random();
		return memory.get(rand.nextInt(memory.size()));
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
