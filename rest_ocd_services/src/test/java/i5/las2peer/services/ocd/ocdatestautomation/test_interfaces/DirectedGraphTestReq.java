package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible with directed networks.
 */
public interface DirectedGraphTestReq extends BaseGraphTestReq {

//    /**
//     * Test OCDA on a directed Sawmill graph
//     */
//    @Test
//    default void basicTestOnSawmill(){
//        OcdAlgorithm algo = getAlgorithm();
//        System.out.println("Inside DirectedGraphTestReq.basicTestOnSawmill: " + algo); //TODO: DELETE 555
//        try {
//            CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
//            Cover cover = algo.detectOverlappingCommunities(graph);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }


    /**
     *  Test OCDA on a directed aperiodic graph with two communities.
     */
    @Test
    default void basicTestOnDirectedAperiodicTwoCommunitiesGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnDirectedAperiodicTwoCommunitiesGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     *  Test OCDA on a directed chain graph of 10 nodes.
     */
    @Test
    default void basicTestOnDirectedChainGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnSimpleDirectedChainGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getSimpleDirectedChainGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     *  Test OCDA on a graph where one node is connected to all other nodes.
     */
    @Test
    default void basicTestOnDirectedHubGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnSimpleDirectedHubGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getDirectedHubGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     *  Test OCDA on a graph where all nodes are connected to a single node.
     */
    @Test
    default void basicTestOnReverseDirectedHubGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnReverseDirectedHubGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getReverseDirectedHubGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Test OCDA on a 3 node clique connected (with a directed edge) to a 3 node directed subgraph.
     */
    @Test
    default void basicTestOnCliqueWithOutliers(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnCliqueWithOutliers: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getCliqueWithOutliersGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }



}
