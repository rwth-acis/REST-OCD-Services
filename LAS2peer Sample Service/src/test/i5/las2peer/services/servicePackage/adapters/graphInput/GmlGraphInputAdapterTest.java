package i5.las2peer.services.servicePackage.adapters.graphInput;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

public class GmlGraphInputAdapterTest {

	@Test
	public void test() throws FileNotFoundException, AdapterException {
		GraphInputAdapter inputAdapter = new GmlGraphInputAdapter();
		inputAdapter.setReader(new FileReader(OcdTestConstants.dolphinsGmlInputPath));
		CustomGraph graph = inputAdapter.readGraph();
		System.out.println("Nodes: " + graph.nodeCount());
		System.out.println("Edges: " + graph.edgeCount());
		assertEquals(62, graph.nodeCount());
		assertEquals(159, graph.edgeCount());
	}

}
