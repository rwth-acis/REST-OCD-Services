package i5.las2peer.services.ocd.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Similarities;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Node;
import y.base.NodeCursor;

/**
 * Implements the newman modularity combined with the cosine similarity of each of the nodes. 
 * The influence of the similarity can be maintained by the weighting factor.  
 * The bigger the factor is chosen, the less is the influence of the similarity.
 * If the weighting factor is equal to 1, the algorithm will behave like the normal newman modularity.
 * @author Sabrina
 *
 */

public class NewmanModularityCombined implements StatisticalMeasure {
	
	
	private double alpha = 0.5;
	
	public static final String ALPHA_NAME = "weightingFactor"; 
	
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.containsKey(ALPHA_NAME)){
			alpha = Double.parseDouble(parameters.get(ALPHA_NAME));
			if(alpha < 0 || alpha > 1){
				throw new IllegalArgumentException();
			}
			parameters.remove(ALPHA_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> param =  new HashMap<String, String>();
		param.put(ALPHA_NAME, Double.toString(alpha));
		return param;
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.ZERO_WEIGHTS);
		compatibleTypes.add(GraphType.CONTENT_LINKED);
		return compatibleTypes;
	}
	
	@Override
	public double measure(Cover cover) throws InterruptedException, OcdAlgorithmException {
		double metricValue = 0;
		Similarities sim = new Similarities();
		CustomGraph graph = cover.getGraph();
		Termmatrix t = new Termmatrix(graph);
		NodeCursor nodesA = graph.nodes();
		NodeCursor nodesB = graph.nodes();
		Node nodeA;
		Node nodeB;
		int indexA;
		int indexB;
		double iteration = 0;
		while(nodesA.ok()) {
			nodeA = nodesA.node();
			nodesB.toFirst();
			indexA = t.getNodeIdList().indexOf(nodeA);
			while(nodesB.ok()) {
				nodeB = nodesB.node();
				indexB = t.getNodeIdList().indexOf(nodeB);
				if(indexB > indexA){
					int coMembership = 0;
					for(Integer comId: cover.getCommunityIndices(nodeA)){
						
						if(cover.getCommunityIndices(nodeB).contains(comId)){
							//coMembership += (cover.getBelongingFactor(nodeA, comId) * cover.getBelongingFactor(nodeB, comId));
							coMembership = 1;
							break;
						}
					}
					iteration++;
					metricValue +=
							(alpha * linkStrength(cover, nodeA, nodeB) + ( 1- alpha) * sim.cosineSim((ArrayRealVector)t.getMatrix().getRowVector(indexA), (ArrayRealVector)t.getMatrix().getRowVector(indexB))) * coMembership;
				}
				nodesB.next();
			}
 			nodesA.next();
		}
		
		return metricValue/iteration;
	}
	
	private double linkStrength(Cover cover, Node nodeA, Node nodeB){
		double cont = 0;
		double edgeCont = 0;
		double degreeProd;
		//double coMembership = 0;
		
		
		
		
		double adjacencyEntry = 0;
		if(cover.getGraph().containsEdge(nodeA, nodeB)){
			adjacencyEntry = 1;
		}
		
		if(cover.getGraph().edgeCount() != 0){
		degreeProd = nodeA.inDegree() * nodeB.inDegree();
		edgeCont = (adjacencyEntry - (degreeProd / (cover.getGraph().edgeCount() * 2)));
		}
		
		cont = edgeCont /* * coMembership*/;
		
		if(cover.getGraph().edgeCount() != 0){
			cont /= (cover.getGraph().edgeCount() * 2);
		}
		
		return cont;
	}

}
