package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.junit.Test;
import y.base.Node;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertTrue;

public class LabelPropagationAlgorithmWithNeighborNodeInfluenceTest {

    /**
     * Run the algorithm on the example graph from the corresponding paper.
     */

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

    /**
     * Run the algorithm on the example graph with weights.
     */

    @Test
    public void testOnWeightedGraph() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {

        // Instantiate the algorithm
        LabelPropagationAlgorithmWithNeighborNodeInfluence lpanni = new LabelPropagationAlgorithmWithNeighborNodeInfluence();

        // Creates new graph
        CustomGraph graph = new CustomGraph();
        graph.addType(GraphType.WEIGHTED);

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

        graph.setEdgeWeight(node[0].getEdgeFrom(node[1]), 3.0);
        graph.setEdgeWeight(node[0].getEdgeFrom(node[3]), 2.0);
        graph.setEdgeWeight(node[0].getEdgeFrom(node[4]), 2.0);
        graph.setEdgeWeight(node[0].getEdgeFrom(node[5]), 1.0);
        graph.setEdgeWeight(node[0].getEdgeFrom(node[7]), 4.0);
        graph.setEdgeWeight(node[0].getEdgeFrom(node[8]), 5.0);

        graph.setEdgeWeight(node[1].getEdgeFrom(node[0]), 3.0);
        graph.setEdgeWeight(node[3].getEdgeFrom(node[0]), 2.0);
        graph.setEdgeWeight(node[4].getEdgeFrom(node[0]), 2.0);
        graph.setEdgeWeight(node[5].getEdgeFrom(node[0]), 1.0);
        graph.setEdgeWeight(node[7].getEdgeFrom(node[0]), 4.0);
        graph.setEdgeWeight(node[8].getEdgeFrom(node[0]), 5.0);

        graph.setEdgeWeight(node[1].getEdgeFrom(node[2]), 3.0);
        graph.setEdgeWeight(node[1].getEdgeFrom(node[4]), 3.0);
        graph.setEdgeWeight(node[2].getEdgeFrom(node[1]), 3.0);
        graph.setEdgeWeight(node[4].getEdgeFrom(node[1]), 3.0);

        graph.setEdgeWeight(node[2].getEdgeFrom(node[3]), 1.0);
        graph.setEdgeWeight(node[2].getEdgeFrom(node[4]), 4.0);
        graph.setEdgeWeight(node[4].getEdgeFrom(node[2]), 4.0);
        graph.setEdgeWeight(node[3].getEdgeFrom(node[2]), 1.0);

        graph.setEdgeWeight(node[3].getEdgeFrom(node[4]), 2.0);
        graph.setEdgeWeight(node[4].getEdgeFrom(node[3]), 2.0);

        graph.setEdgeWeight(node[5].getEdgeFrom(node[6]), 1.0);
        graph.setEdgeWeight(node[5].getEdgeFrom(node[8]), 3.0);
        graph.setEdgeWeight(node[6].getEdgeFrom(node[5]), 1.0);
        graph.setEdgeWeight(node[8].getEdgeFrom(node[5]), 3.0);

        graph.setEdgeWeight(node[6].getEdgeFrom(node[7]), 4.0);
        graph.setEdgeWeight(node[6].getEdgeFrom(node[8]), 5.0);
        graph.setEdgeWeight(node[7].getEdgeFrom(node[6]), 4.0);
        graph.setEdgeWeight(node[8].getEdgeFrom(node[6]), 5.0);

        graph.setEdgeWeight(node[7].getEdgeFrom(node[8]), 2.0);
        graph.setEdgeWeight(node[8].getEdgeFrom(node[7]), 2.0);

        try {
            Cover result = lpanni.detectOverlappingCommunities(graph);
            assertTrue(result.communityCount() == 2);
            System.out.println(result.toString());
        } catch (OcdAlgorithmException | OcdMetricException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
