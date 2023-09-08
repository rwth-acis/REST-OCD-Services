package i5.las2peer.services.ocd.algorithms.ClusteringUtils;

import java.io.FileNotFoundException;
import java.text.ParseException;

import java.util.HashSet;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import org.graphstream.graph.Node;

public class MaximalCliqueGraphRepresentationTest {
	
	@Test
	public void testOnMaximalCliqueGraph() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph() ;
		MaximalCliqueSearch MCl = new MaximalCliqueSearch();
		HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);
		//System.out.println(maxClq);
	}
	
	@Test
	public void testOnUndirectedUnweigthedGraph() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted() ;
		MaximalCliqueSearch MCl = new MaximalCliqueSearch();
		HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);
		//System.out.println(maxClq);

	}
	
	@Test 
	public void testOnTwoCommunitiesGraph() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		MaximalCliqueSearch MCl = new MaximalCliqueSearch();
		HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);
		//System.out.println(maxClq);

	}
}