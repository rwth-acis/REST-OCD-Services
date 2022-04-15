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


/**
 * Implementation of ClusterRank.
 * See: Chen, Duan-Bing and Gao, Hui and Lü, Linyuan and Zhou, Tao. 2013. Identifying influential nodes in large-scale directed networks: the role of clustering.
 * @author Tobias
 *
 */
public class ClusterRank implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		Iterator<Node> nc = graph.iterator();
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CLUSTER_RANK, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));	
		
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();	
			// Calculate clustering coefficient
			int maxEdges = graph.getSuccessorNeighbours(node).size() * (graph.getSuccessorNeighbours(node).size() - 1);
			int edgeCount = 0;
			double clusteringCoefficient = 0;
			
			if(maxEdges == 0) {
				clusteringCoefficient = 0;
			}				
			else {
				Set<Node> outNeighborSet = new HashSet<Node>();
				Iterator<Node> successors = graph.getSuccessorNeighbours(node).iterator();
				while(successors.hasNext()) {
					outNeighborSet.add(successors.next());
					successors.next();
				}		
				for(Node j : outNeighborSet) {
					Iterator<Edge> edgeIterator = j.leavingEdges().iterator();
					while(edgeIterator.hasNext()) {
						Node k = edgeIterator.next().getTargetNode();
						if(outNeighborSet.contains(k))
							edgeCount++;
					}
				}	
				clusteringCoefficient = (double) edgeCount/maxEdges;
			}
			
			// Calculate sum of neighbors out-degrees (+1)
			int degreeSum = 0;
			for (Node neighbor : graph.getSuccessorNeighbours(node)) {
				degreeSum += neighbor.getOutDegree() + 1;
			}
			res.setNodeValue(node, f(clusteringCoefficient)*degreeSum);
		}
		return res;
	}
	
	/**
	 * Function to describe the (negative) influence of the clustering coefficient on the centrality value.
	 * Chosen according to the paper "Identifying Influential Nodes in Large-Scale Directed Networks: The Role of Clustering"
	 * @param c
	 * @return
	 */
	private double f(double c) {
		return Math.pow(10, -c);
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.CLUSTER_RANK;
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
