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
import org.graphstream.graph.Edge;

import org.graphstream.graph.Node;


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
			nc.next();
		}
		nc.toFirst();
		
		// Add ground node
		Node groundNode = graph.createNode();
		while(nc.hasNext()) {
			Node node = nc.next();
			if(node != groundNode) {
				// Add bidirectional edges
				Edge e1 = graph.createEdge(groundNode, node);
				Edge e2 = graph.createEdge(node, groundNode);		
				graph.setEdgeWeight(e1, 1.0);
				graph.setEdgeWeight(e2, 1.0);
			}
			nc.next();
		}
		nc.toFirst();
		res.setNodeValue(groundNode, 0.0);
		
		for(int k = 0; k < 50; k++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			while(nc.hasNext()) {
				Node i = nc.next();
				double weightedRankSum = 0.0;
				
				EdgeCursor inLinks = i.inEdges();
				while(inLinks.hasNext()) {
					Edge eji = inLinks.edge();
					Node j = eji.source();
					weightedRankSum += graph.getEdgeWeight(eji) * res.getNodeValue(j) / graph.getWeightedOutDegree(j);					
					inLinks.next();
				}		
				double newValue = weightedRankSum;
				res.setNodeValue(i, newValue);		
				nc.next();
			}
			nc.toFirst();
		}
		
		// Distribute score of ground node evenly
		double share = res.getNodeValue(groundNode) / n;
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), res.getNodeValue(nc.next()) + share);
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
