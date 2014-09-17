package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

public class OcdMetricExecutor {
	
	public OcdMetricLog executeKnowledgeDrivenMeasure(Cover cover, Cover groundTruth, KnowledgeDrivenMeasure metric) throws OcdMetricException {
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
	
	public OcdMetricLog executeStatisticalMeasure(Cover cover, StatisticalMeasure metric) throws OcdMetricException {
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
