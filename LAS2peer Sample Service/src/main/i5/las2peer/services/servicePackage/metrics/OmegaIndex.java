package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Community;
import i5.las2peer.services.servicePackage.graph.Cover;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import y.base.Node;
import y.base.NodeCursor;

public class OmegaIndex implements KnowledgeDrivenMeasure {

	@Override
	public void measure(Cover cover, Cover groundTruth) throws MetricException {
		Map<Set<Node>, Integer> sharedCommunitiesAlgo = getSharedCommunities(cover);
		Map<Set<Node>, Integer> sharedCommunitiesTruth = getSharedCommunities(groundTruth);
		int pairsInAgreementCount = 0;
		Map<Integer, Integer> sharedCommunityCountsAlgo = new HashMap<Integer, Integer>();
		Map<Integer, Integer> sharedCommunityCountsTruth = new HashMap<Integer, Integer>();
		NodeCursor nodesA = cover.getGraph().nodes();
		NodeCursor nodesB = cover.getGraph().nodes();
		/*
		 * Calculates the number of nodes in agreement and of pairs sharing a given number of communities.
		 */
		while(nodesA.ok()) {
			Node nodeA = nodesA.node();
			nodesB.toFirst();
			while(nodesB.ok()) {
				Node nodeB = nodesB.node();
				if(nodeB.index() < nodeA.index()) {
					Set<Node> pair = new HashSet<Node>();
					pair.add(nodeA);
					pair.add(nodeB);
					Integer sharedAlgo = sharedCommunitiesAlgo.get(pair);
					if(sharedAlgo == null) {
						sharedAlgo = 0;
					}
					Integer count = sharedCommunityCountsAlgo.get(sharedAlgo);
					if(count == null) {
						count = 1;
					}
					else {
						count++;
					}
					sharedCommunityCountsAlgo.put(sharedAlgo, count);
					Integer sharedTruth = sharedCommunitiesTruth.get(pair);
					if(sharedTruth == null) {
						sharedTruth = 0;
					}
					count = sharedCommunityCountsTruth.get(sharedTruth);
					if(count == null) {
						count = 1;
					}
					else {
						count++;
					}
					sharedCommunityCountsTruth.put(sharedTruth, count);
					if(sharedTruth == sharedAlgo) {
						pairsInAgreementCount++;
					}
				}
				else {
					break;
				}
				nodesB.next();
			}
			nodesA.next();
		}
		/*
		 * Calculates the actual omega index.
		 */
		int n = cover.getGraph().nodeCount();
		int pairsCount = ( n * (n - 1) ) / 2;
		double unadjustedIndex = 1d / (double) pairsCount * (double) pairsInAgreementCount;
		double expectedIndex = calculateExpectedIndex(sharedCommunityCountsAlgo, sharedCommunityCountsTruth, pairsCount);
		double metricValue = (unadjustedIndex - expectedIndex) / (1 - expectedIndex);
		MetricLog metric = new MetricLog(MetricType.OMEGA_INDEX, metricValue, new HashMap<String, String>(), cover);
		cover.setMetric(metric);
	}
	
	/*
	 * Calculates the number of shared communities for each node pair.
	 * @param cover The corresponding cover.
	 * @return A mapping from the node pairs to the count of shared communities.
	 */
	private Map<Set<Node>, Integer> getSharedCommunities(Cover cover) {
		Map<Set<Node>, Integer> sharedCommunityCounts = new HashMap<Set<Node>, Integer>();
		Integer count;
		Iterator<Map.Entry<Node, Double>> itA;
		Iterator<Map.Entry<Node, Double>> itB;
		Map.Entry<Node, Double> entryA;
		Map.Entry<Node, Double> entryB;
		int iterationA;
		int iterationB;
		for(Community community : cover.getCommunities()) {
			itA = community.getMemberships().entrySet().iterator();
			iterationA = 0;
			while(itA.hasNext()) {
				entryA = itA.next();
				if(entryA.getValue() > 0) {
					itB = community.getMemberships().entrySet().iterator();
					iterationB = 0;
					while(itB.hasNext() && iterationA > iterationB) {	
						entryB = itB.next();
						if(entryB.getValue() > 0) {
							Set<Node> pair = new HashSet<Node>();
							pair.add(entryA.getKey());
							pair.add(entryB.getKey());
							count = sharedCommunityCounts.get(pair);
							if(count == null) {
								count = 1;
							}
							else {
								count++;
							}
							sharedCommunityCounts.put(pair, count);
						}
						iterationB++;
					}
				}
				iterationA++;
			}
		}
		return sharedCommunityCounts;
	}
	
	private double calculateExpectedIndex(Map<Integer, Integer> sharedCommunityCountsAlgo, Map<Integer, Integer> sharedCommunityCountsTruth, int pairsCount) {
		double expectedIndex = 0;
		for(Map.Entry<Integer, Integer> entry : sharedCommunityCountsAlgo.entrySet()) {
			Integer truthValue = sharedCommunityCountsTruth.get(entry.getKey());
			if(truthValue != null) {
				expectedIndex += truthValue * entry.getValue();
			}
		}
		expectedIndex /= Math.pow(pairsCount, 2);
		return expectedIndex;
	}

}
