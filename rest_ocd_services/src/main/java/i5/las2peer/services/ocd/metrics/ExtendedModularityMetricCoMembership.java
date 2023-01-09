package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.*;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;

public class ExtendedModularityMetricCoMembership implements StatisticalMeasure {
	
public ExtendedModularityMetricCoMembership() {
}

@Override
public void setParameters(Map<String, String> parameters) {
}

@Override
public Map<String, String> getParameters() {
	return new HashMap<String, String>();
}

@Override
public Set<GraphType> compatibleGraphTypes() {
	Set<GraphType> compatibleTypes = new HashSet<GraphType>();
	compatibleTypes.add(GraphType.DIRECTED);
	compatibleTypes.add(GraphType.ZERO_WEIGHTS);
	return compatibleTypes;
}
	
	
	@Override
	public double measure(Cover cover) throws InterruptedException {
		double metricValue = 0;
		CustomGraph graph = cover.getGraph();
		Iterator<Node> nodesA = graph.iterator();
		Iterator<Node> nodesB = graph.iterator();
		Node nodeA;
		Node nodeB;
		while(nodesA.hasNext()) {
			nodeA = nodesA.next();
			nodesB = graph.iterator();
			while(nodesB.hasNext()) {
				nodeB = nodesB.next();
				metricValue +=
						getNodePairModularityContribution(cover, nodeA, nodeB);
			}
		}
		if(graph.getEdgeCount() > 0) {
			metricValue /= (graph.getEdgeCount() * 2);
		}
		return metricValue;
	}
	
	private double getNodePairModularityContribution(Cover cover, Node nodeA, Node nodeB){
		double cont = 0;
		double edgeCont = 0;
		double degreeProd;
		double coMembership = 0;
		
		
		for(Integer comId: cover.getCommunityIndices(nodeA)){
			
			if(cover.getCommunityIndices(nodeB).contains(comId)){
				coMembership += (cover.getBelongingFactor(nodeA, comId) * cover.getBelongingFactor(nodeB, comId));
				
			}
		}
		
		double adjacencyEntry = 0;
		if(nodeA.hasEdgeBetween(nodeB)){ //TODO: Check this method regarding same behaviour to with yFiles containsEdge, in theory this one should be more correct
			adjacencyEntry = 1;
		}
		
		degreeProd = nodeA.getInDegree() * nodeB.getInDegree();
		edgeCont = (adjacencyEntry - (degreeProd / (cover.getGraph().getEdgeCount() * 2)));
		cont = edgeCont * coMembership;
		
		
		return cont;
	}
}
