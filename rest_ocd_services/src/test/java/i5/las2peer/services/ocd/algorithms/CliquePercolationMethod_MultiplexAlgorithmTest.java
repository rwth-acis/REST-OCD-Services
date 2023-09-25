package i5.las2peer.services.ocd.algorithms;

import java.util.UUID;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.Test;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.ThreadHandler;

public class CliquePercolationMethod_MultiplexAlgorithmTest {

    @Test
    public void testOnGraph() {

        Database database = new Database(false);

        // Creates a new graph
        CustomGraph graph = new CustomGraph();

        // Creates nodes
        int size = 10;
        Node n[] = new Node[size];

        // Initialize graph with nodes
        for (int i = 0; i < size; i++) {
            n[i] = graph.addNode(Integer.toString(i));
        }

        // Initialize edges on layer0
        Edge edge = graph.addEdge(UUID.randomUUID().toString(), n[0], n[9]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[9], n[0]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[9], n[8]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[8], n[9]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[8], n[0]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[0], n[8]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[8], n[5]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[8], n[6]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[8]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[3]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[7], n[6]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[7]);
        graph.setEdgeLayerId(edge, "0");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[7], n[3]);
        graph.setEdgeLayerId(edge, "0");
        // Initialize edges on layer 1
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[3]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[2]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[2], n[5]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[6], n[2]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[2], n[6]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[2], n[4]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[4], n[2]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
        graph.setEdgeLayerId(edge, "1");
        edge = graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
        graph.setEdgeLayerId(edge, "1");

        // instantiate the algorithm
        CliquePercolationMethod_MultiplexAlgorithm cpmm = new CliquePercolationMethod_MultiplexAlgorithm();
        Cover cover = cpmm.detectOverlappingCommunities(graph);


    }
}
