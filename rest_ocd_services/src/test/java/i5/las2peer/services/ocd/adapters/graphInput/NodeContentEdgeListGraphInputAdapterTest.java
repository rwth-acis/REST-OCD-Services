package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import org.junit.jupiter.api.Test;

public class NodeContentEdgeListGraphInputAdapterTest {

	@Disabled
	@Test
	public void testOnUrch() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		Map<String, String> adapterParam = new HashMap<String, String>();
		adapterParam.put("startDate", "2006-04-01");
		adapterParam.put("endDate", "2006-04-30");
		adapterParam.put("path", "C:\\indexes\\urch2006");
		NodeContentEdgeListGraphInputAdapter inputAdapter =
				new NodeContentEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.urchPostsEdgeListInputPath));
		inputAdapter.setParameter(adapterParam);
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.getNodeCount());
	}
	
	@Disabled
	@Test
	public void testOnOSS() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException{
		Map<String, String> adapterParam = new HashMap<String, String>();
		adapterParam.put("startDate", "2006-04-01");
		adapterParam.put("endDate", "2006-04-30");
		adapterParam.put("path", "C:\\indexes\\jmol2004");
		NodeContentEdgeListGraphInputAdapter inputAdapter =
				new NodeContentEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.jmolEdgeListInputPath));
		inputAdapter.setParameter(adapterParam);
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.getNodeCount());
	}
}
