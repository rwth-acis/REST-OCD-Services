package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

public class XmlCoverOutputAdapterTest {

	private static final String userName = "coverPersistenceUser";
	private static final String graphName = "coverPersistenceGraph";
	private static final String coverName = "coverPersistenceCover";

	
	@Test
	public void test() throws AdapterException, IOException {
		CustomGraph graph = new CustomGraph();
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
		CoverOutputAdapter adapter = new DefaultXmlCoverOutputAdapter();
		adapter.setWriter(new FileWriter(OcdTestConstants.testXmlCoverOutputPath));
		adapter.writeCover(cover);
	}

}
