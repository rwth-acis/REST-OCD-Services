package i5.las2peer.services.servicePackage.metrics;

import i5.las2peer.services.servicePackage.graph.Cover;

public class ExecutionTime {
	
	private long startTime;
	private long totalTime = 0;
	
	public void start() {
		this.startTime = System.nanoTime();
	}
	
	public void stop() {
		long stopTime = System.nanoTime();
		totalTime += this.startTime - stopTime;
	}
	
	public void setCoverExecutionTime(Cover cover) {
		cover.setMetricValue(Metric.EXECUTION_TIME, this.totalTime);
	}
}
