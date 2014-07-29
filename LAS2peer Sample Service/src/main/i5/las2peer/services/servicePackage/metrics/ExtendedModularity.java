package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

import java.util.HashMap;

import org.la4j.vector.Vectors;

import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;


/**
 * Implements the extended modularity metric.
 */
public class ExtendedModularity implements StatisticalMeasure {
		
	public ExtendedModularity() {
	}
	
	public void measure(Cover cover) {
		double metricValue = 0;
		Graph graph = cover.getGraph();
		NodeCursor nodesA = graph.nodes();
		NodeCursor nodesB = graph.nodes();
		Node nodeA;
		Node nodeB;
		while(nodesA.ok()) {
			nodesB.toFirst();
			while(nodesB.ok()) {
				nodeA = nodesA.node();
				nodeB = nodesB.node();
				if(nodeB.index() > nodeA.index()) {
					break;
				}
				metricValue +=
						getNodePairModularityContribution(cover, nodesA.node(), nodesB.node());
				nodesB.next();
			}
			nodesA.next();
		}
		if(graph.edgeCount() > 0) {
			metricValue /= graph.edgeCount();
		}
		MetricLog metric = new MetricLog(MetricType.EXTENDED_MODULARITY_METRIC, metricValue, new HashMap<String, String>());
		cover.setMetric(metric);
	}
	
	/*
	 * Returns the belonging coefficient of an edge for a certain community.
	 * @param cover The cover being measured.
	 * @param sourceNode The source node of the edge.
	 * @param targetNode The target node of the edge.
	 * @param communityIndex The community index.
	 * @return The belonging coefficient.
	 */
	private double getEdgeBelongingCoefficient(Cover cover, Node sourceNode, Node targetNode, int communityIndex) {
		return cover.getBelongingFactor(sourceNode, communityIndex) * cover.getBelongingFactor(targetNode, communityIndex);
	}
	
	/*
	 * Returns the modularity index contribution by the null model for two given nodes a and b and a certain community.
	 * This contains the contribution for edge a -> b and edge b -> a.
	 * @param cover The cover being measured.
	 * @param nodeA The first node.
	 * @param nodeB The second node.
	 * @param communityIndex The community index.
	 * @return The null model contribution value.
	 */
	private double getNullModelContribution(Cover cover, Node nodeA, Node nodeB, int communityIndex) {
		double coeff = cover.getBelongingFactor(nodeA, communityIndex);
		coeff *= cover.getBelongingFactor(nodeB, communityIndex);
		coeff *= nodeA.outDegree() * nodeB.inDegree() + nodeA.inDegree() * nodeB.outDegree();
		if(coeff != 0) {
			coeff /= Math.pow(cover.getGraph().nodeCount(), 2);
			/*
			 * Edge count cannot be 0 here due to the node degrees.
			 */
			coeff /= cover.getGraph().edgeCount();
			coeff *= Math.pow(cover.getMemberships().getColumn(communityIndex).fold(Vectors.mkManhattanNormAccumulator()), 2);
		}
		return coeff;
	}

	/*
	 * Returns the modularity index for the two nodes a and b.
	 * This includes the edges a -> b and b -> a.
	 * @param cover The cover being measured.
	 * @param nodeA The first node.
	 * @param nodeB The second node.
	 * @return The modularity index for nodes a and b with regard to all communities.
	 */
	private double getNodePairModularityContribution(Cover cover, Node nodeA, Node nodeB) {
		double edgeContribution = 0;
		for(int i=0; i<cover.communityCount(); i++) {
			double coverContribution = 0;
			if(nodeA.getEdgeTo(nodeB) != null) {
				coverContribution += getEdgeBelongingCoefficient(cover, nodeA, nodeB, i);
			}
			if(nodeB.getEdgeTo(nodeA) != null) {
				coverContribution += getEdgeBelongingCoefficient(cover, nodeB, nodeA, i);
			}
			double nullModelContribution = getNullModelContribution(cover, nodeA, nodeB, i);
			edgeContribution += coverContribution - nullModelContribution;
		}
		return edgeContribution;
	}
}
