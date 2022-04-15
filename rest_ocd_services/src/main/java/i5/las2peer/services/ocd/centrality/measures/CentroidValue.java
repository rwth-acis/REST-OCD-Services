package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.algo.ShortestPaths;
import org.graphstream.graph.Node;
import org.graphstream.algorithm.Dijkstra;


/**
 * Implementation of Centroid Value.
 * See: Scardoni, Giovanni and Laudanna, Carlo and Tosadori, Gabriele and Fabbri, Franco and Faizaan, Mohammed. 2009. CentiScaPe: Network centralities for Cytoscape.
 * @author Tobias
 *
 */
public class CentroidValue implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CENTROID_VALUE, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		int n = graph.getNodeCount();
		double[] edgeWeights = graph.getEdgeWeights();
		Matrix dist = new CCSMatrix(n, n);
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();	
			// Create matrix containing all the distances between nodes
			double[] distArray = new double[n];

			//TODO: Check if dijkstra computation similar enough to old yFiles one
			Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
			dijkstra.init(graph);
			dijkstra.setSource(node);
			dijkstra.compute();

			Iterator<Node> iterator = graph.iterator();
			int k = 0;
			while(iterator.hasNext()) {
				distArray[k] = dijkstra.getPathLength(iterator.next());
				k++;
			}

			Vector distVector = new BasicVector(distArray);
			dist.setRow(node.getIndex(), distVector);
		}

		nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node v = nc.next();			
			int min = n;
			Iterator<Node> nc2 = graph.iterator();
			while(nc2.hasNext()) {
				Node w = nc2.next();
				if(v != w) {
					int gammaVW = 0;
					int gammaWV = 0;			
					for(int i = 0; i < n; i++) {
						double distV = dist.get(v.getIndex(), i);
						double distW = dist.get(w.getIndex(), i);
						if(distV < distW)
							gammaVW++;
						else if(distW < distV)
							gammaWV++;
					}			
					int fVW = gammaVW - gammaWV;
					if(fVW < min)
						min = fVW;
				}			
			}
			res.setNodeValue(v, min);
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
		return CentralityMeasureType.CENTROID_VALUE;
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
