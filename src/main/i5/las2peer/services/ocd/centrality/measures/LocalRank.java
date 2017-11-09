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

public class LocalRank implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.LOCAL_RANK, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		int localrank = 0;
		Set<Integer> oneOrTwoStepNeighbors = new HashSet<Integer>();
		NodeCursor nc = graph.nodes();
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();
			localrank = 0;	
			NodeCursor c1 = node.successors();
			while(c1.ok()) {
				Node n1 = c1.node();
				NodeCursor c2 = n1.successors();	
				while(c2.ok()) {
					Node n2 = c2.node();
					oneOrTwoStepNeighbors = new HashSet<Integer>();
					NodeCursor c3 = n2.successors();
					while(c3.ok()) {
						Node n3 = c3.node();
						oneOrTwoStepNeighbors.add(n3.index());
						NodeCursor c4 = n3.successors();
						while(c4.ok()) {
							Node n4 = c4.node();
							oneOrTwoStepNeighbors.add(n4.index());
							c4.next();
						}
						c3.next();
					}
					oneOrTwoStepNeighbors.remove(n2.index());
					localrank += oneOrTwoStepNeighbors.size();
					c2.next();
				}
				c1.next();
			}
			res.setNodeValue(node, localrank);
			nc.next();
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.LOCAL_RANK;
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
