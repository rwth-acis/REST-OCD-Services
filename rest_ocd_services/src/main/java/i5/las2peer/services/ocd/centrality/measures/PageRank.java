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
		
		Iterator<Node> nc = graph.iterator();
		// Set initial PageRank of all nodes to 1
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), 1.0);
		}
		nc = graph.iterator();
		
		int n = graph.getNodeCount();
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
				double newValue = d * weightedRankSum + (1-d) * 1/n;
				res.setNodeValue(i, newValue);	
			}
			nc = graph.iterator();
		}
		
		// Scale the values, so they sum to 1
		double sum = 0.0;
		while(nc.hasNext()) {
			sum += res.getNodeValue(nc.next());
		}
		nc = graph.iterator();
		double factor = 1/sum;
		
		while(nc.hasNext()) {
			Node node = nc.next();
			res.setNodeValue(node, factor * res.getNodeValue(node));
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
