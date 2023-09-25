package i5.las2peer.services.ocd.algorithms;

import java.util.UUID;

import org.graphstream.graph.Node;
import org.junit.Test;
import i5.las2peer.services.ocd.algorithms.CliquePercolationMethodAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.utils.Database;

public class CliquePercolationAlgorithmTest {

    @Test
    public void Test() {

        Database database = new Database(false);
        // Creates new graph
        CustomGraph graph = new CustomGraph();

        // Creates nodes
        int size = 11;
        Node n[] = new Node[size];

        for (int i = 0; i < size; i++) {
            n[i] = graph.addNode(Integer.toString(i));
            Integer name = i + 1;
            graph.setNodeName(n[i], name.toString(size));
        }

        graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
        graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
        graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
        graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);

        graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);

        graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);

        graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
        graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[4], n[7]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[4], n[8]);
        graph.addEdge(UUID.randomUUID().toString(), n[8], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[4], n[9]);
        graph.addEdge(UUID.randomUUID().toString(), n[9], n[4]);

        graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
        graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
        graph.addEdge(UUID.randomUUID().toString(), n[9], n[5]);

        graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[6]);
        graph.addEdge(UUID.randomUUID().toString(), n[6], n[8]);
        graph.addEdge(UUID.randomUUID().toString(), n[8], n[6]);

        graph.addEdge(UUID.randomUUID().toString(), n[8], n[9]);
        graph.addEdge(UUID.randomUUID().toString(), n[9], n[8]);
        
        CliquePercolationMethodAlgorithm cliquePercolationMethodAlgorithm = new CliquePercolationMethodAlgorithm();
        try {
            cliquePercolationMethodAlgorithm.detectOverlappingCommunities(graph);
        } catch (InterruptedException e) {
            System.out.print("Failure while applying CliquePercolationMethod algorithm on graph");
        }
        
        
    }
}
