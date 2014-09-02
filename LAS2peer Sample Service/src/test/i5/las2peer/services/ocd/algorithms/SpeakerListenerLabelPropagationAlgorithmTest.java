package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SpeakerListenerLabelPropagationAlgorithmTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 */
	@Test
	public void testOnTwoCommunities()
	{
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		SpeakerListenerLabelPropagationAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
	
	@Test
	public void testOnSawmill() throws AdapterException, FileNotFoundException
	{
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		SpeakerListenerLabelPropagationAlgorithm algo = new SpeakerListenerLabelPropagationAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}
}
