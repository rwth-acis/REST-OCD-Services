package i5.las2peer.services.ocd.algorithms;

import org.graphstream.graph.Node;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.Cover;
import org.junit.jupiter.api.Test;

import java.util.UUID;


public class MemeticLinkClusteringAlgorithmTest {
    
    @Test

    public void CustomGraphTest(){

        try {
            CustomGraph graph = new CustomGraph();
            Node n1 = graph.addNode(Integer.toString(0));
            Node n2 = graph.addNode(Integer.toString(1));
            Node n3 = graph.addNode(Integer.toString(2));
            Node n4 = graph.addNode(Integer.toString(3));
            Node n5 = graph.addNode(Integer.toString(4));
            Node n6 = graph.addNode(Integer.toString(5));

            graph.addEdge(UUID.randomUUID().toString(), n1, n2);
            graph.addEdge(UUID.randomUUID().toString(), n2, n3);
            graph.addEdge(UUID.randomUUID().toString(), n3, n1);
            graph.addEdge(UUID.randomUUID().toString(), n4, n5);
            graph.addEdge(UUID.randomUUID().toString(), n5, n6);
            graph.addEdge(UUID.randomUUID().toString(), n6, n4);


            OcdAlgorithm algo = new MemeticLinkClusteringAlgorithm();
            Cover cover = algo.detectOverlappingCommunities(graph);
            System.out.println(cover.toString());

        }catch(Exception e){e.printStackTrace();}
    }
}
