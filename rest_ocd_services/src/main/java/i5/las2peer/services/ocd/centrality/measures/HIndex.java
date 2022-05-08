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
import org.graphstream.graph.implementations.MultiNode;


/**
 * Implementation of the H-index.
 * See: Hirsch, J. E. 2005. An index to quantify an individual's scientific research output.
 * Lü, Linyuan and Chen, Duanbing and Ren, Xiao-Long and Zhang, Qian-Ming and Zhang, Yi-Cheng and Zhou, Tao. 2016. Vital nodes identification in complex networks.
 * @author Tobias
 *
 */
public class HIndex implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.H_INDEX, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();		
			int h = 0;
			boolean checkNext = true;
			Iterator<Node> neighbors = graph.getSuccessorNeighbours(node).iterator();
			while(graph.getWeightedNodeDegree((MultiNode) node)/2 >= h && checkNext) {
				checkNext = false;
				neighbors = graph.getSuccessorNeighbours(node).iterator();
				int counter = 0;		
				while(neighbors.hasNext() && counter < h) {
					if(graph.getWeightedNodeDegree((MultiNode) neighbors.next())/2 >= h) {
						counter++;
					}
				}
				if(counter >= h) {
					h++;
					checkNext = true;
				}
			}
			res.setNodeValue(node, h-1);
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
		return CentralityMeasureType.H_INDEX;
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
