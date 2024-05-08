package i5.las2peer.services.ocd.testsUtils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.*;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.*;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

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
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[9], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[5]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
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
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[10]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
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
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[10]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
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
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[9]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[10]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
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
			graph.addNode(Integer.toString(i));
		}
		// Creates edges
		Node n[] = graph.nodes().toArray(Node[]::new);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[8]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
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
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[4]);
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
			n[i] = graph.addNode();
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
		adapterParam.put("startDate", "2002-04-14");
		adapterParam.put("endDate", "2003-11-21");
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
	
	public static CustomGraph getSignedGahukuGamaTestGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.gahukuGamaWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.gahukuGamaGraphName);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getLfrGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.lfrUnweightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.lfrGraphName);
		graph.addType(GraphType.DIRECTED);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	// @author: YLi
	public static CustomGraph getSignedLfrGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSignedLfrSixNodesGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrSixNodesWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrSixNodesGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSignedLfrBlurredGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrBlurredWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSignedLfrMadeUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.signedLfrWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.signedLfrGraphName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSloveneParliamentaryPartyGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.sloveneParliamentaryPartyWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.sloveneParliamentaryPartyGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSloveneParliamentaryPartyDirectedGraph()
			throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.sloveneParliamentaryPartyWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.sloveneParliamentaryPartyGraphName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		processor.makeDirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getWikiElecGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.wikiElecWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.wikiElecGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getWikiElecUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.wikiElecWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.wikiElecGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getEpinionsGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.epinionsWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.epinionsGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getEpinionsUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.epinionsWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.epinionsGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getslashDotGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.slashDotWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.slashDotGraphName);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getSlashDotUndirectedGraph() throws FileNotFoundException, AdapterException {
		GraphInputAdapter adapter = new WeightedEdgeListGraphInputAdapter();
		adapter.setReader(new FileReader(OcdTestConstants.slashDotWeightedEdgeListInputPath));
		CustomGraph graph = adapter.readGraph();
		graph.setName(OcdTestConstants.slashDotGraphName);
		graph.addType(GraphType.NEGATIVE_WEIGHTS);
		GraphProcessor processor = new GraphProcessor();
		processor.makeUndirected(graph);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	// @author: YLi
	public static CustomGraph getFiveNodesGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.fiveNodesGraphName);
		// Creates nodes
		Node n[] = new Node[5];
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		// Set edge weight
		graph.setEdgeWeight(graph.getEdge(0), 1);
		graph.setEdgeWeight(graph.getEdge(1), -1);
		graph.setEdgeWeight(graph.getEdge(2), 1);
		graph.setEdgeWeight(graph.getEdge(3), -1);
		graph.setEdgeWeight(graph.getEdge(4), 1);
		graph.setEdgeWeight(graph.getEdge(5), 1);
		graph.setEdgeWeight(graph.getEdge(6), 1);
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphUndirectedUnweighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Undirected Unweighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphUndirectedWeighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Undirected Weighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
		// Set edge weights
		graph.setEdgeWeight(n[0].getEdgeToward(n[1]), 3.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[0]), 3.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[2]), 2.0);
		graph.setEdgeWeight(n[2].getEdgeToward(n[1]), 2.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[3]), 2.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[1]), 2.0);
		graph.setEdgeWeight(n[2].getEdgeToward(n[3]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[2]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[4]), 1.0);
		graph.setEdgeWeight(n[4].getEdgeToward(n[3]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[5]), 1.0);
		graph.setEdgeWeight(n[5].getEdgeToward(n[3]), 1.0);
		graph.setEdgeWeight(n[4].getEdgeToward(n[6]), 4.0);
		graph.setEdgeWeight(n[6].getEdgeToward(n[4]), 4.0);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		graph.addType(GraphType.WEIGHTED);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphDirectedUnweighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Directed Unweighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		graph.addType(GraphType.DIRECTED);
		return graph;
	}
	
	public static CustomGraph getSimpleGraphDirectedWeighted() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph Directed Weighted");
		
		// Create nodes
		Node n[] = new Node[7];
		for (int i = 0; i < 7; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		// Set edge weights
		graph.setEdgeWeight(n[0].getEdgeToward(n[1]), 3.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[2]), 2.0);
		graph.setEdgeWeight(n[1].getEdgeToward(n[3]), 2.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[2]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[4]), 1.0);
		graph.setEdgeWeight(n[3].getEdgeToward(n[5]), 1.0);
		graph.setEdgeWeight(n[4].getEdgeToward(n[6]), 4.0);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		graph.addType(GraphType.DIRECTED);
		graph.addType(GraphType.WEIGHTED);
		return graph;
	}
	
	/*
	 * Graph taken from the paper "The worst-case time complexity for generating 
	 * all maximal cliques and computational experiments" from Tomita et al. 
	 */
	public static CustomGraph getMaximalCliqueGraph() {
		CustomGraph graph = new CustomGraph();
		graph.setName("Simple Test Graph for Maximal Clique Search");
		
		// Create nodes
		Node n[] = new Node[9];
		for (int i = 0; i < 9; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			graph.setNodeName(n[i], Integer.toString(i));
		}
		// Create edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[8]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[8], n[2]);
		
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getLinkgraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.LinkGraphName);
		// Creates nodes
		Node n[] = new Node[8];  
		for (int i = 0; i < 8; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[4]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[7], n[6]);
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
	
	public static CustomGraph getModularityTestGraph() {
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		graph.setName(OcdTestConstants.ModularityTestGraphName);
		// Creates nodes
		Node n[] = new Node[4];  
		for (int i = 0; i < 4; i++) {
			n[i] = graph.addNode(Integer.toString(i));
		}
		// Creates edges
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[0]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[1]);
		graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
		graph.addEdge(UUID.randomUUID().toString(), n[3], n[2]);

	
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()) {
			Edge edge = edges.next();
			graph.setEdgeWeight(edge, 1);
		}
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	public static DynamicGraph getTimestampedKarateGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new TimestampedEdgeListInputAdapter(new FileReader(OcdTestConstants.timestampedKarateClubPath));
		DynamicGraph graph = (DynamicGraph) adapter.readGraph();
		graph.setName(OcdTestConstants.timestampedKarateName);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DYNAMIC);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}

	public static DynamicGraph getRdynTestGraph() throws AdapterException, FileNotFoundException {
		GraphInputAdapter adapter = new TimestampedEdgeListInputAdapter(new FileReader(OcdTestConstants.rdynTestGraphPath));
		DynamicGraph graph = (DynamicGraph) adapter.readGraph();
		graph.setName(OcdTestConstants.rdynTestGraph);
		GraphProcessor processor = new GraphProcessor();
		graph.addType(GraphType.DYNAMIC);
		processor.makeCompatible(graph, new HashSet<GraphType>());
		GraphCreationLog log = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String, String>());
		log.setStatus(ExecutionStatus.COMPLETED);
		graph.setCreationMethod(log);
		return graph;
	}
}
