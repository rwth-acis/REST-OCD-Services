package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

@Disabled //TODO: disabled as content-based graphs need a rework
public class ContentBasedWeightingAlgorithmTest {
	

	@Test
	public void testOnJmol() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdAlgorithmException, InterruptedException{
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		ContentBasedWeightingAlgorithm algo = new ContentBasedWeightingAlgorithm();
		graph = algo.detectOverlappingCommunities(graph, new ExecutionTime());
		//System.out.println(graph.toString());
	}

}
