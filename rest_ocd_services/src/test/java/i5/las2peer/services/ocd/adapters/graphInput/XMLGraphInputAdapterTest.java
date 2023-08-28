package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.junit.jupiter.api.Test;

public class XMLGraphInputAdapterTest {
	@Disabled
	@Test
	public void test() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		Map<String, String> adapterParam = new HashMap<String, String>();
		adapterParam.put("startDate", "2015-04-01");
		adapterParam.put("endDate", "2015-04-30");
		adapterParam.put("indexPath", "C:\\indexes\\stackex2004");
		XMLGraphInputAdapter inputAdapter =
				new XMLGraphInputAdapter();
    	inputAdapter.setParameter(adapterParam);
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.getNodeCount());
	}
}
