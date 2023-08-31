package i5.las2peer.services.ocd.benchmarks;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import org.graphstream.graph.Node;


public class NewmanBenchmarkTest {

	@Test
	public void testCreateGroundTruthCover() throws InterruptedException {
		for(int i = 0; i<9; i++) {
			NewmanBenchmark model = new NewmanBenchmark(i);
			Cover cover = model.createGroundTruthCover();
			assertNotNull(cover);
			CustomGraph graph = cover.getGraph();
			assertNotNull(graph);
			assertEquals(128, graph.getNodeCount());
			assertEquals(4, cover.communityCount());
			Iterator<Node> nodes = graph.iterator();
			while(nodes.hasNext()) {
				Node node = nodes.next();
				List<Integer> communityIndices = cover.getCommunityIndices(node);
				assertNotNull(communityIndices);
				assertEquals(1, communityIndices.size());
				int communityIndex = communityIndices.get(0);
				assertEquals(1, cover.getBelongingFactor(node, communityIndex), 0);
				int internalEdges = 0;
				int externalEdges = 0;
				Iterator<Node> successors = graph.getSuccessorNeighbours(node).iterator();
				while(successors.hasNext()) {
					Node successor = successors.next();
					assertTrue(successor.hasEdgeToward(node));
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
				}
				assertEquals(i, externalEdges);
				assertEquals(16 - i, internalEdges);
			}
		}
	}

}
