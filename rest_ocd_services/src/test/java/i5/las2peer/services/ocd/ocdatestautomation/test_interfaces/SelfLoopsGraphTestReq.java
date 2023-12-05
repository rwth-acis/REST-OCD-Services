package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible networks with self loops.
 */
public interface SelfLoopsGraphTestReq extends BaseGraphTestReq {


    /**
     * Test OCDA on a graph consisting of a cycle where every node has a self loop
     */
    @Test
    default void basicTestOnAllNodeSelfLoopGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside SelfLoopGraphTestReq.basicTestOnAllNodeSelfLoopGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getAllNodeSelfLoopGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on an undirected bipartite graph with self loops
     */
    @Test
    default void basicTestOnBipartiteGraphWithSelfLoops(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside SelfLoopGraphTestReq.basicTestOnBipartiteGraphWithSelfLoops: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getBipartiteGraphWithSelfLoops();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
