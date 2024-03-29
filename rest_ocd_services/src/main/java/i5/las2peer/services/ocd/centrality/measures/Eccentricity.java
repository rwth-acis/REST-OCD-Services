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
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;



/**
 * Implementation of Eccentricity.
 * See: Lü, Linyuan and Chen, Duanbing and Ren, Xiao-Long and Zhang, Qian-Ming and Zhang, Yi-Cheng and Zhou, Tao. 2016. Vital nodes identification in complex networks.
 * @author Tobias
 *
 */
public class Eccentricity implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.ECCENTRICITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));

		// Set edge length attribute for the Dijkstra algorithm
		Iterator<Edge> edges = graph.edges().iterator();
		Edge edge;
		HashMap<Edge, Double> edgeweights = new HashMap<Edge, Double>();
		while (edges.hasNext()) {
			edge = edges.next();
			edge.setAttribute("edgeLength", graph.getEdgeWeight(edge));

		}

		Iterator<Node> nc = graph.iterator();
		double[] edgeWeights = graph.getEdgeWeights();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			double[] dist = new double[graph.getNodeCount()];

			// Length is determined by edge weight
			Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "edgeLength");
			dijkstra.init(graph);
			dijkstra.setSource(node);
			dijkstra.compute();

			double max = 0.0;
			Iterator<Node> nc2 = graph.iterator();
			while(nc2.hasNext()){
				double d = dijkstra.getPathLength(nc2.next());
				if(d > max) {
					max = d;
				}
			}

			if(max == 0) {
				res.setNodeValue(node, 0);
			}
			else {
				res.setNodeValue(node, 1/max);
			}
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
		return CentralityMeasureType.ECCENTRICITY;
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
