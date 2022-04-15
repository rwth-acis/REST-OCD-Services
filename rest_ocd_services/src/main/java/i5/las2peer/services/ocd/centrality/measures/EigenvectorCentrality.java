package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.MatrixOperations;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;


/**
 * Implementation of Eigenvector Centrality.
 * See: Bonacich, Phillip. 1972. Factoring and weighting approaches to status scores and clique identification. 
 * @author Tobias
 *
 */
public class EigenvectorCentrality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.EIGENVECTOR_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		// If the graph contains no edges
		if(graph.getEdgeCount() == 0) {
			while(nc.hasNext()) {
				Node node = nc.next();
				res.setNodeValue(node, 0);
				nc.next();
			}
			return res;
		}
		
		Matrix A = graph.getNeighbourhoodMatrix();
		Vector eigenvector = MatrixOperations.calculatePrincipalEigenvector(A);
		
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			res.setNodeValue(node, eigenvector.get(node.getIndex()));
			nc.next();
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.EIGENVECTOR_CENTRALITY;
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
