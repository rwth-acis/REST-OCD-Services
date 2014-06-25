package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

/**
 * A common interface for statistical overlapping community detection measures.
 * @author Sebastian
 *
 */
public interface StatisticalMeasure {

	/**
	 * Calculates the metric value of a cover.
	 * @param cover The cover which is evaluated. It must also contain the corresponding graph.
	 * @return The metric value.
	 */
	public double getValue(Cover cover);
	
}
