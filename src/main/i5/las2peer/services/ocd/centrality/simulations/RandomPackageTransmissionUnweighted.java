package i5.las2peer.services.ocd.centrality.simulations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

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
		
		Node[] nodes = graph.getNodeArray();
		double[] passageCounter = new double[graph.nodeCount()];
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
					Edge[] incidentEdges = new Edge[currentNode.outDegree()];
					double currentDegree = currentNode.outDegree();
					EdgeCursor ec = currentNode.outEdges();
					int i = 0;
					while(ec.ok()) {
						Edge edge = ec.edge();
						incidentEdges[i] = edge;
						i++;
						ec.next();
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
					packagePosition = nextEdge.target().index();
					passageCounter[nextEdge.target().index()]++;
				}	
				// Change the position of t
				tPos++;
			}
			// Change the position of s
			sPos++;
		}
		// Set centrality value equal to the number of times a package passed the node
		for(int i = 0; i < graph.nodeCount(); i++) {
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
