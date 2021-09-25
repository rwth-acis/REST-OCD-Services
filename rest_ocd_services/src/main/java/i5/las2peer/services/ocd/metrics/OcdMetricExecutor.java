package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

/**
 * Manages the execution of an OcdMetric.
 * @author Sebastian
 *
 */
public class OcdMetricExecutor {
	
	/**
	 * Executes a knowledge driven measure on a cover and adds a corresponding metric log to it.
	 * @param cover The cover to calculate the measure for.
	 * @param groundTruth A ground truth cover corresponding to the same graph as the cover being measured.
	 * @param metric The measure to execute.
	 * @return The metric log of the execution.
	 * @throws OcdMetricException In case of a metric failure.
	 * @throws InterruptedException In case of a metric interrupt.
	 */
	public OcdMetricLog executeKnowledgeDrivenMeasure(Cover cover, Cover groundTruth, KnowledgeDrivenMeasure metric) throws OcdMetricException, InterruptedException {
		CustomGraph graph = cover.getGraph();
		CustomGraph graphCopy = new CustomGraph(graph);
		Cover coverCopy = new Cover(graphCopy, cover.getMemberships());
		Cover groundTruthCopy = new Cover(graphCopy, groundTruth.getMemberships());
		double value = metric.measure(coverCopy, groundTruthCopy);
		OcdMetricLog log = new OcdMetricLog(OcdMetricType.lookupType(metric.getClass()), value, metric.getParameters(), cover);
		log.setStatus(ExecutionStatus.COMPLETED);
		cover.addMetric(log);
		return log;
	}
	
	/**
	 * Executes a statistical measure on a cover and adds a corresponding metric log to it.
	 * @param cover The cover to calculate the measure for.
	 * @param metric The measure to execute.
	 * @return The metric log of the execution.
	 * @throws OcdMetricException In case of a metric failure.
	 * @throws InterruptedException In case of a metric interrupt.
	 * @throws OcdAlgorithmException If the execution failed.
	 */
	public OcdMetricLog executeStatisticalMeasure(Cover cover, StatisticalMeasure metric) throws OcdMetricException, InterruptedException, OcdAlgorithmException {
		GraphProcessor processor = new GraphProcessor();
		CustomGraph graph = cover.getGraph();
		CustomGraph graphCopy = new CustomGraph(graph);
		processor.makeCompatible(graphCopy, metric.compatibleGraphTypes());
		Cover coverCopy = new Cover(graphCopy, cover.getMemberships());
		double value = metric.measure(coverCopy);
		OcdMetricLog log = new OcdMetricLog(OcdMetricType.lookupType(metric.getClass()), value, metric.getParameters(), cover);
		log.setStatus(ExecutionStatus.COMPLETED);
		cover.addMetric(log);
		return log;
	}

}
