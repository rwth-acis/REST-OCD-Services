package i5.las2peer.services.servicePackage.algorithms.utils;

import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import y.base.Edge;
import y.base.Node;

/**
 * Implements a concrete Listener Rule for the Speaker Listener
 * Label Propagation Algorithm. The most frequent label is selected.
 * @author Sebastian
 *
 */
public class SlpaPopularityListenerRule implements SlpaListenerRuleCommand {

	/**
	 * Determines the label which the listener node will accept.
	 */
	@Override
	public int getLabel(CustomGraph graph, Node listener, Map<Node, Integer> receivedLabels) {
		/*
		 * Creates a histogram for the label occurrences.
		 */
		Map<Integer, Double> histogram = new HashMap<Integer, Double>();
		for (Map.Entry<Node, Integer> entry : receivedLabels.entrySet()) {
			int label = entry.getValue();
			Edge edge = listener.getEdgeTo(entry.getKey());
			double influence = graph.getEdgeWeight(edge);
			if(histogram.containsKey(label)) {
				double popularity = histogram.get(label);
				histogram.put(label, popularity + influence);
			}
			else {
				histogram.put(label, influence);
			}
		}
		/*
		 * Derives the most frequent Label.
		 */
		double maxPopularity = Double.NEGATIVE_INFINITY;
		List<Integer> mostPopularLabels = new ArrayList<Integer>();
		for(Map.Entry<Integer, Double> entry : histogram.entrySet()) {
			double popularity = entry.getValue();
			if(popularity >= maxPopularity) {
				if(popularity > maxPopularity) {
					maxPopularity = popularity;
					mostPopularLabels.clear();
				}
				mostPopularLabels.add(entry.getKey());
			}
		}
		int chosenLabel = -1;
		if(mostPopularLabels.size() > 0) {
			/*
			 * Ensures node order does not influence label selection
			 */
			Collections.shuffle(mostPopularLabels);
			chosenLabel = mostPopularLabels.get(0);
		}
		return chosenLabel;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
