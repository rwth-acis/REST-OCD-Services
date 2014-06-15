package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;


/**
 * Implements the extended modularity metric.
 */
public class ExtendedModularityMetric {
	
	public ExtendedModularityMetric() {
	}
	
	/**
	 * Returns the index of this metric for a given cover.
	 */
	public double getMetricIndex(Cover cover) {
		double metricValue = 0;
		Graph graph = cover.getGraph();
		NodeCursor sourceNodes = graph.nodes();
		while(sourceNodes.ok()) {
			NodeCursor targetNodes = graph.nodes();
			while(targetNodes.ok()) {
				metricValue +=
						getEdgeModularityContribution(cover, sourceNodes.node(), targetNodes.node());
				targetNodes.next();
			}
			sourceNodes.next();
		}
		return metricValue / graph.edgeCount();
	}
	
	/*
	 * Returns the belonging factor of an edge for a certain community
	 */
	private double getEdgeBelongingCoefficient(Cover cover, Node sourceNode, Node targetNode, int communityIndex) {
		return cover.getBelongingFactor(sourceNode, communityIndex) * cover.getBelongingFactor(targetNode, communityIndex);
	}
	
	/*
	 * Returns the expected belonging coefficient of an incoming edge 
	 * pointing to the target node for a certain community.
	 */
	private double getInEdgeExpectedBelongingCoefficient(Cover cover, Node targetNode, int communityIndex) {
		CustomGraph graph = cover.getGraph();
		double coeff = 0;
		NodeCursor nodes = graph.nodes();
		while(nodes.ok()) {
			Node sourceNode = nodes.node();
			coeff += getEdgeBelongingCoefficient(cover, sourceNode, targetNode, communityIndex);
			nodes.next();
		}
		coeff /= graph.nodeCount();
		return coeff;
	}
	
	/*
	 * Returns the expected belonging coefficient of an outgoing edge 
	 * pointing away from the source node for a certain community.
	 */
	private double getOutEdgeExpectedBelongingCoefficient(Cover cover, Node sourceNode, int communityIndex) {
		CustomGraph graph = cover.getGraph();
		double coeff = 0;
		NodeCursor nodes = graph.nodes();
		while(nodes.ok()) {
			Node targetNode = nodes.node();
			coeff += getEdgeBelongingCoefficient(cover, sourceNode, targetNode, communityIndex);
			nodes.next();
		}
		coeff /= graph.nodeCount();
		return coeff;
	}
	
	/*
	 * Returns the contribution of modularity for the single edge from source node to target node
	 */
	private double getEdgeModularityContribution(Cover cover, Node sourceNode, Node targetNode) {
		double edgeContribution = 0;
		for(int i=0; i<cover.communityCount(); i++) {
			double coverContribution = 0;
			if(sourceNode.getEdgeTo(targetNode) != null) {
				coverContribution = getEdgeBelongingCoefficient(cover, sourceNode, targetNode, i);
			}
			double nullModelContribution = sourceNode.outDegree() * targetNode.inDegree();
			nullModelContribution *= getOutEdgeExpectedBelongingCoefficient(cover, sourceNode, i);
			nullModelContribution *= getInEdgeExpectedBelongingCoefficient(cover, targetNode, i);
			CustomGraph graph = cover.getGraph();
			nullModelContribution /= graph.edgeCount();
			edgeContribution += coverContribution - nullModelContribution;
		}
		return edgeContribution;
	}
}
