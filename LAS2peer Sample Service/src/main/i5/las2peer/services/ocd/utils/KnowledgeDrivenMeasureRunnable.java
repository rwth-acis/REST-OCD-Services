package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class KnowledgeDrivenMeasureRunnable implements Runnable {

	private OcdMetricLogId logId;
	private KnowledgeDrivenMeasure metric;
	private Cover cover;
	private Cover groundTruth;
	private ThreadHandler threadHandler;
	
	
	public KnowledgeDrivenMeasureRunnable(OcdMetricLogId logId, KnowledgeDrivenMeasure metric, Cover cover, Cover groundTruth, ThreadHandler threadHandler) {
		this.logId = logId;
		this.metric = metric;
		this.cover = cover;
		this.groundTruth = groundTruth;
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {
		boolean error = false;
		/*
		 * Set metric state to running.
		 */
    	RequestHandler requestHandler = new RequestHandler();
    	EntityManager em = requestHandler.getEntityManager();
    	EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			OcdMetricLog persistedLog = em.find(OcdMetricLog.class, logId);
			if(persistedLog == null) {
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Log deleted while metric running.");
				throw new IllegalStateException();
			}
			persistedLog.setStatus(ExecutionStatus.RUNNING);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			error = true;
		}
		em.close();
		/*
		 * Run metric.
		 */
		OcdMetricLog resultLog = null;
		if(!error) {
	        OcdMetricExecutor executor = new OcdMetricExecutor();
	        try {
	        	resultLog = executor.executeKnowledgeDrivenMeasure(cover, groundTruth, metric);
	        	if(Thread.interrupted()) {
	        		throw new InterruptedException();
	        	}
	        }
	        catch (InterruptedException e) {
	        	return;
	        }
			catch (Exception e) {
				requestHandler.log(Level.SEVERE, "Metric Failure.", e);
				error = true;
			}
		}
    	threadHandler.createMetric(resultLog, logId, error);
	}

}
