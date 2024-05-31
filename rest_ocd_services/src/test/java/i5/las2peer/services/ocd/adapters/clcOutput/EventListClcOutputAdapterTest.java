package i5.las2peer.services.ocd.adapters.clcOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.iLCDAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.DynamicGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.CommunityLifeCycle;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;

public class EventListClcOutputAdapterTest {

    @Test
    public void test() throws OcdAlgorithmException, IOException, AdapterException, InterruptedException, OcdMetricException {
        DynamicGraph graph = OcdTestGraphFactory.getRdynTestGraph();
        OcdAlgorithm algo = new iLCDAlgorithm();
        Cover cover = algo.detectOverlappingCommunities(graph);
        CommunityLifeCycle clc = ((iLCDAlgorithm)algo).getClc();
        ClcOutputAdapter adapter = new EventListOutputAdapter(new FileWriter(OcdTestConstants.testEventListClcOutputPath));
        adapter.writeClc(clc);
    }
}
