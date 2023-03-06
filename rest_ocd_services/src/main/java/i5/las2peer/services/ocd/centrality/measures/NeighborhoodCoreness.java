package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;


/**
 * Implementation of Neighborhood Coreness.
 * See: Bae, Joonhyun and Kim, Sangwook. 2014. Identifying and ranking influential spreaders in complex networks by neighborhood coreness.
 * @author Tobias
 *
 */
public class NeighborhoodCoreness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.NEIGHBORHOOD_CORENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		// Graph is copied because the coreness algorithm removes all the nodes
		CustomGraph graphCopy = new CustomGraph(graph);
		Coreness corenessAlgorithm = new Coreness();
		CentralityMap corenessMap = corenessAlgorithm.getValues(graphCopy);
		Map<String, Double> nameCorenessMap = corenessMap.getMap();
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();	
			double neighborCorenessSum = 0.0;
			Iterator<Node> neighbors = graph.getSuccessorNeighbours(node).iterator();
			while(neighbors.hasNext()) {
				String nodeName = graph.getNodeName(neighbors.next());
				neighborCorenessSum += nameCorenessMap.get(nodeName);
			}
			res.setNodeValue(node, neighborCorenessSum);
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
		return CentralityMeasureType.NEIGHBORHOOD_CORENESS;
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
