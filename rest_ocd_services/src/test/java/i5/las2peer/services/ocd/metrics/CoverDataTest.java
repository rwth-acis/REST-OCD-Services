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

public class CoverDataTest {
	
	@Disabled
	@Test
	public void testOnJmolSLPA() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdAlgorithmException, InterruptedException, OcdMetricException{
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		OcdAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		CoverData cd = new CoverData();
		double valueACS = cd.avgCommunitySize(cover);
		double valueON = cd.noOverlappNodes(cover);
		Integer[] valueDegreeDist = cd.degreeDist(graph);
		Integer[] valueCommSizeDist = cd.communitySizeDist(cover);
		System.out.println(valueACS);
		System.out.println(valueON);
		System.out.println(valueDegreeDist);
		System.out.println(valueCommSizeDist);
	}
	
}
