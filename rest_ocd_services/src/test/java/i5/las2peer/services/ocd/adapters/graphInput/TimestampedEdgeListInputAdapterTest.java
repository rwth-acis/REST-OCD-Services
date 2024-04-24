package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.DynamicGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

public class TimestampedEdgeListInputAdapterTest {

    @Test
    public void testWithoutAction() throws AdapterException, FileNotFoundException {
        GraphInputAdapter inputAdapter =
                new TimestampedEdgeListInputAdapter(new FileReader(OcdTestConstants.timestampedEdgeListWithoutActionPath));
        DynamicGraph graph = (DynamicGraph) inputAdapter.readGraph();
        assertEquals(5, graph.getNodeCount());
        assertEquals(7, graph.getEdgeCount());
        assertEquals(7, graph.getDynamicInteractions().size());
        System.out.println(graph.getDynamicInteractions().toString());
    }

    @Test
    public void testWithAction() throws AdapterException, FileNotFoundException {
        GraphInputAdapter inputAdapter =
                new TimestampedEdgeListInputAdapter(new FileReader(OcdTestConstants.timestampedEdgeListWithActionPath));
        DynamicGraph graph = (DynamicGraph) inputAdapter.readGraph();
        assertEquals(7, graph.getNodeCount());
        assertEquals(7, graph.getEdgeCount());
        assertEquals(13, graph.getDynamicInteractions().size());
        System.out.println(graph.getDynamicInteractions().toString());
    }
}
