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
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
 * Implementation of PageRank.
 * See: Page, Lawrence and Brin, Sergey and Motwani, Rajeev and Winograd, Terry. 1999. The PageRank citation ranking: Bringing order to the web.
 * @author Tobias
 *
 */
public class PageRank implements CentralityAlgorithm {
	private double d = 0.85;
	/*
	 * PARAMETER NAMES
	 */
	protected static final String DAMPING_FACTOR_NAME = "Damping Factor";
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.PAGERANK, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();
		// Set initial PageRank of all nodes to 1
		while(nc.ok()) {
			res.setNodeValue(nc.node(), 1.0);
			nc.next();
		}
		nc.toFirst();
		
		int n = nc.size();
		for(int k = 0; k < 50; k++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			while(nc.ok()) {
				Node i = nc.node();
				double weightedRankSum = 0.0;		
				EdgeCursor inLinks = i.inEdges();
				while(inLinks.ok()) {
					Edge eji = inLinks.edge();
					Node j = eji.source();
					weightedRankSum += graph.getEdgeWeight(eji) * res.getNodeValue(j) / graph.getWeightedOutDegree(j);
					inLinks.next();
				}	
				double newValue = d * weightedRankSum + (1-d) * 1/n;
				res.setNodeValue(i, newValue);	
				nc.next();
			}
			nc.toFirst();
		}
		
		// Scale the values, so they sum to 1
		double sum = 0.0;
		while(nc.ok()) {
			sum += res.getNodeValue(nc.node());
			nc.next();
		}
		nc.toFirst();
		double factor = 1/sum;
		
		while(nc.ok()) {
			res.setNodeValue(nc.node(), factor * res.getNodeValue(nc.node()));
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
		return CentralityMeasureType.PAGERANK;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(DAMPING_FACTOR_NAME, Double.toString(d));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(DAMPING_FACTOR_NAME)) {
			d = Double.parseDouble(parameters.get(DAMPING_FACTOR_NAME));
			parameters.remove(DAMPING_FACTOR_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
}
