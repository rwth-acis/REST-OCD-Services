package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

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
		MetricLog metric = new MetricLog(MetricIdentifier.EXECUTION_TIME, (double)totalTime / 1000d, new HashMap<String, String>());
		cover.setMetric(metric);
	}
}
