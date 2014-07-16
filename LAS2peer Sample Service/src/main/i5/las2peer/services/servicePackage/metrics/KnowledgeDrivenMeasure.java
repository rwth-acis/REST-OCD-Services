package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

public interface KnowledgeDrivenMeasure {

	/**
	 * @param output The cover which is evaluated. It must also contain the corresponding graph.
	 * @param groundTruth The ground truth cover on whose bases the metric value is calculated.
	 * It must contain the same graph as the output cover.
	 */
	public void measure(Cover output, Cover groundTruth);
	
}
