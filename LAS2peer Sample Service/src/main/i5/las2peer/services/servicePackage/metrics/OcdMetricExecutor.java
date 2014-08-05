package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;

public class OcdMetricExecutor {
	
	public void executeKnowledgeDrivenMeasure(Cover cover, Cover groundTruth, KnowledgeDrivenMeasure metric) throws MetricException {
		CustomGraph graph = cover.getGraph();
		CustomGraph graphCopy = new CustomGraph(graph);
		Cover coverCopy = new Cover(graphCopy, cover.getMemberships());
		Cover groundTruthCopy = new Cover(graphCopy, groundTruth.getMemberships());
		metric.measure(coverCopy, groundTruthCopy);
		cover.setMetric(coverCopy.getMetrics().get(0));
	}
	
	public void executeStatisticalMeasure(Cover cover, StatisticalMeasure metric) throws MetricException {
		GraphProcessor processor = new GraphProcessor();
		CustomGraph graph = cover.getGraph();
		CustomGraph graphCopy = new CustomGraph(graph);
		processor.makeCompatible(graphCopy, metric.compatibleGraphTypes());
		Cover coverCopy = new Cover(graphCopy, cover.getMemberships());
		metric.measure(coverCopy);
		cover.setMetric(coverCopy.getMetrics().get(0));
	}

}
