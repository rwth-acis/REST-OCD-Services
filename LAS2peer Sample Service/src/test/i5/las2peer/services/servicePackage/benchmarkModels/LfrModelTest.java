package i5.las2peer.services.servicePackage.benchmarkModels;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import org.junit.Test;

public class LfrModelTest {

	@Test
	public void test() throws BenchmarkException {
		LfrModel model = new LfrModel();
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}

}
