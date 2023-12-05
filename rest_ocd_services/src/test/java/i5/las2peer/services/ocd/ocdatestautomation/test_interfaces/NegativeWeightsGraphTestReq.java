package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

/**
 * This interface holds basic test methods that should be present
 * in all OCDA compatible networks with negative weights.
 */
public interface NegativeWeightsGraphTestReq extends BaseGraphTestReq{
    /**
     * Test OCDA on a graph consisting of a cycle with only negative weights
     */
    @Test
    default void basicTestOnNegativeCycleGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside NegativeWeightGraphTestReq.basicTestOnNegativeCycleGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getNegativeCycleGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test OCDA on a graph consisting of a mixture of positive and negative weights
     */
    @Test
    default void basicTestOnMixedWeightsGraph(){
        OcdAlgorithm algo = getAlgorithm();
        System.out.println("Inside NegativeWeightGraphTestReq.basicTestOnMixedWeightsGraph: " + algo); //TODO: DELETE 555
        try {
            CustomGraph graph = OcdTestGraphFactory.getMixedWeightsGraph();
            Cover cover = algo.detectOverlappingCommunities(graph);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
