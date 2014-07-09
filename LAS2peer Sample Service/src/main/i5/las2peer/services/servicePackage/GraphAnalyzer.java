package i5.las2peer.services.servicePackage;

import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Node;

public class GraphAnalyzer {

	public List<Cover> analyze(List<CustomGraph> graphs, List<OcdAlgorithm> algorithms,
			List<StatisticalMeasure> statisticalMeasures, int componentNodeCountFilter) throws OcdAlgorithmException {
		List<Cover> covers = new ArrayList<Cover>();
		GraphProcessor processor = new GraphProcessor();
		CustomGraph graph;
		Map<CustomGraph, Map<Node, Node>> components;
		Map<Cover, Map<Node, Node>> componentCovers;
		Cover cover;
		for(int i=0; i<graphs.size(); i++) {
			for(int j=0; j<algorithms.size(); j++) {
				graph = graphs.get(i);
				components = processor.divideIntoConnectedComponents(graph);
				componentCovers = calculateComponentCovers(components, algorithms.get(j), componentNodeCountFilter);
				cover = processor.mergeComponentCovers(graph, componentCovers);
				for(int k=0; k<statisticalMeasures.size(); k++) {
					statisticalMeasures.get(k).measure(cover);;
				}
				covers.add(cover);
			}
		}
		return covers;
	}
	
	private Map<Cover, Map<Node, Node>> calculateComponentCovers(Map<CustomGraph, Map<Node, Node>> components,
			OcdAlgorithm algorithm, int componentNodeCountFilter) throws OcdAlgorithmException {
		Map<Cover, Map<Node, Node>> componentCovers = new HashMap<Cover, Map<Node, Node>>();
		CustomGraph component;
		Cover componentCover;
		for(Map.Entry<CustomGraph, Map<Node, Node>> entry : components.entrySet()) {
			component = entry.getKey();
			if(component.nodeCount() < componentNodeCountFilter) {
				componentCover = computeSingleCommunityCover(component, algorithm.getAlgorithm());
			}
			else {
				componentCover = algorithm.detectOverlappingCommunities(component);
			}
			componentCovers.put(componentCover, entry.getValue());
		}
		return componentCovers;
	}
	
	private Cover computeSingleCommunityCover(CustomGraph graph, Algorithm algorithm) {
		Matrix memberships = new CCSMatrix(graph.nodeCount(), 1);
		memberships.assign(1);
		return new Cover(graph, memberships, algorithm);
	}
	
}
