package i5.las2peer.services.ocd.metrics;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.junit.jupiter.api.Disabled;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

public class ExtendedModularityMetricCoMembershipTest {
	
	@Disabled
	@Test
	public void testExtendedModularityOnSawmillSLPA() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException, IllegalArgumentException, ParseException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		OcdAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		ExtendedModularityMetricCoMembership metric = new ExtendedModularityMetricCoMembership();
		double value = metric.measure(cover);
		System.out.println(value);
	}
}
