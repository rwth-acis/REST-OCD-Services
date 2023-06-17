package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.base.Sys;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.graph.Node;

/**
 * Manages the execution of an OcdAlgorithm.
 * @author Sebastian
 *
 */
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
	 * @throws OcdAlgorithmException In case of an algorithm failure.
	 * @throws InterruptedException In case of an algorithm interrupt.
	 * @throws OcdMetricException if the metric execution failed
	 */
	public Cover execute(CustomGraph graph, OcdAlgorithm algorithm, int componentNodeCountFilter) throws OcdAlgorithmException, InterruptedException, OcdMetricException, IOException {
		CustomGraph graphCopy = new CustomGraph(graph);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graphCopy, algorithm.compatibleGraphTypes());
		if(algorithm.getAlgorithmType().toString().equalsIgnoreCase(CoverCreationType.SIGNED_PROBABILISTIC_MIXTURE_ALGORITHM.toString()) || algorithm.getAlgorithmType().toString().equalsIgnoreCase(CoverCreationType.WORD_CLUSTERING_REF_ALGORITHM.toString()) || algorithm.getAlgorithmType().toString().equalsIgnoreCase(CoverCreationType.COST_FUNC_OPT_CLUSTERING_ALGORITHM.toString()) || algorithm.getAlgorithmType().toString().equalsIgnoreCase(CoverCreationType.LOCAL_SPECTRAL_CLUSTERING_ALGORITHM.toString()) || algorithm.getAlgorithmType().toString().equalsIgnoreCase(CoverCreationType.LOUVAIN_ALGORITHM.toString())){
			ExecutionTime executionTime = new ExecutionTime();
			//TODO: I think it should be detectOverlappingCommunities(graphCopy) - Tobias
			Cover cover = algorithm.detectOverlappingCommunities(graph);
			cover.setCreationMethod(new CoverCreationLog(algorithm.getAlgorithmType(), algorithm.getParameters(), algorithm.compatibleGraphTypes()));
			cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
			executionTime.setCoverExecutionTime(cover);
			return cover;
		}else{
			List<Pair<CustomGraph, Map<Node, Node>>> components;
			List<Pair<Cover, Map<Node, Node>>> componentCovers;
			components = processor.divideIntoConnectedComponents(graphCopy);
			ExecutionTime executionTime = new ExecutionTime();
			componentCovers = calculateComponentCovers(components, algorithm, componentNodeCountFilter, executionTime);
			Cover coverCopy = processor.mergeComponentCovers(graphCopy, componentCovers);
			if(DescriptiveVisualization.getVisualize()){
				DescriptiveVisualization.saveJson();
			}
			Cover cover = new Cover(graph, coverCopy.getMemberships());
			cover.setCreationMethod(coverCopy.getCreationMethod());
			cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
			executionTime.setCoverExecutionTime(cover);
			return cover;
		}
	}
	
	/**
	 * Calculates the cover of each connected component.
	 * @param components The connected components each with a node mapping from the component nodes to the original graph nodes.
	 * @param algorithm The algorithm to calculate the covers with.
	 * @param componentNodeCountFilter Components of a size lower than the filter will automatically be considered a single community.
	 * @param executionTime The execution time metric corresponding the algorithm execution.
	 * @return The covers of the connected components each with a node mapping from the component nodes to the original graph nodes.
	 * @throws OcdAlgorithmException In case of an algorithm failure.
	 * @throws InterruptedException In case of an algorithm interrupt.
	 * @throws OcdMetricException 
	 */
	private List<Pair<Cover, Map<Node, Node>>> calculateComponentCovers(List<Pair<CustomGraph, Map<Node, Node>>> components,
			OcdAlgorithm algorithm, int componentNodeCountFilter, ExecutionTime executionTime) throws OcdAlgorithmException, InterruptedException, OcdMetricException, IOException {
		List<Pair<Cover, Map<Node, Node>>> componentCovers = new ArrayList<Pair<Cover, Map<Node, Node>>>();
		CustomGraph component;
		Cover componentCover;
		for(Pair<CustomGraph, Map<Node, Node>> pair : components) {
			component = pair.getFirst();
			if(component.getNodeCount() < componentNodeCountFilter) {
				componentCover = computeSingleCommunityCover(component, algorithm);
			}
			else {
				executionTime.start();
				componentCover = algorithm.detectOverlappingCommunities(component);
				componentCover.setCreationMethod(new CoverCreationLog(algorithm.getAlgorithmType(), algorithm.getParameters(), algorithm.compatibleGraphTypes()));
				componentCover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
				executionTime.stop();
			}
			componentCovers.add(new Pair<Cover, Map<Node, Node>>(componentCover, pair.getSecond()));
		}
		return componentCovers;
	}
	
	/**
	 * Calculates a cover consisting of a single community.
	 * @param graph The graph to create the cover for.
	 * @param algorithm The algorithm used for setting the log entry.
	 * @return The cover.
	 */
	private Cover computeSingleCommunityCover(CustomGraph graph, OcdAlgorithm algorithm) {
		Matrix memberships = new CCSMatrix(graph.getNodeCount(), 1);
		memberships.assign(1);
		Cover cover = new Cover (graph, memberships);
		cover.setCreationMethod(new CoverCreationLog(algorithm.getAlgorithmType(), algorithm.getParameters(), algorithm.compatibleGraphTypes()));
		cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		return cover;
	}
	
}
