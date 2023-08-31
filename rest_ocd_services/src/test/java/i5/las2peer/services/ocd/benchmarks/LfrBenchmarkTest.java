package i5.las2peer.services.ocd.benchmarks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import org.graphstream.graph.Node;

import java.util.Iterator;


public class LfrBenchmarkTest {
	
	@Test
	public void test() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(12, 0.1, 0.5);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.getNodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testLowerBound() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(24, 0.3, 0);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.getNodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testUpperBound() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(24, 0.3, 1);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.getNodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testMembershipCounts() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(24, 0.3, 0.5);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.getNodeCount());
		Iterator<Node> nodes = graph.iterator();
		Node node;
		int oneMembershipCount = 0;
		int twoMembershipsCount = 0;
		while(nodes.hasNext()) {
			node = nodes.next();
			int memberships = cover.getCommunityIndices(node).size();
			if(memberships == 1) {
				oneMembershipCount++;
			}
			else if(memberships == 2) {
				twoMembershipsCount ++;
			}
		}
		System.out.println("One memberships: " + oneMembershipCount);
		System.out.println("Two memberships: " + twoMembershipsCount);
		assertEquals(500, oneMembershipCount);
		assertEquals(500, twoMembershipsCount);
	}

}
