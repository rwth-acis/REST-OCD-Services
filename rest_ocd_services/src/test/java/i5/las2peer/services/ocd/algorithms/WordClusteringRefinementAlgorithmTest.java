package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled //TODO: disabled as content-based graphs need a rework
public class WordClusteringRefinementAlgorithmTest {

	@Test
	public void testOnJmol() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdAlgorithmException, InterruptedException{
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		WordClusteringRefinementAlgorithm algo = new WordClusteringRefinementAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(WordClusteringRefinementAlgorithm.OVERLAPP_COEF_NAME, Double.toString(0.4));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		////System.out.println(cover.toString());
	}

	@Test
	public void testSVDversion()throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdAlgorithmException, InterruptedException{
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		WordClusteringRefinementAlgorithm algo = new WordClusteringRefinementAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(WordClusteringRefinementAlgorithm.OVERLAPP_COEF_NAME, Double.toString(0.3));
		parameters.put(WordClusteringRefinementAlgorithm.SVD_NAME, Boolean.toString(true));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		////System.out.println(cover.toString());
	}
}
