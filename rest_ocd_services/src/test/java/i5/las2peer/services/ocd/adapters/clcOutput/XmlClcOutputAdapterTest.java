package i5.las2peer.services.ocd.adapters.clcOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.adapters.coverOutput.DefaultXmlCoverOutputAdapter;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.CommunityLifeCycle;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class XmlClcOutputAdapterTest {
    private static final String userName ="clcUser";
    private static final String graphName = "clcGraphName";
    private static final String coverName = "clcCoverName";
    private static final String clcName = "clcName";

    @Test
    public void test() throws AdapterException, IOException{
        DynamicGraph graph = new DynamicGraph();
        graph.setUserName(userName);
        graph.setName(graphName);
        Node nodeA = graph.addNode("A");
        Node nodeB = graph.addNode("B");
        Node nodeC = graph.addNode("C");
        graph.setNodeName(nodeA, "A");
        graph.setNodeName(nodeB, "B");
        graph.setNodeName(nodeC, "C");
        Edge edgeAB = graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
        graph.setEdgeWeight(edgeAB, 5);
        Edge edgeBC = graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeC);
        graph.setEdgeWeight(edgeBC, 2.5);

        Matrix memberships = new CCSMatrix(3, 2);
        memberships.set(0, 0, 1);
        memberships.set(1, 0, 0.5);
        memberships.set(1, 1, 0.5);
        memberships.set(2, 1, 1);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("param1", "val1");
        params.put("param2", "val2");
        CoverCreationLog algo = new CoverCreationLog(CoverCreationType.UNDEFINED, params, new HashSet<GraphType>());
        Cover cover = new Cover(graph, memberships);

        cover.setCreationMethod(algo);
        cover.setName(coverName);
        cover.setCommunityColor(1, Color.BLUE);
        cover.setCommunityName(1, "Community1");

        OcdMetricLog metric = new OcdMetricLog(OcdMetricType.EXECUTION_TIME, 3.55, params, cover);
        cover.addMetric(metric);

        List<String> birth_nodes = new ArrayList<>();
        birth_nodes.add("A");
        birth_nodes.add("B");
        birth_nodes.add("C");
        CommunityLifeCycle clc = new CommunityLifeCycle(cover, graph);
        clc.setName(clcName);
        clc.handleBirth("0", 1, birth_nodes);
        clc.handleBirth("1", 2, birth_nodes);
        clc.handleGrowth("3", 1, "D");

        ClcOutputAdapter adapter = new XmlClcOutputAdapter();
        adapter.setWriter(new FileWriter(OcdTestConstants.testXmlClcOutputPath));
        adapter.writeClc(clc);
    }
}
