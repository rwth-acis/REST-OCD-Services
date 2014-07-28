package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.metrics.ExecutionTime;

import java.util.HashMap;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Node;

public class OcdAlgorithmExecutor {

	public Cover execute(CustomGraph graph, OcdAlgorithm algorithm, int componentNodeCountFilter) throws OcdAlgorithmException {
		GraphProcessor processor = new GraphProcessor();
		Map<CustomGraph, Map<Node, Node>> components;
		Map<Cover, Map<Node, Node>> componentCovers;
		components = processor.divideIntoConnectedComponents(graph);
		ExecutionTime executionTime = new ExecutionTime();
		componentCovers = calculateComponentCovers(components, algorithm, componentNodeCountFilter, executionTime);
		Cover cover = processor.mergeComponentCovers(graph, componentCovers);
		executionTime.setCoverExecutionTime(cover);
		return cover;
	}
	
	private Map<Cover, Map<Node, Node>> calculateComponentCovers(Map<CustomGraph, Map<Node, Node>> components,
			OcdAlgorithm algorithm, int componentNodeCountFilter, ExecutionTime executionTime) throws OcdAlgorithmException {
		Map<Cover, Map<Node, Node>> componentCovers = new HashMap<Cover, Map<Node, Node>>();
		CustomGraph component;
		Cover componentCover;
		for(Map.Entry<CustomGraph, Map<Node, Node>> entry : components.entrySet()) {
			component = entry.getKey();
			if(component.nodeCount() < componentNodeCountFilter) {
				componentCover = computeSingleCommunityCover(component, algorithm.getAlgorithm());
			}
			else {
				executionTime.start();
				componentCover = algorithm.detectOverlappingCommunities(component);
				executionTime.stop();
			}
			componentCovers.put(componentCover, entry.getValue());
		}
		return componentCovers;
	}
	
	private Cover computeSingleCommunityCover(CustomGraph graph, AlgorithmLog algorithm) {
		Matrix memberships = new CCSMatrix(graph.nodeCount(), 1);
		memberships.assign(1);
		return new Cover(graph, memberships, algorithm);
	}
	
}
