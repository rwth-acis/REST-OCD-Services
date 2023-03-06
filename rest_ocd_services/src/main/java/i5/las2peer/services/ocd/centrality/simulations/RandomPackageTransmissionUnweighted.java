package i5.las2peer.services.ocd.centrality.simulations;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralitySimulationType;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulation;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * In this simulation each node sends a package to each node in the graph (including itself). 
 * The package travels through the network by randomly traversing edges until it reaches the target node. 
 * The centrality values are determined by counting the number of times a package passes a node.
 * @author Tobias
 *
 */
public class RandomPackageTransmissionUnweighted implements CentralitySimulation {

	@Override
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap map = new CentralityMap(graph);
		map.setCreationMethod(new CentralityCreationLog(CentralitySimulationType.RANDOM_PACKAGE_TRANSMISSION_UNWEIGHTED, CentralityCreationType.SIMULATION, this.getParameters(), this.compatibleGraphTypes()));
		
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		double[] passageCounter = new double[graph.getNodeCount()];
		int sPos = 0;
		int tPos = 0;
		
		while(sPos < nodes.length) {
			tPos = 0;
			while(tPos < nodes.length) {
				// Send package from s to t
				int packagePosition = sPos;
				while(packagePosition != tPos) {
					if(Thread.interrupted()) {
						throw new InterruptedException();
					}
					Node currentNode = nodes[packagePosition];
					// Determine incident edges
					Edge[] incidentEdges = new Edge[currentNode.getOutDegree()];
					double currentDegree = currentNode.getOutDegree();
					Iterator<Edge> ec = currentNode.leavingEdges().iterator();
					int i = 0;
					while(ec.hasNext()) {
						Edge edge = ec.next();
						incidentEdges[i] = edge;
						i++;
					}
					// Choose one of the edges at random
					int randomEdgeIndex = -1;
					double random = Math.random() * currentDegree;
					for(int j = 0; j < incidentEdges.length; j++) {
						random--;
						if(random <= 0) {
							randomEdgeIndex = j;
							break;
						}
					}
					Edge nextEdge = incidentEdges[randomEdgeIndex];
					packagePosition = nextEdge.getTargetNode().getIndex();
					passageCounter[nextEdge.getTargetNode().getIndex()]++;
				}	
				// Change the position of t
				tPos++;
			}
			// Change the position of s
			sPos++;
		}
		// Set centrality value equal to the number of times a package passed the node
		for(int i = 0; i < graph.getNodeCount(); i++) {
			map.setNodeValue(nodes[i], passageCounter[i]);
		}
		return map;
	}

	@Override
	public CentralitySimulationType getSimulationType() {
		return CentralitySimulationType.RANDOM_PACKAGE_TRANSMISSION_UNWEIGHTED;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		return compatibleTypes;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}

}
