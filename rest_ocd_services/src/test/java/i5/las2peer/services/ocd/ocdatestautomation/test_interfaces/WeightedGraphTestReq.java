package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible with weighted networks.
 */
public interface WeightedGraphTestReq extends BaseGraphTestReq {

    /**
     * Test OCDA on a weighted graph consisting of equal edge weights
     */
    @Test
    default void basicTestOnGraphWithEqualWeights(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside WeightedGraphTestReq.basicTestOnGraphWithEqualWeights: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getCompleteGraphWithEqualWeights();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a weighted chain graph with increasing weights
     */
    @Test
    default void basicTestOnChainGraphWithIncreasingWeights(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside WeightedGraphTestReq.basicTestOnChainGraphWithIncreasingWeights: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getChainGraphWithIncreasingWeights();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a graph consisting of a cycle that has an edge
     *  with a weight value that represents an outlier
     */
    @Test
    default void basicTestOnLoopWithOutlierWeight(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside WeightedGraphTestReq.basicTestOnLoopWithOutlierWeight: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getLoopWithOutlierWeight();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a chain graph where most weights have decimal values
     */
    @Test
    default void basicTestOnChainWithDecimalWeights(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside WeightedGraphTestReq.basicTestOnChainWithDecimalWeights: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getChainWithDecimalWeights();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a weighted graph consisting of two communities
     */
    @Test
    default void basicTestOnTwoCommunitiesWeightedGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside WeightedGraphTestReq.basicTestOnTwoCommunitiesWeightedGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesWeightedGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
