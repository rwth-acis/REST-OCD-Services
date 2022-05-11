package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.glassfish.jersey.internal.inject.Custom;
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
        CustomGraph dynamicGraph= new CustomGraph();
        dynamicGraph.addType(GraphType.DYNAMIC);

        CustomGraph graph1 = new CustomGraph();
        // Creates nodes
        int size = 11;
        Node n[] = new Node[size];
        for (int i = 0; i < size; i++) {
            n[i] = graph1.createNode();
        }
        // first community (nodes: 0, 1, 2, 3, 4)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i != j ) {
                    graph1.createEdge(n[i], n[j]);
                }
            }
        }
        // second community (nodes: 5, 6, 7, 8, 9)
        for(int i = 5; i < 10; i++) {
            for (int j = 5; j < 10; j++) {
                if(i!=j ) {
                    graph1.createEdge(n[i], n[j]);
                }
            }
        }
        //Connect above two communities, which creates another small community of size 3 (nodes 0, 5, 10)
        graph1.createEdge(n[5], n[10]);
        graph1.createEdge(n[10], n[5]);
        graph1.createEdge(n[0], n[10]);
        graph1.createEdge(n[10], n[0]);
        graph1.createEdge(n[0], n[5]);
        graph1.createEdge(n[5], n[0]);






        CustomGraph graph2 = new CustomGraph();
        // Creates nodes
        int size2 = 12;
        Node n2[] = new Node[size2];
        for (int i = 0; i < size2; i++) {
            n2[i] = graph2.createNode();
        }
        // first community (nodes: 0, 1, 2, 3, 4)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i != j ) {
                    graph2.createEdge(n2[i], n2[j]);
                }
            }
        }
        // second community (nodes: 5, 6, 7, 8, 9)
        for(int i = 5; i < 10; i++) {
            for (int j = 5; j < 10; j++) {
                if(i!=j ) {
                    graph2.createEdge(n2[i], n2[j]);
                }
            }
        }
        //Connect above two communities, which creates another small community of size 3 (nodes 0, 5, 10)
        graph2.createEdge(n2[5], n2[10]);
        graph2.createEdge(n2[10], n2[5]);
        graph2.createEdge(n2[0], n2[10]);
        graph2.createEdge(n2[10], n2[0]);
        graph2.createEdge(n2[0], n2[5]);
        graph2.createEdge(n2[5], n2[0]);

        graph2.createEdge(n2[11], n2[10]);
        graph2.createEdge(n2[10], n2[11]);
        graph2.createEdge(n2[0], n2[11]);
        graph2.createEdge(n2[11], n2[0]);
        graph2.createEdge(n2[11], n2[5]);
        graph2.createEdge(n2[5], n2[11]);


        dynamicGraph.addGraphIntoGraphSeries(graph1);
        dynamicGraph.addGraphIntoGraphSeries(graph2);

        // instantiate the algorithm

        CommunityOverlapPropagationAlgorithm copra=new CommunityOverlapPropagationAlgorithm();

        try {
            copra.detectOverlappingCommunities(dynamicGraph);
        } catch (OcdAlgorithmException | OcdMetricException | InterruptedException e) {
            e.printStackTrace();
        }

        
        
        
    }

}
