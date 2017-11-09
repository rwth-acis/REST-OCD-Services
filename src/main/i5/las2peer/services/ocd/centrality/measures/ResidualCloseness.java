package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.algo.ShortestPaths;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

public class ResidualCloseness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.RESIDUAL_ClOSENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();	
		// If there are less than 3 nodes
		if(graph.nodeCount() < 3) {
			while(nc.ok()) {
				res.setNodeValue(nc.node(), 0);
				nc.next();
			}
			return res;
		}
		
		// Calculate the network closeness (for normalization)
		double[] edgeWeights = graph.getEdgeWeights();
		double networkCloseness = 0.0;	
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();
			double[] dist = new double[graph.nodeCount()];
			ShortestPaths.dijkstra(graph, node, true, edgeWeights, dist);
			for(double d : dist) {
				if(d != 0) {
					networkCloseness += 1.0/Math.pow(2, d);
				}
			}
			nc.next();
		}
		
		Matrix A = graph.getNeighbourhoodMatrix();
		int n = graph.nodeCount();
		Node[] nodes = graph.getNodeArray();	
		// Remove and re-add each node (by removing its edges)
		for(int k = 0; k < n; k++) {
			Node currentNode = nodes[k];
			// Remove edges
			EdgeCursor currentNodeEdges = currentNode.edges();
			while(currentNodeEdges.ok()) {
				graph.removeEdge(currentNodeEdges.edge());
				currentNodeEdges.next();
			}
			
			nc.toFirst();
			double[] newEdgeWeights = graph.getEdgeWeights();
			double distSum = 0.0;		
			// Calculate the sum of distances in the graph without the current node
			while(nc.ok()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				Node node = nc.node();
				double[] dist = new double[graph.nodeCount()];
				ShortestPaths.dijkstra(graph, node, true, newEdgeWeights, dist);
				for(double d : dist) {
					if(d != 0) {
						distSum += 1.0/Math.pow(2, d);
					}
				}
				nc.next();
			}
			res.setNodeValue(currentNode, networkCloseness/distSum);
			
			// Recreate edges
			for(int i = 0; i < n; i++) {
				double weight = A.get(currentNode.index(), i);
				if(weight != 0) {
					Edge newEdge = graph.createEdge(currentNode, nodes[i]);
					graph.setEdgeWeight(newEdge, weight);
				}
			}
			for(int i = 0; i < n; i++) {
				double weight = A.get(i, currentNode.index());
				if(weight != 0) {
					Edge newEdge = graph.createEdge(nodes[i], currentNode);
					graph.setEdgeWeight(newEdge, weight);
				}
			}
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.WEIGHTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.RESIDUAL_ClOSENESS;
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
