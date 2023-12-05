package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible networks with zero weights.
 */
public interface ZeroWeightsGraphTestReq extends BaseGraphTestReq {

    /**
     * Test OCDA on a graph consisting of a cycle with only zero weights
     */
    @Test
    default void basicTestOnZeroCycleGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside ZeroWeightedGraphTestReq.basicTestOnZeroCycleGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getZeroCycleGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a graph consisting of a mixture of zero and non-zero weights
     */
    @Test
    default void basicTestOnZeroAndNonZeroWeightMixGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside ZeroWeightedGraphTestReq.basicTestOnZeroAndNonZeroWeightMixGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getZeroAndNonZeroWeightMixGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a weighted graph consisting of two communities connected with a zero weight edge
     */
    @Test
    default void basicTestOnWeightedGraphConnectedWithZeroWeightEdge(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside ZeroWeightedGraphTestReq.basicTestOnWeightedGraphConnectedWithZeroWeightEdge: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraphConnectedWithZeroWeightEdge();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
