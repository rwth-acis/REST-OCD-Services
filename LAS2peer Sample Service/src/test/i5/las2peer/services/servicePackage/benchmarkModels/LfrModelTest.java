package i5.las2peer.services.servicePackage.benchmarkModels;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import org.junit.Test;

public class LfrModelTest {
	
	@Test
	public void test() throws BenchmarkException {
		LfrModel model = new LfrModel(12, 0.1, 0.5);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testLowerBound() throws BenchmarkException {
		LfrModel model = new LfrModel(24, 0.3, 0);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testUpperBound() throws BenchmarkException {
		LfrModel model = new LfrModel(24, 0.3, 1);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}

}
