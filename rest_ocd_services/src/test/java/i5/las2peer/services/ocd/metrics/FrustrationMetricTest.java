package i5.las2peer.services.ocd.metrics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.testsUtils.OcdTestCoverFactory;


public class FrustrationMetricTest {

	@Test
	/*
	 * test on a synthetic network where intra-community edges are all positive
	 * and inter-community one all negative,i.e.frustration should be equal to
	 * 0.
	 */
	public void testOnSignedLfr() throws Exception {
		FrustrationMetric metric = new FrustrationMetric();
		Cover cover = OcdTestCoverFactory.getSignedLfrCover();
		double value = metric.measure(cover);
		System.out.println("Frustration value for unblurred network: " + value);
		assertEquals(0.0, value, 0.001);

	}

	@Test
	/*
	 * test on a synthetic network where intra-community edges are all positive
	 * except one edge and inter-community one all negative except one edge,
	 * i.e.frustration should be equal to 1, of the weighting parameter is set
	 * to 0.5
	 */
	public void testOnSignedLfrBlurred() throws Exception {
		FrustrationMetric metric = new FrustrationMetric();
		Cover cover = OcdTestCoverFactory.getSignedLfrBlurredCover();
		double value = metric.measure(cover);
		System.out.println("Frustration value for blurred network: " + value);
		assertEquals(0.028571429, value, 0.001);
	}
}
