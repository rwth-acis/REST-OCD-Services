package i5.las2peer.services.ocd.test_interfaces.ocda;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThrows;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible with directed networks.
 */
public interface DirectedGraphTestReq extends BaseGraphTestReq {

    /**
     * Test OCDA on a directed Sawmill graph
     */
    @Test
    default void basicTestOnSawmill(){
        // I want to print algo object from ClizzAlgorithmTest here: System.out.println(algo);
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnSawmill: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     *  Test OCDA on a directed aperiodic graph with two communities.
     */
    @Test
    default void basicTestOnDirectedAperiodicTwoCommunitiesGraph(){
        // I want to print algo object from ClizzAlgorithmTest here: System.out.println(algo);
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
        // I want to print algo object from ClizzAlgorithmTest here: System.out.println(algo);
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
        // I want to print algo object from ClizzAlgorithmTest here: System.out.println(algo);
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
        // I want to print algo object from ClizzAlgorithmTest here: System.out.println(algo);
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
        // I want to print algo object from ClizzAlgorithmTest here: System.out.println(algo);
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside DirectedGraphTestReq.basicTestOnCliqueWithOutliers: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getCliqueWithOutliersGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    // this test covers methods like getParameters() and increases coverage but this is not really needed to be covered
    //@Disabled
    @Test
    default void checkingParams(){
        getAlgorithm().compatibleGraphTypes();
        getAlgorithm().getAlgorithmType();
        Map<String,String> receivedParams = getAlgorithm().getParameters();
        Map<String,String> recreatedParams = new HashMap<String, String>();
        for (String receivedParam : receivedParams.keySet()){
            recreatedParams.put(receivedParam, receivedParams.get(receivedParam));
        }

        // add a non-existent parameter to check fi the exception is thrown
        recreatedParams.put("non-existent-parameter", "100");

        assertThrows(IllegalArgumentException.class, () -> {
            getAlgorithm().setParameters(recreatedParams);
        });

    }
}
