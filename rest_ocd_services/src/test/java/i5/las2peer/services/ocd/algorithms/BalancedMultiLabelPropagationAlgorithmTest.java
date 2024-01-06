package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.graphstream.graph.Node;
import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalancedMultiLabelPropagationAlgorithmTest {

    @Ignore
    @Test
    public void testEntireAlgorithm() throws OcdAlgorithmException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
        BalancedMultiLabelPropagationAlgorithm algo = new BalancedMultiLabelPropagationAlgorithm();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(BalancedMultiLabelPropagationAlgorithm.LPA_MAX_ITERATION_NAME, "10");
        parameters.put(BalancedMultiLabelPropagationAlgorithm.LPA_THRESHOLD_NAME, "0.2");
        algo.setParameters(parameters);
        Cover cover = algo.detectOverlappingCommunities(graph);
        System.out.println(cover);
    }
}