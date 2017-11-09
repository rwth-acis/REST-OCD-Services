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
import y.base.Node;
import y.base.NodeCursor;

public class BridgingCoefficient implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.BRIDGING_COEFFICIENT, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();	
			// Calculate the probability of leaving the direct neighborhood subgraph in two steps
			double leavingProbability = 0.0;
			double nodeWeightedOutDegree = graph.getWeightedOutDegree(node);
			NodeCursor neighbors = node.successors();
			while(neighbors.ok()) {
				Node neighbor = neighbors.node();
				double nodeEdgeWeight = graph.getEdgeWeight(node.getEdgeTo(neighbor));
				if(nodeEdgeWeight > 0) {
					double neighborWeightedOutDegree = graph.getWeightedOutDegree(neighbor);
					NodeCursor twoStepNeighbors = neighbor.successors();
					while(twoStepNeighbors.ok()) {
						Node twoStepNeighbor = twoStepNeighbors.node();
						double neighborEdgeWeight = graph.getEdgeWeight(neighbor.getEdgeTo(twoStepNeighbor));
						if(twoStepNeighbor != node && node.getEdgeTo(twoStepNeighbor) == null) {
							// If twoStepNeighbor is not in the direct neighborhood graph
							leavingProbability += nodeEdgeWeight/nodeWeightedOutDegree * neighborEdgeWeight/neighborWeightedOutDegree;
						}
						twoStepNeighbors.next();
					}
				}
				neighbors.next();
			}	
			res.setNodeValue(node, leavingProbability);
			nc.next();
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
		return CentralityMeasureType.BRIDGING_COEFFICIENT;
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
