package i5.las2peer.services.ocd.ocdatestautomation.test_interfaces;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThrows;

/**
 * This interface holds tests related to OCDA parameters
 */
public interface OCDAParameterTestReq extends BaseGraphTestReq {

    // this test covers methods like getParameters() and increases coverage but this is not really needed to be covered

    /**
     * Test to check if the algorithm has functioning basic methods that should be part of
     * all OCDA implemented in WebOCD. Additionally, it checks that the algorithm
     * can deal with incorrect parameters.
     */
    @Test
    default void OCDAParameterTest(){
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
