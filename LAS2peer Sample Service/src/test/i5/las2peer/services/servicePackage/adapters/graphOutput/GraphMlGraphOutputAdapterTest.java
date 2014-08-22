package i5.las2peer.services.servicePackage.adapters.graphOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class GraphMlGraphOutputAdapterTest {

	@Test
	public void test() throws AdapterException, IOException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		graph.setEdgeWeight(graph.getEdgeArray()[0], 2);
		GraphOutputAdapter adapter = new GraphMlGraphOutputAdapter();
		adapter.setWriter(new FileWriter(OcdTestConstants.sawmillGraphMlOutputPath));
		adapter.writeGraph(graph);
	}

}