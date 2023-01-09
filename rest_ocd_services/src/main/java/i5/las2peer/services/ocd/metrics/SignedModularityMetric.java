package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.*;

import org.la4j.matrix.Matrix;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/** 
 * @author YLi
 */
public class SignedModularityMetric implements StatisticalMeasure {
	public SignedModularityMetric() {
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.WEIGHTED);
		compatibleTypes.add(GraphType.NEGATIVE_WEIGHTS);
		return compatibleTypes;
	}

	@Override
	public double measure(Cover cover) throws InterruptedException {
		double metricValue = 0;
		CustomGraph graph = cover.getGraph();
		double positiveWeightSum = 0;
		double negativeWeightSum = 0;
		/*
		 * calculate the effective number of edges
		 */
		Iterator<Edge> edges = graph.edges().iterator();
		Edge edge;
		while (edges.hasNext()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.next();
			double edgeWeight = graph.getEdgeWeight(edge);
			if (edgeWeight > 0) {
				positiveWeightSum += edgeWeight;
			} else if (edgeWeight < 0) {
				negativeWeightSum += Math.abs(edgeWeight);
			}
			int sameCommunity = examCommunityIdentity(cover, edge.getSourceNode(), edge.getTargetNode());
			if (sameCommunity > 1) {
				if (edgeWeight > 0) {
					positiveWeightSum = positiveWeightSum + edgeWeight * (sameCommunity - 1);
				} else {
					negativeWeightSum = negativeWeightSum - edgeWeight * (sameCommunity - 1);
				}
			}
		}
		/*
		 * Another round of iteration must be carried out, as the effective
		 * number of edges has to be certain for calculation.
		 */
		Iterator<Edge> edgesMod = graph.edges().iterator();
		Edge edgeMod;
		while (edgesMod.hasNext()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			edgeMod = edgesMod.next();
			double edgeWeight = graph.getEdgeWeight(edgeMod);
			int sameCommunity = examCommunityIdentity(cover, edgeMod.getSourceNode(), edgeMod.getTargetNode());
			metricValue += sameCommunity * (edgeWeight
					- (graph.getPositiveInDegree(edgeMod.getSourceNode()) + graph.getPositiveOutDegree(edgeMod.getSourceNode()))
							* (graph.getPositiveOutDegree(edgeMod.getTargetNode())
									+ graph.getPositiveInDegree(edgeMod.getTargetNode()))
							/ (2 * positiveWeightSum)
					+ (graph.getNegativeInDegree(edgeMod.getSourceNode()) + graph.getNegativeOutDegree(edgeMod.getSourceNode()))
							* (graph.getNegativeInDegree(edgeMod.getTargetNode())
									+ graph.getNegativeOutDegree(edgeMod.getTargetNode()))
							/ (2 * negativeWeightSum));
		}
		return metricValue / (2 * positiveWeightSum + 2 * negativeWeightSum);
	}

	/**
	 * Returns the number of communities node A nd node B both belong to.
	 * 
	 * @param cover The cover being measured.
	 * 
	 * @param sourceNode The source node of the edge.
	 * 
	 * @param targetNode The target node of the edge.
	 * 
	 * @return The number of communities node A and node B both belong to.
	 * 
	 */
	private int examCommunityIdentity(Cover cover, Node sourceNode, Node targetNode) throws InterruptedException {
		int sameCommunity = 0;
		Matrix membershipMatrix = cover.getMemberships();
		int communityCount = cover.communityCount();
		for (int i = 0; i < communityCount; i++) {
			if (membershipMatrix.get(sourceNode.getIndex(), i) > 0 & membershipMatrix.get(targetNode.getIndex(), i) > 0) {
				sameCommunity++;
			}
		}
		return sameCommunity;
	}

}
