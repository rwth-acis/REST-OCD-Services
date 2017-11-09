package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.algo.ShortestPaths;
import y.base.Node;
import y.base.NodeCursor;

public class Integration implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.INTEGRATION, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();
		// If there is only a single node
		if(graph.nodeCount() == 1) {
			res.setNodeValue(nc.node(), 0);
			return res;
		}
		
		// Calculate the sum of distances and the number of reachable nodes for all nodes and find the diameter of the graph
		double[] edgeWeights = graph.getEdgeWeights();
		Map<Node, Integer> reachableNodes = new HashMap<Node, Integer>();
		double maxDistance = 0;
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();
			double[] dist = new double[graph.nodeCount()];
			
			ShortestPaths.dijkstra(graph, node, true, edgeWeights, dist);
			double distSum = 0.0;
			int reachableNodesCounter = 0;
			for(double d : dist) {
				if(d != Double.POSITIVE_INFINITY && d != 0) {
					distSum += d;
					reachableNodesCounter++;
					if(d > maxDistance)
						maxDistance = d;
				}
			}
			reachableNodes.put(node, reachableNodesCounter);
			res.setNodeValue(node, distSum);
			nc.next();
		}
		
		// Reverse distances
		nc.toFirst();
		while(nc.ok()) {
			Node node = nc.node();
			double distSum = res.getNodeValue(node);
			/*
			 * Each distance in the sum is reversed which is equivalent to multiplying the number of reachable nodes with the 
			 * diameter of the graph and subtracting the sum of distances (+ 1 added to differentiate disconnected nodes).
			 */
			res.setNodeValue(node, (reachableNodes.get(node) * (1 + maxDistance) - distSum)/(graph.nodeCount()-1));
			nc.next();
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
		return CentralityMeasureType.INTEGRATION;
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
