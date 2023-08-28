package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class RequestHandlerTest {

	@Test
	public void testGetGraphMetas() throws AdapterException, ParserConfigurationException, IOException, SAXException, InstantiationException, IllegalAccessException {
		RequestHandler requestHandler = new RequestHandler();
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		List<CustomGraph> graphs = new ArrayList<CustomGraph>();
		graphs.add(graph);
		requestHandler.writeGraphMetas(graphs);
	}

}
