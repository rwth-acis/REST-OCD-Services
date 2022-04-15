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
import y.algo.ShortestPaths;
import org.graphstream.graph.Node;


/**
 * Implementation of Harmonic Centrality.
 * See: Lü, Linyuan and Chen, Duanbing and Ren, Xiao-Long and Zhang, Qian-Ming and Zhang, Yi-Cheng and Zhou, Tao. 2016. Vital nodes identification in complex networks.
 * @author Tobias
 *
 */
public class HarmonicCentrality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {	
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.HARMONIC_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		// If there is only a single node
		if(graph.getNodeCount() == 1) {
			res.setNodeValue(nc.next(), 0);
			return res;
		}
		
		double[] edgeWeights = graph.getEdgeWeights();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			double[] dist = new double[graph.getNodeCount()];
			ShortestPaths.dijkstra(graph, node, true, edgeWeights, dist);
			double inverseDistSum = 0.0;
			for(double d : dist) {
				if(d != 0.0) {
					inverseDistSum += 1.0/d;
				}
			}
			res.setNodeValue(node, 1.0/(graph.getNodeCount()-1.0)*inverseDistSum);
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
		return CentralityMeasureType.HARMONIC_CENTRALITY;
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
