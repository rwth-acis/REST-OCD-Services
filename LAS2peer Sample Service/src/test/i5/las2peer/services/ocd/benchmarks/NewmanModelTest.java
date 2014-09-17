package i5.las2peer.services.ocd.benchmarks;

import static org.junit.Assert.*;

import java.util.List;

import i5.las2peer.services.ocd.benchmarks.NewmanBenchmark;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import org.junit.Test;

import y.base.Node;
import y.base.NodeCursor;

public class NewmanModelTest {

	@Test
	public void testCreateGroundTruthCover() throws InterruptedException {
		for(int i = 0; i<9; i++) {
			NewmanBenchmark model = new NewmanBenchmark(i);
			Cover cover = model.createGroundTruthCover();
			assertNotNull(cover);
			CustomGraph graph = cover.getGraph();
			assertNotNull(graph);
			assertEquals(128, graph.nodeCount());
			assertEquals(4, cover.communityCount());
			NodeCursor nodes = graph.nodes();
			while(nodes.ok()) {
				Node node = nodes.node();
				List<Integer> communityIndices = cover.getCommunityIndices(node);
				assertNotNull(communityIndices);
				assertEquals(1, communityIndices.size());
				int communityIndex = communityIndices.get(0);
				assertEquals(1, cover.getBelongingFactor(node, communityIndex), 0);
				int internalEdges = 0;
				int externalEdges = 0;
				NodeCursor successors = node.successors();
				while(successors.ok()) {
					Node successor = successors.node();
					assertTrue(graph.containsEdge(successor, node));
					double successorCommunityMembership = cover.getBelongingFactor(successor, communityIndex);
					if(successorCommunityMembership == 1) {
						internalEdges++;
					}
					else if (successorCommunityMembership == 0) {
						externalEdges ++;
					}
					else {
						fail("Invalid membership degree (neither 0 nor 1)");
					}
					successors.next();
				}
				assertEquals(i, externalEdges);
				assertEquals(16 - i, internalEdges);
				nodes.next();
			}
		}
	}

}
