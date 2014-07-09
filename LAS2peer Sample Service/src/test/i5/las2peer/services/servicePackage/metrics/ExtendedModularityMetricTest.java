package i5.las2peer.services.servicePackage.metrics;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.servicePackage.utils.OcdAlgorithmException;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;

public class ExtendedModularityMetricTest {

	/*
	 * Assures that modularity is 0 if only one community exists.
	 */
	@Test
	public void testExtendedModularityWithOneCommunity() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		Matrix memberships = new Basic2DMatrix(graph.nodeCount(), 1);
		for(int i=0; i<memberships.rows(); i++) {
			memberships.set(i, 0, 1);
		}
		Cover cover = new Cover(graph, memberships, null);
		ExtendedModularityMetric metric = new ExtendedModularityMetric();
		metric.measure(cover);
		System.out.println("1 Community");
		System.out.println(cover.getMetricResult(Metric.EXTENDED_MODULARITY_METRIC));
	}
	
	@Test
	public void testExtendedModularityOnSawmillSLPA() throws OcdAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		OcdAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		ExtendedModularityMetric metric = new ExtendedModularityMetric();
		metric.measure(cover);
		System.out.println("Sawmill SLPA");
		System.out.println(cover.getMetricResult(Metric.EXTENDED_MODULARITY_METRIC));
	}
	
	@Test
	public void testExtendedModularityOnDirectedAperiodicTwoCommunities() throws OcdAlgorithmException {
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
		Cover cover = new Cover(graph, memberships, Algorithm.UNDEFINED);
		ExtendedModularityMetric metric = new ExtendedModularityMetric();
		metric.measure(cover);
		assertEquals(0.581, cover.getMetricResult(Metric.EXTENDED_MODULARITY_METRIC), 0.01);
		System.out.println(cover);
	}

}
