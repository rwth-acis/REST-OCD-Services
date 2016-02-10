package i5.las2peer.services.ocd.testsUtils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GmlGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.NodeContentEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.NodeWeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
 * Provides graphs for testing purposes.
 * @author Sebastian
 *
 */
public class OcdTestGraphFactory {
	
	public static CustomGraph getTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.twoCommunitiesName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
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
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getAperiodicTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.aperiodicTwoCommunitiesName);
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
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[1], n[10]);
		graph.createEdge(n[4], n[10]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDirectedAperiodicTwoCommunitiesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.directedAperiodicTwoCommunitiesName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.createEdge(n[1], n[0]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[3], n[0]);
		graph.createEdge(n[4], n[0]);
		graph.createEdge(n[10], n[0]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[5], n[10]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[1], n[10]);
		graph.createEdge(n[4], n[10]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSimpleTwoComponentsGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.simpleTwoComponentsName);
		// Creates nodes
		Node n[] = new Node[11];  
		for (int i = 0; i < 11; i++) {
			n[i] = graph.createNode();
		}
		// Creates edges
		graph.createEdge(n[1], n[0]);
		graph.createEdge(n[2], n[0]);
		graph.createEdge(n[3], n[0]);
		graph.createEdge(n[4], n[0]);
		graph.createEdge(n[10], n[0]);
		graph.createEdge(n[5], n[6]);
		graph.createEdge(n[5], n[7]);
		graph.createEdge(n[5], n[8]);
		graph.createEdge(n[5], n[9]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[1], n[10]);
		graph.createEdge(n[4], n[10]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	/*
	 * A small test graph given in the original paper that introduces
	 * the link communities algorithm.
	 * Is undirected and unweighted.
	 */
	public static CustomGraph getLinkCommunitiesTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.linkCommunitiesTestName);
		// Creates nodes
		for (int i = 0; i < 9; i++) {
			graph.createNode();
		}
		// Creates edges
		Node n[] = graph.getNodeArray();
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[0], n[3]);
		graph.createEdge(n[1], n[2]);
		graph.createEdge(n[1], n[3]);
		graph.createEdge(n[2], n[3]);
		graph.createEdge(n[3], n[4]);
		graph.createEdge(n[3], n[5]);
		graph.createEdge(n[3], n[6]);
		graph.createEdge(n[4], n[5]);
		graph.createEdge(n[6], n[7]);
		graph.createEdge(n[6], n[8]);
		graph.createEdge(n[7], n[8]);
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			graph.setEdgeWeight(edge, 1);
			edges.next();
		}
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.sawmillName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDirectedSawmillGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new NodeWeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.sawmillNodeWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.directedSawmillName);
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSiamDmGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.siamDmUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.siamDmName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getFacebookGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.facebookUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.facebookName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getNewmanClizzGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.newmanClizzGraphWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.newmanClizzName);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.NEWMAN, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getNewmanLinkGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.newmanLinkGraphWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.newmanLinkName);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.NEWMAN, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getMiniServiceTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.miniServiceTestGraphName);
		// Creates nodes
		Node n[] = new Node[5];  
		for (int i = 0; i < 5; i++) {
			n[i] = graph.createNode();
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.createEdge(n[0], n[1]);
		graph.createEdge(n[0], n[2]);
		graph.createEdge(n[0], n[3]);
		graph.createEdge(n[0], n[4]);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDolphinsGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new GmlGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.dolphinsGmlInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.dolphinsName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.SELF_LOOPS);
		graph.addType(GraphType.WEIGHTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getDocaTestGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new UnweightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.docaTestUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.docaTestGraphName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.SELF_LOOPS);
		graph.addType(GraphType.WEIGHTED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	/*public static CustomGraph getContentTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.contentTestName);
		// Creates nodes
		
		Node n[] = new Node[5];  
		for (int i = 0; i < 5; i++) {
			n[i] = graph.createNode();
			graph.setNodeName(n[i], Integer.toString(i));
		}
				
		graph.setNodeContent(n[0],"John likes movies theatre test case" );
		graph.setNodeContent(n[1], "Marie likes books theatre case");
		graph.setNodeContent(n[2], "John likes movies theatre test case");
		graph.setNodeContent(n[3], "Marie likes movies theatre case");
		graph.setNodeContent(n[4], "node unconnected");
		
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.CONTENT_UNLINKED);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}*/
	
	public static CustomGraph getJmolTestGraph() throws AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		
		NodeContentEdgeListGraphInputAdapter adapter = new NodeContentEdgeListGraphInputAdapter(new FileReader(OcdTestConstants.jmolEdgeListInputPath));
		Map<String, String> adapterParam = new HashMap<String, String>();
		adapterParam.put("startDate", "2006-04-01");
		adapterParam.put("endDate", "2006-04-30");
		adapterParam.put("path", "C:\\indexes\\jmol2004");
		adapter.setParameter(adapterParam);
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.jmolName);
		graph.addType(GraphType.CONTENT_LINKED);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
}
