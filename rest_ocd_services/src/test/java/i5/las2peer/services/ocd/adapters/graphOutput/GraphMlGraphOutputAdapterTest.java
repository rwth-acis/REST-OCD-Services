package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.graphstream.graph.Edge;
import org.junit.jupiter.api.Test;

public class GraphMlGraphOutputAdapterTest {

	@Test
	public void test() throws AdapterException, IOException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		graph.setEdgeWeight(graph.edges().toArray(Edge[]::new)[0], 2);
		GraphOutputAdapter adapter = new GraphMlGraphOutputAdapter();
		adapter.setWriter(new FileWriter(OcdTestConstants.sawmillGraphMlOutputPath));
		adapter.writeGraph(graph);
	}

}