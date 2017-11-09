package i5.las2peer.services.ocd.centrality.measures;

import java.util.Arrays;
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
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Node;
import y.base.NodeCursor;

public class BridgingCentrality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.BRIDGING_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		int n = graph.nodeCount();
		
		// Determine bridging coefficient ranks
		BridgingCoefficient bridgingCoefficientAlgorithm = new BridgingCoefficient();
		CentralityMap bridgingCoefficientMap = bridgingCoefficientAlgorithm.getValues(graph);
		
		Double[] bridgingCoefficientValues = bridgingCoefficientMap.getMap().values().toArray(new Double[n]);
		Arrays.sort(bridgingCoefficientValues);
		
		Map<Double, Integer> bridgingCoefficientValueToRankMap = new HashMap<Double, Integer>();
		int rank = 0;
		double previousValue = Double.MAX_VALUE;
		for(int i = 0; i < n; i++) {
			double currentValue = bridgingCoefficientValues[n-1-i];
			if(currentValue < previousValue) {
				rank++;
				bridgingCoefficientValueToRankMap.put(currentValue, rank);
				previousValue = currentValue;
			}
		}
		
		// Determine betweenness ranks
		BetweennessCentrality betweennessCentralityAlgorithm = new BetweennessCentrality();
		GraphProcessor processor = new GraphProcessor();
		processor.invertEdgeWeights(graph);
		CentralityMap betweennessCentralityMap = betweennessCentralityAlgorithm.getValues(graph);
		
		Double[] betweennessCentralityValues = betweennessCentralityMap.getMap().values().toArray(new Double[n]);
		Arrays.sort(betweennessCentralityValues);
		
		Map<Double, Integer> betweennessCentralityValueToRankMap = new HashMap<Double, Integer>();
		rank = 0;
		previousValue = Double.MAX_VALUE;
		for(int i = 0; i < n; i++) {
			double currentValue = betweennessCentralityValues[n-1-i];
			if(currentValue < previousValue) {
				rank++;
				betweennessCentralityValueToRankMap.put(currentValue, rank);
				previousValue = currentValue;
			}
		}
		
		NodeCursor nc = graph.nodes();
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();	
			int bridgingRank = bridgingCoefficientValueToRankMap.get(bridgingCoefficientMap.getNodeValue(node));
			int betweennessRank = betweennessCentralityValueToRankMap.get(betweennessCentralityMap.getNodeValue(node));
			res.setNodeValue(node, bridgingRank * betweennessRank);
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
		return CentralityMeasureType.BRIDGING_CENTRALITY;
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
