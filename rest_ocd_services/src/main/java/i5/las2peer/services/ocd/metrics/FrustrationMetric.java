package i5.las2peer.services.ocd.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.EdgeCursor;

/** 
 * @author YLi
 */
public class FrustrationMetric implements StatisticalMeasure {
	/**
	 * Parameter for weighting between negative edges within communities and
	 * positive edges between communities.
	 */
	private double weightingParameter = 0.5d;

	@Override
	public void setParameters(Map<String, String> parameters) {
		if (parameters.containsKey(WEIGHT_NAME)) {
			weightingParameter = Double.parseDouble(parameters.get(WEIGHT_NAME));
			parameters.remove(WEIGHT_NAME);
			if (weightingParameter < 0 || weightingParameter > 1) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(WEIGHT_NAME, Double.toString(weightingParameter));
		return parameters;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.WEIGHTED);
		compatibleTypes.add(GraphType.NEGATIVE_WEIGHTS);
		return compatibleTypes;
	}

	/**
	 * Creates a standardized instance of the frustration metric.
	 */

	public FrustrationMetric() {
	}

	/**
	 * Creates an instance of the frustration metric with a predefined
	 * parameter.
	 *
	 * @param weightingParameter the weighting parameter
	 */

	public FrustrationMetric(double weightingParameter) {
		this.weightingParameter = weightingParameter;
	}

	protected final String WEIGHT_NAME = "weightingParameter";

	@Override
	public double measure(Cover cover) throws InterruptedException {
		CustomGraph graph = cover.getGraph();
		Matrix membership = cover.getMemberships();
		int communityCount = cover.communityCount();
		int intraEdgeNegative = 0;
		int interEdgePositive = 0;
		int effectiveEdges = graph.edgeCount();
		EdgeCursor edges = graph.edges();
		Edge edge;
		while (edges.ok()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.edge();
			int belongingToSameCommunity = 0;
			int belongingToDiffCommunity = 0;
			for (int i = 0; i < communityCount; i++) {
				if (membership.get(edge.source().index(), i) * membership.get(edge.target().index(), i) != 0.0) {
					belongingToSameCommunity++;
				} else if (membership.get(edge.source().index(), i) * membership.get(edge.target().index(), i) == 0.0
						& (membership.get(edge.source().index(), i)
								+ membership.get(edge.target().index(), i)) != 0.0) {
					belongingToDiffCommunity++;
				}
			}
			/*
			 * If the nodes of an edge belong to more than one community the
			 * edge will be counted more than once when calculating effective
			 * edges
			 */
			if (belongingToSameCommunity > 0) {
				effectiveEdges += belongingToSameCommunity - 1;
			}

			if (graph.getEdgeWeight(edge) < 0) {
				intraEdgeNegative += belongingToSameCommunity;

			} else if (graph.getEdgeWeight(edge) > 0 & belongingToSameCommunity == 0 & belongingToDiffCommunity != 0) {
				/*
				 * if the concerned two nodes are in no common community,the
				 * weight of the edge connecting these nodes should never be
				 * positive
				 */
				interEdgePositive++;
			}
			edges.next();
		}
		return (weightingParameter * intraEdgeNegative + (1 - weightingParameter) * interEdgePositive) / effectiveEdges;
	}
}