package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

public class XGMMLGraphInputAdapterTest {
	@Disabled
	@Test
	public void test() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		Map<String, String> adapterParam = new HashMap<String, String>();
		adapterParam.put("indexPath", "C:\\indexes\\fitness_graph_jung");
		adapterParam.put("filePath", "ocd\\test\\input\\fitness_graph_jung.xgmml");
		XGMMLGraphInputAdapter inputAdapter = new XGMMLGraphInputAdapter();
    	inputAdapter.setParameter(adapterParam);
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println(graph.getNodeCount());
	}
}
