package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.MatrixOperations;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Node;
import y.base.NodeCursor;

public class BargainingCentrality implements CentralityAlgorithm {
	private double alpha = 1;
	private double beta = 0.1;
	
	/*
	 * PARAMETER NAMES
	 */
	protected static final String BETA_NAME = "Beta";
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.BARGAINING_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		NodeCursor nc = graph.nodes();
		// If the graph contains no edges
		if(graph.edgeCount() == 0) {
			while(nc.ok()) {
				Node node = nc.node();
				res.setNodeValue(node, 0);
				nc.next();
			}
			return res;
		}
		
		int n = nc.size();
		Matrix R = graph.getNeighbourhoodMatrix();
		Vector c = new BasicVector(n);
		
		for(int k = 0; k < 50; k++) {
			while(nc.ok()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				Node i = nc.node();
				double sum = 0.0;
				NodeCursor neighbors = i.successors();
				while(neighbors.ok()) {
					Node j = neighbors.node();
					double Rij = R.get(i.index(), j.index());
					double cj = c.get(j.index());
					sum += (alpha + beta * cj) * Rij;
					neighbors.next();
				}	
				c.set(i.index(), sum);
				nc.next();
			}
			nc.toFirst();
		}
		
		double norm = MatrixOperations.norm(c);
		c = c.multiply(n/norm);
		
		nc.toFirst();
		while(nc.ok()) {
			Node node = nc.node();
			res.setNodeValue(node, c.get(node.index()));
			nc.next();
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.BARGAINING_CENTRALITY;
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.containsKey(BETA_NAME)) {
			beta = Double.parseDouble(parameters.get(BETA_NAME));
			parameters.remove(BETA_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(BETA_NAME, Double.toString(beta));
		return parameters;
	}
}
