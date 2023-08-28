package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class WeightedEdgeListGraphOutputAdapterTest {

	@Test
	public void test() throws IOException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		GraphOutputAdapter adapter = new WeightedEdgeListGraphOutputAdapter(new FileWriter(OcdTestConstants.sawmillWeightedEdgeListOutputPath));
		adapter.writeGraph(graph);
	}

}
