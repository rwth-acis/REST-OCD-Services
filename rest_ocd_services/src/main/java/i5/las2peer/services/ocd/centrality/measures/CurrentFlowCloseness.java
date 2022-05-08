package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import org.la4j.inversion.GaussJordanInverter;
import org.la4j.inversion.MatrixInverter;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Edge;

import org.graphstream.graph.Node;


/**
 * Implementation of Current-flow Closeness.
 * See: Brandes, Ulrik and Fleischer, Daniel. 2005. Centrality Measures Based on Current Flow.
 * @author Tobias
 *
 */
public class CurrentFlowCloseness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.CURRENT_FLOW_CLOSENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();	
			res.setNodeValue(node, 0.0);
		}
		
		// If the graph contains no edges
		if(graph.getEdgeCount() == 0) {
			return res;
		}
		
		int n = graph.getNodeCount();
		Matrix L = new CCSMatrix(n, n);
		
		// Create laplacian matrix
		nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			int i = node.getIndex();
			L.set(i, i, graph.getWeightedInDegree(node));
		}
		Iterator<Edge> ec = graph.edges().iterator();
		while(ec.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Edge edge = ec.next();
			L.set(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex(), -graph.getEdgeWeight(edge));
		}

		// Remove the first row and column
		L = L.slice(1, 1, n, n);
		
		MatrixInverter gauss = new GaussJordanInverter(L);
		Matrix L_inverse = gauss.inverse();
		
		// Create matrix C
		Matrix C = new CCSMatrix(n, n);
		for(int i = 0; i < n-1; i++) {
			for(int j = 0; j < n-1; j++) {
				C.set(i+1, j+1, L_inverse.get(i, j));
			}
		}
		
		nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node v = nc.next();
			Iterator<Node> nc2 = graph.iterator();
			while(nc2.hasNext()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				Node w = nc2.next();
				double increaseW = C.get(v.getIndex(), v.getIndex());
				double increaseV = increaseW - 2 * C.get(w.getIndex(), v.getIndex());
				res.setNodeValue(v, res.getNodeValue(v) + increaseV);
				res.setNodeValue(w, res.getNodeValue(w) + increaseW);
			}
		}
		
		nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			res.setNodeValue(node, (double)1/res.getNodeValue(node));
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
		return CentralityMeasureType.CURRENT_FLOW_CLOSENESS;
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
