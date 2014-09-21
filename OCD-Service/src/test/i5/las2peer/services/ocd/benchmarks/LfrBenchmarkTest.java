package i5.las2peer.services.ocd.benchmarks;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

public class LfrBenchmarkTest {
	
	@Test
	public void test() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(12, 0.1, 0.5);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testLowerBound() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(24, 0.3, 0);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testUpperBound() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(24, 0.3, 1);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		System.out.println(cover.toString());
	}
	
	@Test
	public void testMembershipCounts() throws OcdBenchmarkException, InterruptedException {
		LfrBenchmark model = new LfrBenchmark(24, 0.3, 0.5);
		Cover cover = model.createGroundTruthCover();
		CustomGraph graph = cover.getGraph();
		assertEquals(1000, graph.nodeCount());
		NodeCursor nodes = graph.nodes();
		Node node = nodes.node();
		int oneMembershipCount = 0;
		int twoMembershipsCount = 0;
		while(nodes.ok()) {
			node = nodes.node();
			int memberships = cover.getCommunityIndices(node).size();
			if(memberships == 1) {
				oneMembershipCount++;
			}
			else if(memberships == 2) {
				twoMembershipsCount ++;
			}
			nodes.next();
		}
		System.out.println("One memberships: " + oneMembershipCount);
		System.out.println("Two memberships: " + twoMembershipsCount);
		assertEquals(500, oneMembershipCount);
		assertEquals(500, twoMembershipsCount);
	}

}
