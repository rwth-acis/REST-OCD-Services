package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import y.base.Node;
import y.base.NodeCursor;

public class CentroidValue implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CENTROID_VALUE, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();
		int n = graph.nodeCount();
		double[] edgeWeights = graph.getEdgeWeights();
		Matrix dist = new CCSMatrix(n, n);
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();	
			// Create matrix containing all the distances between nodes
			double[] distArray = new double[n];
			ShortestPaths.dijkstra(graph, node, true, edgeWeights, distArray);
			Vector distVector = new BasicVector(distArray);
			dist.setRow(node.index(), distVector);
			nc.next();
		}
		
		nc.toFirst();
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node v = nc.node();			
			int min = n;
			NodeCursor nc2 = graph.nodes();
			while(nc2.ok()) {
				Node w = nc2.node();
				if(v != w) {
					int gammaVW = 0;
					int gammaWV = 0;			
					for(int i = 0; i < n; i++) {
						double distV = dist.get(v.index(), i);
						double distW = dist.get(w.index(), i);
						if(distV < distW)
							gammaVW++;
						else if(distW < distV)
							gammaWV++;
					}			
					int fVW = gammaVW - gammaWV;
					if(fVW < min)
						min = fVW;
				}			
				nc2.next();
			}		
			res.setNodeValue(v, min);
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
