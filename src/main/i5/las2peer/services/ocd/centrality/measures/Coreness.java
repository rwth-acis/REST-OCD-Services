package i5.las2peer.services.ocd.centrality.measures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

public class Coreness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		if(graph.getTypes().contains(GraphType.WEIGHTED)) {
			return getValuesWeighted(graph);
		}
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CORENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();
		// Execute k-core decomposition
		int k = 0;
		while(!graph.isEmpty()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			boolean nodeRemoved = true;		
			while(nodeRemoved == true) {
				nodeRemoved = false;
				nc = graph.nodes();			
				while(nc.ok()) {
					Node node = nc.node();			
					if(node.inDegree() <= k) {
						res.setNodeValue(node, k);
						graph.removeNode(node);
						nodeRemoved = true;
					}
					nc.next();
				}
			}
			k++;
		}
		return res;
	}
	
	private CentralityMap getValuesWeighted(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CORENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc;
		// Execute k-core decomposition
		int k = 1;
		while(!graph.isEmpty()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			double minDegree = graph.getMinWeightedInDegree();	
			
			boolean nodeRemoved = true;
			while(nodeRemoved == true) {
				nodeRemoved = false;
				// Find nodes with minimum degree
				List<Node> nodeRemoveList = new ArrayList<Node>();
				nc = graph.nodes();
				while(nc.ok()) {
					Node node = nc.node();
					if(graph.getWeightedInDegree(node) <= minDegree) {
						nodeRemoveList.add(node);
					}
					nc.next();
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
