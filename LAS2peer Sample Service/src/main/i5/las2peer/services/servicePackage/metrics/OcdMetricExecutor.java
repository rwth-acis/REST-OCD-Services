package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

public class OcdMetricExecutor {
	
	public void executeKnowledgeDrivenMeasure(Cover cover, Cover groundTruth, KnowledgeDrivenMeasure metric) {
		metric.measure(cover, groundTruth);
	}
	
	public void executeStatisticalMeasure(Cover cover, StatisticalMeasure metric) {
		metric.measure(cover);
	}

}
