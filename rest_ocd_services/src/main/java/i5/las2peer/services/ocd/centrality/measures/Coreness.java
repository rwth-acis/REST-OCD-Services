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
 * Implementation of Coreness.
 * See: Kitsak, Maksim and Gallos, Lazaros K. and Havlin, Shlomo and Liljeros, Fredrik and Muchnik, Lev and Stanley, H. Eugene and Makse, Hernán A. 2010. Identification of influential spreaders in complex networks. 
 * @author Tobias
 *
 */
//TODO: Check if algorithm should not create extra own copy to remove nodes on
public class Coreness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		if(graph.getTypes().contains(GraphType.WEIGHTED)) {
			return getValuesWeighted(graph);
		}
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CORENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		// Execute k-core decomposition
		int k = 0;
		while(graph.getNodeCount() > 0) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			boolean nodeRemoved = true;		
			while(nodeRemoved == true) {
				nodeRemoved = false;

				nc = graph.iterator();
				ArrayList<Node> nodesToRemove = new ArrayList<Node>(); // List for nodes to be removed, cant remove during iteration
				while(nc.hasNext()) {
					Node node = nc.next();			
					if(node.getInDegree() <= k) {
						res.setNodeValue(node, k);
						nodesToRemove.add(node);
						nodeRemoved = true;
					}
				}

				for(Node node : nodesToRemove) { // Remove nodes
					graph.removeNode(node);
				}
			}
			k++;
		}
		return res;
	}
	
	private CentralityMap getValuesWeighted(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CORENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc;
		// Execute k-core decomposition
		int k = 1;
		while(graph.getNodeCount() > 0) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			double minDegree = graph.getMinWeightedInDegree();	
			
			boolean nodeRemoved = true;
			while(nodeRemoved == true) {
				nodeRemoved = false;
				// Find nodes with minimum degree
				List<Node> nodeRemoveList = new ArrayList<Node>();
				nc = graph.iterator();
				while(nc.hasNext()) {
					Node node = nc.next();
					if(graph.getWeightedInDegree(node) <= minDegree) {
						nodeRemoveList.add(node);
					}
				}
				if(!nodeRemoveList.isEmpty()) {
					nodeRemoved = true;
				}
				// Remove nodes
				for(Node node : nodeRemoveList) {
					res.setNodeValue(node, k);
					graph.removeNode(node);
				}
			}
			k++;
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
		return CentralityMeasureType.CORENESS;
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
