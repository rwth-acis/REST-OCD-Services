package i5.las2peer.services.servicePackage.adapters.graphOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class WeightedEdgeListGraphOutputAdapterTest {

	@Test
	public void test() throws IOException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		GraphOutputAdapter adapter = new WeightedEdgeListGraphOutputAdapter(new FileWriter(OcdTestConstants.sawmillWeightedEdgeListOutputPath));
		adapter.writeGraph(graph);
	}

}
