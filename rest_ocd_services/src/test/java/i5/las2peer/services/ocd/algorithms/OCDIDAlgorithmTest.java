package i5.las2peer.services.ocd.algorithms;

import org.junit.Test;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;


public class OCDIDAlgorithmTest {

    @Test
    public void testOnAperiodicTwoCommunitiesGraph() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm

        // Set parameters
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("thresholdOCDID", "0.001");
        inputParams.put("thresholdCD", "0.001");
        inputParams.put("thresholdOCD", "0.2");
        ocdid.setParameters(inputParams);

        CustomGraph sawmill = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();

        try {
            Cover c = ocdid.detectOverlappingCommunities(sawmill);
            System.out.println(c.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
