package i5.las2peer.services.ocd.metrics;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.ExtendedModularity;
import i5.las2peer.services.ocd.metrics.MetricException;
import i5.las2peer.services.ocd.metrics.MetricType;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

public class OcdMetricExecutorTest {

	@Test
	public void testExtendedModularityOnDirectedAperiodicTwoCommunities() throws OcdAlgorithmException, MetricException {
		CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		Matrix memberships = new CCSMatrix(graph.nodeCount(), 3);
		memberships.set(0, 0, 1);
		memberships.set(1, 0, 0.7);
		memberships.set(1, 1, 0.3);
		memberships.set(2, 0, 1);
		memberships.set(3, 0, 1);
		memberships.set(4, 0, 0.8);
		memberships.set(4, 1, 0.2);
		memberships.set(5, 1, 0.4);
		memberships.set(5, 2, 0.6);
		memberships.set(6, 2, 1);
		memberships.set(7, 2, 1);
		memberships.set(8, 2, 1);
		memberships.set(9, 1, 0.1);
		memberships.set(9, 2, 0.9);
		memberships.set(10, 0, 0.4);
		memberships.set(10, 1, 0.4);
		memberships.set(10, 2, 0.2);
		Cover cover = new Cover(graph, memberships);
		ExtendedModularity metric = new ExtendedModularity();
		OcdMetricExecutor executor = new OcdMetricExecutor();
		executor.executeStatisticalMeasure(cover, metric);
		assertEquals(0.581, cover.getMetric(MetricType.EXTENDED_MODULARITY).getValue(), 0.01);
		System.out.println(cover);
	}

}
