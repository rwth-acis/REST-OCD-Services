package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;

import java.util.*;

import org.graphstream.graph.Node;

public class OmegaIndex implements KnowledgeDrivenMeasure {
	
	@Override
	public void setParameters(Map<String, String> parameters) {
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public double measure(Cover cover, Cover groundTruth) throws OcdMetricException, InterruptedException {
		Map<Set<Node>, Integer> sharedCommunitiesAlgo = getSharedCommunities(cover);
		Map<Set<Node>, Integer> sharedCommunitiesTruth = getSharedCommunities(groundTruth);
		int pairsInAgreementCount = 0;
		Map<Integer, Integer> sharedCommunityCountsAlgo = new HashMap<Integer, Integer>();
		Map<Integer, Integer> sharedCommunityCountsTruth = new HashMap<Integer, Integer>();
		Iterator<Node> nodesA = cover.getGraph().iterator();
		Iterator<Node> nodesB = cover.getGraph().iterator();
		/*
		 * Calculates the number of nodes in agreement and of pairs sharing a given number of communities.
		 */
		while(nodesA.hasNext()) {
			Node nodeA = nodesA.next();
			nodesB = cover.getGraph().iterator();
			while(nodesB.hasNext()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				Node nodeB = nodesB.next();
				if(nodeB.getIndex() < nodeA.getIndex()) {
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
			}
		}
		/*
		 * Calculates the actual omega index.
		 */
		int n = cover.getGraph().getNodeCount();
		int pairsCount = ( n * (n - 1) ) / 2;
		double unadjustedIndex = 1d / pairsCount * pairsInAgreementCount;
		double expectedIndex = calculateExpectedIndex(sharedCommunityCountsAlgo, sharedCommunityCountsTruth, pairsCount);
		double metricValue = (unadjustedIndex - expectedIndex) / (1 - expectedIndex);
		return metricValue;
	}
	
	/**
	 * Calculates the number of shared communities for each node pair.
	 * @param cover The corresponding cover.
	 * @return A mapping from the node pairs to the count of shared communities.
	 */
	private Map<Set<Node>, Integer> getSharedCommunities(Cover cover) throws InterruptedException {
		Map<Set<Node>, Integer> sharedCommunityCounts = new HashMap<Set<Node>, Integer>();
		Integer count;
		Node nodeA;
		Node nodeB;
		Iterator<Node> nodesA = cover.getGraph().iterator();
		Iterator<Node> nodesB = cover.getGraph().iterator();
		for(int i = 0; i<cover.communityCount(); i++) {
			while(nodesA.hasNext()) {
				nodeA = nodesA.next();
				if(cover.getBelongingFactor(nodeA, i) > 0) {
					while(nodesB.hasNext()) {
						if(Thread.interrupted()) {
							throw new InterruptedException();
						}
						nodeB = nodesB.next();
						/*
						 * Pairs are regarded only once.
						 */
						if(nodeA.getIndex() <= nodeB.getIndex()) {
							break;
						}
						if(cover.getBelongingFactor(nodeB, i) > 0) {
							Set<Node> pair = new HashSet<Node>();
							pair.add(nodeA);
							pair.add(nodeB);
							count = sharedCommunityCounts.get(pair);
							if(count == null) {
								count = 1;
							}
							else {
								count++;
							}
							sharedCommunityCounts.put(pair, count);
						}
					}
				}
				nodesB = cover.getGraph().iterator();
			}
			nodesA = cover.getGraph().iterator();
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
