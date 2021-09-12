package i5.las2peer.services.ocd.viewer.testsUtil;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.LabeledMembershipMatrixCoverInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GmlGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import y.base.Edge;
import y.base.Node;

public class ViewerTestGraphFactory {
	
	public static CustomGraph getTinyCircleGraph() {
		// Initialization
		CustomGraph graph = new CustomGraph();
		Matrix memberships = new Basic2DMatrix(10, 2);
		// Creates 10 nodes.
		Node n[] = new Node[10];  
		for (int i = 0; i < 10; i++) {
			n[i] = graph.createNode();
			graph.setNodeName(n[i], "id: " + i);
			memberships.set(i, i%2, 1);
		}
		// Creates 10 edges forming a cycle
		Edge e[] = new Edge[10];
		for (int i = 0; i < 10; i++) {
			e[i] = graph.createEdge(n[i], n[(i+1)%10]);
			graph.setEdgeWeight(e[i], 1.0);
		}
		return new CustomGraph(graph);
	}
	
	public static CustomGraph getTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[0], n[3]);
		graph.createEdge(n[0], n[4]);
		graph.createEdge(n[0], n[10]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[5], n[10]);
		graph.createEdge(n[1], n[0]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[3], n[0]);
		graph.createEdge(n[4], n[0]);
		graph.createEdge(n[10], n[0]);
		graph.createEdge(n[6], n[5]);
		graph.createEdge(n[7], n[5]);
		graph.createEdge(n[8], n[5]);
		graph.createEdge(n[9], n[5]);
		graph.createEdge(n[10], n[5]);
		return new CustomGraph(graph);
	}
	
	public static CustomGraph getSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter(new FileReader(ViewerTestConstants.sawmillEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static CustomGraph getDolphinsGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new GmlGraphInputAdapter();
		adapter.setReader(new FileReader(ViewerTestConstants.dolphingGmlInputPath));
		CustomGraph graph = adapter.readGraph();
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		return graph;
	}
	
	public static Cover getSlpaSawmillCover() throws AdapterException, FileNotFoundException {
		CustomGraph graph = getSawmillGraph();
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter();
		adapter.setReader(new FileReader(ViewerTestConstants.slpaSawmillLabeledMembershipMatrix));
		Cover cover = adapter.readCover(graph);
		return cover;
	}
	
	public static Cover getSlpaDolphinsCover() throws AdapterException, FileNotFoundException {
		CustomGraph graph = getDolphinsGraph();
		CoverInputAdapter adapter = new LabeledMembershipMatrixCoverInputAdapter();
		adapter.setReader(new FileReader(ViewerTestConstants.slpaDolphinsLabeledMembershipMatrix));
		Cover cover = adapter.readCover(graph);
		return cover;
	}
	
}
