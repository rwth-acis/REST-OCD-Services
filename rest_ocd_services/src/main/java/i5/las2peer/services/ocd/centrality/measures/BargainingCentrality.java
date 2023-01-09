package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

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
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Implementation of Bargaining Centrality.
 * See: Bonacich, Phillip. 1987. Power and centrality: A family of measures.
 * @author Tobias
 *
 */
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
		
		Node[] nc = graph.nodes().toArray(Node[]::new);
		// If the graph contains no edges
		if(graph.getEdgeCount() == 0) {
			for(Node node : nc) {
				res.setNodeValue(node, 0);
			}
			return res;
		}
		
		int n = nc.length;
		Matrix R = graph.getNeighbourhoodMatrix();
		Vector c = new BasicVector(n);
		
		for(int k = 0; k < 50; k++) {
			for(Node i : nc) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				double sum = 0.0;
				Set<Node> neighbors = graph.getSuccessorNeighbours(i);
				for(Node j : neighbors) {
					double Rij = R.get(i.getIndex(), j.getIndex());
					double cj = c.get(j.getIndex());
					sum += (alpha + beta * cj) * Rij;
				}
				c.set(i.getIndex(), sum);
			}
		}
		
		double norm = MatrixOperations.norm(c);
		c = c.multiply(n/norm);

		for(Node node : nc) {
			res.setNodeValue(node, c.get(node.getIndex()));
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
