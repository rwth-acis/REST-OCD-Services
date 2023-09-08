package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import org.junit.jupiter.api.Test;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SpeakerListenerLabelPropagationAlgorithmTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 * @throws InterruptedException 
	 */
	@Test
	public void testOnTwoCommunities() throws InterruptedException
	{
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		SpeakerListenerLabelPropagationAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmill() throws AdapterException, FileNotFoundException, InterruptedException
	{
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SpeakerListenerLabelPropagationAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}
}
