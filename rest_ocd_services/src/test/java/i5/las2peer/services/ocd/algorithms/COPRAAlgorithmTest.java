package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.junit.Test;
import y.base.Node;

import java.io.FileNotFoundException;

public class COPRAAlgorithmTest {
    @Test
    public void name() {

    }

    /*
     * Run the algorithm on a simple graph with 3 communities
     */
    @Test
    public void testOnSimpleGraph() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException{

        // Creates new graph
        CustomGraph graph = new CustomGraph();


//        // Creates nodes
//        int size = 11;
//        Node n[] = new Node[size];
//        for (int i = 0; i < size; i++) {
//            n[i] = graph.createNode();
//        }
//        // first community (nodes: 0, 1, 2, 3, 4)
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 5; j++) {
//                if (i != j ) {
//                    graph.createEdge(n[i], n[j]);
//                }
//            }
//        }
//        // second community (nodes: 5, 6, 7, 8, 9)
//        for(int i = 5; i < 10; i++) {
//            for (int j = 5; j < 10; j++) {
//                if(i!=j ) {
//                    graph.createEdge(n[i], n[j]);
//                }
//            }
//        }
//        /*
//         * Connect above two communities, which creates another small community of size 3 (nodes 0, 5, 10)
//         */
//        graph.createEdge(n[5], n[10]);
//        graph.createEdge(n[10], n[5]);
//        graph.createEdge(n[0], n[10]);
//        graph.createEdge(n[10], n[0]);
//


        // Creates nodes
        int size = 7;
        Node n[] = new Node[size];
        for (int i = 0; i < size; i++) {
            n[i] = graph.createNode();
        }
        // first community (nodes: 0, 1, 2, 3, 4)
        graph.createEdge(n[0],n[1]);
        graph.createEdge(n[1],n[2]);
        graph.createEdge(n[2],n[3]);
        graph.createEdge(n[3],n[0]);
        graph.createEdge(n[1],n[3]);
        // second community (nodes: 5, 6, 7, 8, 9)
        graph.createEdge(n[0],n[4]);
        graph.createEdge(n[4],n[5]);
        graph.createEdge(n[5],n[6]);
        graph.createEdge(n[6],n[0]);
        graph.createEdge(n[4],n[6]);

        // instantiate the algorithm
        //WeakCliquePercolationMethodAlgorithm wcpm = new WeakCliquePercolationMethodAlgorithm();
        CommunityOverlapPropagationAlgorithm copra=new CommunityOverlapPropagationAlgorithm();

        try {
            copra.detectOverlappingCommunities(graph);
        } catch (OcdAlgorithmException | OcdMetricException | InterruptedException e) {
            e.printStackTrace();
        }

        
        
        
    }
}
