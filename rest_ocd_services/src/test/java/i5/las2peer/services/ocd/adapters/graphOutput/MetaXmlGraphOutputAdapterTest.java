package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;

public class MetaXmlGraphOutputAdapterTest {

	@Test
	public void test() throws IOException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		GraphOutputAdapter adapter = new MetaXmlGraphOutputAdapter();
		adapter.setWriter(new FileWriter(OcdTestConstants.sawmillMetaXmlOutputPath));
		adapter.writeGraph(graph);
	}

}
