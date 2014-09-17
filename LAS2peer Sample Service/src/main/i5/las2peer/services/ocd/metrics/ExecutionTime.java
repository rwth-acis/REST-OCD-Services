package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;

public class ExecutionTime {
	
	private long startTime;
	private long totalTime = 0;
	
	public void start() {
		this.startTime = System.currentTimeMillis();
	}
	
	public void stop() {
		totalTime += System.currentTimeMillis() - this.startTime;
	}
	
	public void setCoverExecutionTime(Cover cover) {
		OcdMetricLog metric = new OcdMetricLog(OcdMetricType.EXECUTION_TIME, (double)totalTime / 1000d, new HashMap<String, String>(), cover);
		metric.setStatus(ExecutionStatus.COMPLETED);
		cover.addMetric(metric);
	}
}
