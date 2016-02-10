package i5.las2peer.services.ocd.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;

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
	
	
	public double measure(Cover cover) throws InterruptedException {
		double metricValue = 0;
		Graph graph = cover.getGraph();
		NodeCursor nodesA = graph.nodes();
		NodeCursor nodesB = graph.nodes();
		Node nodeA;
		Node nodeB;
		while(nodesA.ok()) {
			nodeA = nodesA.node();
			nodesB.toFirst();
			while(nodesB.ok()) {
				nodeB = nodesB.node();
				metricValue +=
						getNodePairModularityContribution(cover, nodeA, nodeB);
				nodesB.next();
			}
 			nodesA.next();
		}
		if(graph.edgeCount() > 0) {
			metricValue /= (graph.edgeCount() * 2);
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
		if(cover.getGraph().containsEdge(nodeA, nodeB)){
			adjacencyEntry = 1;
		}
		
		degreeProd = nodeA.inDegree() * nodeB.inDegree();
		edgeCont = (adjacencyEntry - (degreeProd / (cover.getGraph().edgeCount() * 2)));
		cont = edgeCont * coMembership;
		
		
		return cont;
	}
}
