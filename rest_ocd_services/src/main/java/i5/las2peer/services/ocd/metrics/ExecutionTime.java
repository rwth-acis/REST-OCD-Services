package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;

/**
 * Measures the execution time of an algorithm.
 * This metric is not instantiatable through the factory since it is calculated automatically at algorithm runtime.
 * Note that the measuring may be paused but can not be reset. For starting a new measuring a new instance is required.
 * @author Sebastian
 *
 */
public class ExecutionTime {
	
	private long startTime;
	private long totalTime = 0;
	
	/**
	 * Starts/resumes the measuring.
	 */
	public void start() {
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * Stops/pauses the measuring. Time until a potential next resume will not be taken into consideration.
	 */
	public void stop() {
		totalTime += System.currentTimeMillis() - this.startTime;
	}
	
	/**
	 * Sets a corresponding execution time log entry for a cover.
	 * @param cover The cover.
	 */
	public void setCoverExecutionTime(Cover cover) {
		OcdMetricLog metric = new OcdMetricLog(OcdMetricType.EXECUTION_TIME, totalTime / 1000d, new HashMap<String, String>(), cover);
		metric.setStatus(ExecutionStatus.COMPLETED);
		cover.addMetric(metric);
	}
}
