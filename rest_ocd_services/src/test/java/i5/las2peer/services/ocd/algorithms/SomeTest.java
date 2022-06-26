package i5.las2peer.services.ocd.algorithms;

import java.io.FileReader;

import org.junit.Test;

import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphMlGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.Cover;
import y.base.Node;
import y.base.Edge;

public class SomeTest {
    
    @Test

    public void smthTest(){

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

        }catch(Exception e){System.out.println(e);}
    }
}
