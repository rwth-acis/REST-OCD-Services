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


/**
 * Implementation of Laplacian Centrality.
 * See: Qi, Xingqin and Fuller, Eddie and Wu, Qin and Wu, Yezhou and Zhang, Cun-Quan. 2012. Laplacian centrality: A new centrality measure for weighted networks.
 * @author Tobias
 *
 */
public class LaplacianCentrality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.LAPLACIAN_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			// Count closed 2-walks that contain the node
			double nwc = 0;
			Set<Node> neighbors = graph.getNeighbours(node);
			for(Node neighbor : neighbors) {
				double edgeWeight = graph.getEdgeWeight(node.getEdgeToward(neighbor));
				nwc += edgeWeight * edgeWeight;
			}
			// Count 2-walks with node as an end point
			double nwe = 0;
			for(Node neighbor : neighbors) {
				double edgeWeight1 = graph.getEdgeWeight(node.getEdgeToward(neighbor));
				Set<Node> twoStepNeighbors = graph.getNeighbours(neighbor);
				twoStepNeighbors.remove(node);
				for(Node twoStepNeighbor : twoStepNeighbors) {
					double edgeWeight2 = graph.getEdgeWeight(neighbor.getEdgeToward(twoStepNeighbor));
					nwe += edgeWeight1 * edgeWeight2;
				}
			}
			// Count 2-walks with node in the middle
			double nwm = 0;
			for(Node neighbor : neighbors) {
				double edgeWeight1 = graph.getEdgeWeight(neighbor.getEdgeToward(node));
				for(Node neighbor2 : neighbors) {
					if(neighbor != neighbor2) {
						double edgeWeight2 = graph.getEdgeWeight(node.getEdgeToward(neighbor2));
						nwm += edgeWeight1 * edgeWeight2;
					}
				}
			}
			res.setNodeValue(node, 4 * nwc + 2 * nwe + 2 * nwm);			
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
		return CentralityMeasureType.LAPLACIAN_CENTRALITY;
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
