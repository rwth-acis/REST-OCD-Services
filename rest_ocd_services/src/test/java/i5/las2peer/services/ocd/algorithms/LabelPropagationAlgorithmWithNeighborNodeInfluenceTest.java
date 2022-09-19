package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import y.base.Node;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class LabelPropagationAlgorithmWithNeighborNodeInfluenceTest {

    @Test
    public void testOnTwoCommunities() throws InterruptedException, OcdAlgorithmException, OcdMetricException {
        CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
        LabelPropagationAlgorithmWithNeighborNodeInfluence lpanni = new LabelPropagationAlgorithmWithNeighborNodeInfluence();
        Cover cover = lpanni.detectOverlappingCommunities(graph);
        System.out.println(cover.toString());
    }

    @Test
    public void testOnExampleGraph() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {

        // Instantiate the algorithm
        LabelPropagationAlgorithmWithNeighborNodeInfluence lpanni = new LabelPropagationAlgorithmWithNeighborNodeInfluence();

        // Creates new graph
        CustomGraph graph = new CustomGraph();

        int nodeCount = 9;
        Node node[] = new Node[nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            node[i] = graph.createNode();
        }

        graph.createEdge(node[0], node[1]);
        graph.createEdge(node[0], node[3]);
        graph.createEdge(node[0], node[4]);
        graph.createEdge(node[0], node[5]);
        graph.createEdge(node[0], node[7]);
        graph.createEdge(node[0], node[8]);
        graph.createEdge(node[1], node[0]);
        graph.createEdge(node[3], node[0]);
        graph.createEdge(node[4], node[0]);
        graph.createEdge(node[5], node[0]);
        graph.createEdge(node[7], node[0]);
        graph.createEdge(node[8], node[0]);

        graph.createEdge(node[1], node[2]);
        graph.createEdge(node[1], node[4]);
        graph.createEdge(node[2], node[1]);
        graph.createEdge(node[4], node[1]);

        graph.createEdge(node[2], node[3]);
        graph.createEdge(node[2], node[4]);
        graph.createEdge(node[3], node[2]);
        graph.createEdge(node[4], node[2]);

        graph.createEdge(node[3], node[4]);
        graph.createEdge(node[4], node[3]);

        graph.createEdge(node[5], node[6]);
        graph.createEdge(node[5], node[8]);
        graph.createEdge(node[6], node[5]);
        graph.createEdge(node[8], node[5]);

        graph.createEdge(node[6], node[7]);
        graph.createEdge(node[6], node[8]);
        graph.createEdge(node[7], node[6]);
        graph.createEdge(node[8], node[6]);

        graph.createEdge(node[7], node[8]);
        graph.createEdge(node[8], node[7]);


        try {
            Cover result = lpanni.detectOverlappingCommunities(graph);
            System.out.println(result.toString());
        } catch (OcdAlgorithmException | OcdMetricException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
