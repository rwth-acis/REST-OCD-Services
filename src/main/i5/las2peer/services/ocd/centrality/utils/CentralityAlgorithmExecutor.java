package i5.las2peer.services.ocd.centrality.utils;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

/**
 * Manages the execution of a CentralityAlgorithm.
 * @author Tobias
 *
 */
public class CentralityAlgorithmExecutor {

	/**
	 * Calculates a CentralityMap by executing a CentralityAlgorithm on a graph.
	 * @param graph The graph.
	 * @param algorithm The algorithm.
	 * @return A CentralityMap of the graph calculated by the algorithm.
	 * @throws CentralityAlgorithmException In case of an algorithm failure.
	 * @throws InterruptedException In case of an algorithm interrupt.
	 */
	public CentralityMap execute(CustomGraph graph, CentralityAlgorithm algorithm) throws CentralityAlgorithmException, InterruptedException {
		CustomGraph graphCopy = new CustomGraph(graph);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graphCopy, algorithm.compatibleGraphTypes());
		if(algorithm.getCentralityMeasureType() == CentralityMeasureType.ECCENTRICITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.CLOSENESS_CENTRALITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.HARMONIC_CENTRALITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.HARMONIC_IN_CLOSENESS || algorithm.getCentralityMeasureType() == CentralityMeasureType.BETWEENNESS_CENTRALITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.STRESS_CENTRALITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.INTEGRATION || algorithm.getCentralityMeasureType() == CentralityMeasureType.RADIALITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.RESIDUAL_ClOSENESS || algorithm.getCentralityMeasureType() == CentralityMeasureType.CENTROID_VALUE) {
			processor.invertEdgeWeights(graphCopy);
		}
		if(algorithm.getCentralityMeasureType() == CentralityMeasureType.HARMONIC_IN_CLOSENESS || algorithm.getCentralityMeasureType() == CentralityMeasureType.RADIALITY) {
			processor.reverseEdgeDirections(graphCopy);
		}
		long startTime = System.currentTimeMillis();
		CentralityMap map = algorithm.getValues(graphCopy);
		map.setCreationMethod(new CentralityCreationLog(algorithm.getCentralityMeasureType(), CentralityCreationType.CENTRALITY_MEASURE, algorithm.getParameters(), algorithm.compatibleGraphTypes()));
		map.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		long executionTime = System.currentTimeMillis() - startTime;
		map.getCreationMethod().setExecutionTime(executionTime);
		return map;
	}
}
