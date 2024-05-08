package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.DynamicGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class iLCDAlgorithmTest {
    @Test
    public void testOnKarateClub() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException{
        iLCDAlgorithm algorithm = new iLCDAlgorithm();

        Map<String,String> params = new HashMap<String, String>();
        params.put("minimalCommunitySize","3");
        params.put("integrationThreshold","0.5");
        params.put("mergeThreshold", "0.3");

        algorithm.setParameters(params);

        DynamicGraph karateClub = OcdTestGraphFactory.getTimestampedKarateGraph();
        try {
            Cover c = algorithm.detectOverlappingCommunities(karateClub);
            System.out.println(c.toString());
        } catch (OcdAlgorithmException | OcdMetricException | InterruptedException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testOnRdynSyntheticGraph() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException{
        iLCDAlgorithm algorithm = new iLCDAlgorithm();
        Map<String,String> params = new HashMap<String, String>();
        params.put("minimalCommunitySize","3");
        params.put("integrationThreshold","0.2");
        params.put("mergeThreshold", "0.5");

        algorithm.setParameters(params);

        DynamicGraph rdynTestGraph = OcdTestGraphFactory.getRdynTestGraph();
        try {
            Cover c = algorithm.detectOverlappingCommunities(rdynTestGraph);
        } catch (OcdAlgorithmException | OcdMetricException | InterruptedException e){
            e.printStackTrace();
        }
    }
}
