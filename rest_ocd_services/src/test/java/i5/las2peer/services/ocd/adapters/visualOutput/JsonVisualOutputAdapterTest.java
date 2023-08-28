package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.viewer.LayoutHandler;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestConstants;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class JsonVisualOutputAdapterTest {
	
	@Test
	public void testOnTinyCircleGraph() throws IOException, AdapterException, InstantiationException, IllegalAccessException, InterruptedException {
		CustomGraph graph = ViewerTestGraphFactory.getTinyCircleGraph();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(graph, GraphLayoutType.ORGANIC, true, false, 20, 45);
		JsonVisualOutputAdapter adapter = new JsonVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.tinyCircleJsonOutputPath));
		adapter.writeGraph(graph);
	}
	
	@Test
	public void testOnTwoCommunitiesGraph() throws AdapterException, IOException, InstantiationException, IllegalAccessException, InterruptedException {
		CustomGraph graph = ViewerTestGraphFactory.getTwoCommunitiesGraph();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(graph, GraphLayoutType.ORGANIC, true, false, 20, 45);
		JsonVisualOutputAdapter adapter = new JsonVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.twoCommunitiesJsonOutputPath));
		adapter.writeGraph(graph);
	}
	
	@Test
	public void testSvgOnSawmillGraph() throws AdapterException, IOException, InstantiationException, IllegalAccessException, InterruptedException {
		CustomGraph graph = ViewerTestGraphFactory.getSawmillGraph();
		JsonVisualOutputAdapter adapter = new JsonVisualOutputAdapter();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(graph, GraphLayoutType.ORGANIC, true, false, 20, 45);
		adapter.setWriter(new FileWriter(ViewerTestConstants.sawmillJsonOutputPath));
		adapter.writeGraph(graph);
	}
}
