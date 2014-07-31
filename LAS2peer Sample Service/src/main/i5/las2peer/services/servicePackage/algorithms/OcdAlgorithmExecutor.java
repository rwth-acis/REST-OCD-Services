package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.metrics.ExecutionTime;
import i5.las2peer.services.servicePackage.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Node;

public class OcdAlgorithmExecutor {

	/**
	 * Calculates a cover by executing an ocd algorithm on a graph.
	 * The algorithm is run on each weakly connected component seperately.
	 * Small components are automatically considered to be one community.
	 * @param graph The graph.
	 * @param algorithm The algorithm.
	 * @param componentNodeCountFilter Weakly connected components of a size
	 * lower than the filter will automatically be considered a single community.
	 * @return A cover of the graph calculated by the algorithm.
	 * @throws OcdAlgorithmException
	 */
	public Cover execute(CustomGraph graph, OcdAlgorithm algorithm, int componentNodeCountFilter) throws OcdAlgorithmException {
		CustomGraph graphCopy = new CustomGraph(graph);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, algorithm.getAlgorithmLog().getCompatibleGraphTypes());
		List<Pair<CustomGraph, Map<Node, Node>>> components;
		List<Pair<Cover, Map<Node, Node>>> componentCovers;
		components = processor.divideIntoConnectedComponents(graphCopy);
		ExecutionTime executionTime = new ExecutionTime();
		componentCovers = calculateComponentCovers(components, algorithm, componentNodeCountFilter, executionTime);
		Cover cover = processor.mergeComponentCovers(graphCopy, componentCovers);
		executionTime.setCoverExecutionTime(cover);
		return cover;
	}
	
	private List<Pair<Cover, Map<Node, Node>>> calculateComponentCovers(List<Pair<CustomGraph, Map<Node, Node>>> components,
			OcdAlgorithm algorithm, int componentNodeCountFilter, ExecutionTime executionTime) throws OcdAlgorithmException {
		List<Pair<Cover, Map<Node, Node>>> componentCovers = new ArrayList<Pair<Cover, Map<Node, Node>>>();
		CustomGraph component;
		Cover componentCover;
		for(Pair<CustomGraph, Map<Node, Node>> pair : components) {
			component = pair.getFirst();
			if(component.nodeCount() < componentNodeCountFilter) {
				componentCover = computeSingleCommunityCover(component, algorithm.getAlgorithmLog());
			}
			else {
				executionTime.start();
				componentCover = algorithm.detectOverlappingCommunities(component);
				executionTime.stop();
			}
			componentCovers.add(new Pair<Cover, Map<Node, Node>>(componentCover, pair.getSecond()));
		}
		return componentCovers;
	}
	
	private Cover computeSingleCommunityCover(CustomGraph graph, AlgorithmLog algorithm) {
		Matrix memberships = new CCSMatrix(graph.nodeCount(), 1);
		memberships.assign(1);
		return new Cover(graph, memberships, algorithm);
	}
	
}
