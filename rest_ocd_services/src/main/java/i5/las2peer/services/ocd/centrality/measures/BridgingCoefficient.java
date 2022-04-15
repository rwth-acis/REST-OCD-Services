package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;


/**
 * Implementation of the Bridging Coefficient.
 * See: Hwang, Woochang and Kim, Taehyong and Ramanathan, Murali and Zhang, Aidong. 2008. Bridging centrality: graph mining from element level to group level.
 * @author Tobias
 *
 */
public class BridgingCoefficient implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.UNDEFINED, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();	
			// Calculate the probability of leaving the direct neighborhood subgraph in two steps
			double leavingProbability = 0.0;
			double nodeWeightedOutDegree = graph.getWeightedOutDegree((MultiNode) node);
			for (Node neighbor : graph.getSuccessorNeighbours(node)) {
				double nodeEdgeWeight = graph.getEdgeWeight(node.getEdgeToward(neighbor));
				if (nodeEdgeWeight > 0) {
					double neighborWeightedOutDegree = graph.getWeightedOutDegree((MultiNode) neighbor);
					for (Node twoStepNeighbor : graph.getSuccessorNeighbours(neighbor)) {
						double neighborEdgeWeight = graph.getEdgeWeight(neighbor.getEdgeToward(twoStepNeighbor));
						if (twoStepNeighbor != node && node.getEdgeToward(twoStepNeighbor) == null) {
							// If twoStepNeighbor is not in the direct neighborhood graph
							leavingProbability += nodeEdgeWeight / nodeWeightedOutDegree * neighborEdgeWeight / neighborWeightedOutDegree;
						}
					}
				}
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
		return CentralityMeasureType.UNDEFINED;
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
