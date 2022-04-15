package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Node;

/**
 * Implementation of Closeness Centrality.
 * See: Lü, Linyuan and Chen, Duanbing and Ren, Xiao-Long and Zhang, Qian-Ming and Zhang, Yi-Cheng and Zhou, Tao. 2016. Vital nodes identification in complex networks.
 * @author Tobias
 *
 */
public class ClosenessCentrality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CLOSENESS_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
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
			double[] distArray = new double[graph.getNodeCount()];

			//TODO: Check if dijkstra computation similar enough to old yFiles one
			Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
			dijkstra.init(graph);
			dijkstra.setSource(node);
			dijkstra.compute();

			Iterator<Node> iterator = graph.iterator();
			int k = 0;
			double distSum = 0.0;
			while(iterator.hasNext()) {
				double d = dijkstra.getPathLength(iterator.next());
				distArray[k] = d;
				distSum += d;
				k++;
			}
			//ShortestPaths.dijkstra(graph, node, true, edgeWeights, distArray);
			//for(double d : distArray) {
			//	distSum += d;
			//}

			res.setNodeValue(node, (graph.getNodeCount()-1)/distSum);
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
		return CentralityMeasureType.CLOSENESS_CENTRALITY;
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
