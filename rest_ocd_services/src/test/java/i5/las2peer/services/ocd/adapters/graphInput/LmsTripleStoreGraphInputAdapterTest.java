package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

//TODO: Refactor Triplestore tests
public class LmsTripleStoreGraphInputAdapterTest {
		

	@Test
	public void wholeGraphtest() throws AdapterException {
		LmsTripleStoreGraphInputAdapter inputAdapter = new LmsTripleStoreGraphInputAdapter();
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.getNodeCount() + " " +  graph.getEdgeCount());
		System.out.println(graph.getNodeName(graph.getNode(0)));
	}
	
	@Test
	public void testParams() throws MalformedURLException, IOException, AdapterException, IllegalArgumentException, ParseException {		
		LmsTripleStoreGraphInputAdapter inputAdapter = new LmsTripleStoreGraphInputAdapter();
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("startDate", "2021-03-01");
		params.put("endDate", "2021-03-10");
		params.put("showUserNames", "true");
		inputAdapter.setParameter(params);
		
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.getNodeCount() + " " +  graph.getEdgeCount());
		if(graph.getNodeCount() != 0) {
			System.out.println(graph.getNodeName(graph.getNode(0)));
		}
	}
			
}