package i5.las2peer.services.ocd.algorithms;

import org.junit.Test;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.Cover;
import y.base.Node;

public class MemeticLinkClusteringAlgorithmTest {
    
    @Test

    public void CustomGraphTest(){

        try {
            CustomGraph graph = new CustomGraph();
            for(int i = 0; i < 2; i++){
                Node n1 = graph.createNode();
                Node n2 = graph.createNode();
                Node n3 = graph.createNode();
                graph.createEdge(n1,n2);
                graph.createEdge(n2,n3);
                graph.createEdge(n3,n1);
            }


            OcdAlgorithm algo = new MemeticLinkClusteringAlgorithm();
            Cover cover = algo.detectOverlappingCommunities(graph);
            System.out.println(cover.toString());

        }catch(Exception e){e.printStackTrace();}
    }
}
