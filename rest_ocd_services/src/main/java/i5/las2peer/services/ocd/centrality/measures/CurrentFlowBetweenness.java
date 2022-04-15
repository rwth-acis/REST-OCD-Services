package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import org.la4j.inversion.GaussJordanInverter;
import org.la4j.inversion.MatrixInverter;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Edge;

import org.graphstream.graph.Node;


/**
 * Implementation of Current-Flow Betweenness.
 * See: Brandes, Ulrik and Fleischer, Daniel. 2005. Centrality Measures Based on Current Flow.
 * @author Tobias
 *
 */
public class CurrentFlowBetweenness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CURRENT_FLOW_BETWEENNESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		// If the graph contains no edges
		if(graph.getEdgeCount() == 0) {
			while(nc.hasNext()) {
				Node node = nc.next();
				res.setNodeValue(node, 0);
			}
			return res;
		}
		
		int n = graph.getNodeCount();
		Matrix L = new CCSMatrix(n, n);
		
		// Create laplacian matrix
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			int i = node.getIndex();
			L.set(i, i, graph.getWeightedInDegree(node));
		}
		Iterator<Edge> ec = graph.edges().iterator();
		while(ec.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Edge edge = ec.next();
			L.set(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex(), -graph.getEdgeWeight(edge));
		}

		// Remove the first row and column
		L = L.slice(1, 1, n, n);
		
		MatrixInverter gauss = new GaussJordanInverter(L);
		Matrix L_inverse = gauss.inverse();
		
		// Create matrix C
		Matrix C = new CCSMatrix(n, n);
		for(int i = 0; i < n-1; i++) {
			for(int j = 0; j < n-1; j++) {
				C.set(i+1, j+1, L_inverse.get(i, j));
			}
		}
		
		/*
		 * Each (undirected) edge must have an arbitrary but fixed orientation, 
		 * here it points from the node with the smaller index to the one with the higher index.
		 * The edge in the opposite direction is removed.
		 */
		ec = graph.edges().iterator();
		while(ec.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Edge edge = ec.next();
			Node s = edge.getSourceNode();
			Node t = edge.getTargetNode();
			if(s.getIndex() < t.getIndex()) {
				Edge reverseEdge = t.getEdgeToward(s);
				graph.removeEdge(reverseEdge);
			}
		}
		
		// Create matrix B
		ec = graph.edges().iterator();
		int m = graph.getEdgeCount();
		Matrix B = new CCSMatrix(m, n);
		int edgeIndex = 0;
		while(ec.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Edge edge = ec.next();
			int s = edge.getSourceNode().getIndex();
			int t = edge.getTargetNode().getIndex();
			B.set(edgeIndex, s, graph.getEdgeWeight(edge));
			B.set(edgeIndex, t, -graph.getEdgeWeight(edge));
			edgeIndex++;
		}
		Matrix F = B.multiply(C);
		int normalizationFactor = (n-2)*(n-1);
		Node[] nodeArray = graph.nodes().toArray(Node[]::new);
		nc = graph.nodes().iterator();
		
		// Calculate centrality value for each node
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();	
			double throughputSum = 0.0;		
			for(int sourceIndex = 0; sourceIndex < n; sourceIndex++) {
				for(int targetIndex = sourceIndex+1; targetIndex < n; targetIndex++) {
					if(sourceIndex != node.getIndex() && targetIndex != node.getIndex()) {
						Node s = nodeArray[sourceIndex];
						Node t = nodeArray[targetIndex];		
						ec = graph.edges().iterator();
						edgeIndex = 0;
						while(ec.hasNext()) {
							Edge edge = ec.next();
							if(edge.getTargetNode() == node || edge.getSourceNode() == node) {
								throughputSum += Math.abs(F.get(edgeIndex, s.getIndex()) - F.get(edgeIndex, t.getIndex()));
							}
							edgeIndex++;
						}
					}
				}
			}
			res.setNodeValue(node, 1.0/normalizationFactor * throughputSum/2);
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.WEIGHTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.CURRENT_FLOW_BETWEENNESS;
	}
	
	@Override
	public HashMap<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
}
