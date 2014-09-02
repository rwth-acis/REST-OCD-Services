package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class MetaXmlGraphOutputAdapterTest {

	@Test
	public void test() throws IOException, AdapterException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		GraphOutputAdapter adapter = new MetaXmlGraphOutputAdapter();
		adapter.setWriter(new FileWriter(OcdTestConstants.sawmillMetaXmlOutputPath));
		adapter.writeGraph(graph);
	}

}
