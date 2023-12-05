package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible with undirected networks.
 */
public interface UndirectedGraphTestReq extends BaseGraphTestReq {

    /**
     * Test OCDA on a simple undirected cycle graph
     */
    //@Disabled
    @Test
    default void basicTestOnSimpleCycleGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside UndirectedGraphTestReq.basicTestOnSimpleCycleGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getSimpleCycleGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a small clique
     */
    //@Disabled
    @Test
    default void basicTestOnSimpleClique(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside UndirectedGraphTestReq.basicTestOnSimpleClique: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getSimpleClique();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on an undirected bipartite graph
     */
    //@Disabled
    @Test
    default void basicTestOnUndirectedBipartiteGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside UndirectedGraphTestReq.basicTestOnUndirectedBipartiteGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getUndirectedBipartiteGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on an undirected hub graph
     */
    //@Disabled
    @Test
    default void basicTestOnUndirectedHubGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside UndirectedGraphTestReq.basicTestOnUndirectedHubGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getUndirectedHubGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on an undirected graph consisting of a combination
     * of a cycle and star graph.
     */
    //@Disabled
    @Test
    default void basicTestOnUndirectedWheelGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside UndirectedGraphTestReq.basicTestOnUndirectedWheelGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getUndirectedWheelGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }



}
