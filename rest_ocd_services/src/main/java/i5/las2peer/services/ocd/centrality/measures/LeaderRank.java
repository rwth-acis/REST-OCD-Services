package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Edge;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;


/**
 * Implementation of LeaderRank.
 * See: Lü, Linyuan and Zhang, Yi-Cheng and Yeung, Chi Ho and Zhou, Tao. 2011. Leaders in social networks, the delicious case. 
 * @author Tobias
 *
 */
public class LeaderRank implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.LEADERRANK, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		int n = graph.getNodeCount();
		// Set initial LeaderRank of all nodes to 1
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), 1.0);
		}
		nc = graph.iterator();
		
		// Add ground node
		Node groundNode = graph.addNode("groundNode");
		while(nc.hasNext()) {
			Node node = nc.next();
			if(node != groundNode) {
				// Add bidirectional edges
				Edge e1 = graph.addEdge(UUID.randomUUID().toString(), groundNode, node);
				Edge e2 = graph.addEdge(UUID.randomUUID().toString(), node, groundNode);
				graph.setEdgeWeight(e1, 1.0);
				graph.setEdgeWeight(e2, 1.0);
			}
		}
		nc = graph.iterator();
		res.setNodeValue(groundNode, 0.0);
		
		for(int k = 0; k < 50; k++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			while(nc.hasNext()) {
				Node i = nc.next();
				double weightedRankSum = 0.0;
				
				Iterator<Edge> inLinks = i.enteringEdges().iterator();
				while(inLinks.hasNext()) {
					Edge eji = inLinks.next();
					Node j = eji.getSourceNode();
					weightedRankSum += graph.getEdgeWeight(eji) * res.getNodeValue(j) / graph.getWeightedOutDegree((MultiNode) j);
				}
				double newValue = weightedRankSum;
				res.setNodeValue(i, newValue);		
			}
			nc = graph.iterator();
		}
		
		// Distribute score of ground node evenly
		double share = res.getNodeValue(groundNode) / n;
		while(nc.hasNext()) {
			Node node = nc.next();
			res.setNodeValue(node, res.getNodeValue(node) + share);
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
		return CentralityMeasureType.LEADERRANK;
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
