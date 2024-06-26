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

		// Set edge length attribute for the Dijkstra algorithm
		Iterator<Edge> edges = graph.edges().iterator();
		Edge edge;

		while (edges.hasNext()) {
			edge = edges.next();
			edge.setAttribute("edgeLength", graph.getEdgeWeight(edge));
		}

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

			double inverseDistSum = 0.0;

			Iterator<Node> nc2 = graph.iterator();
			while(nc2.hasNext()){
				double d = dijkstra.getPathLength(nc2.next());
				if(d != 0.0) {
					inverseDistSum += 1.0/d;
				}
			}
			res.setNodeValue(node, 1.0/(graph.getNodeCount()-1.0)*inverseDistSum);
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
