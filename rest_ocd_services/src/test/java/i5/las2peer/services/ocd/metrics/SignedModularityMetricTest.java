package i5.las2peer.services.ocd.metrics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.testsUtils.OcdTestCoverFactory;


public class SignedModularityMetricTest {

	@Test
	public void testOnSignedLfrWithTwelveNodes() throws Exception {
		SignedModularityMetric metric = new SignedModularityMetric();
		Cover cover = OcdTestCoverFactory.getSignedLfrCover();
		double value = metric.measure(cover);
		System.out.println("Modularity-Network with 12 nodes: " + value);
		assertEquals(0.26221, value, 0.001);
	}

	@Test
	public void testOnSignedLfrWithSixNodes() throws Exception {
		SignedModularityMetric metric = new SignedModularityMetric();
		Cover cover = OcdTestCoverFactory.getSignedLfrSixNodesCover();
		double value = metric.measure(cover);
		System.out.println("Modularity-Network with 6 nodes: " + value);
		assertEquals(0.1375, value, 0.001);
	}

}
