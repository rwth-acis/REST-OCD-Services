package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputAdapter;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputAdapterFactory;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputFormat;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputAdapter;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputAdapterFactory;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputFormat;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapterFactory;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapterFactory;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputFormat;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapterFactory;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapterFactory;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.simulations.CentralitySimulationType;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.linear.RealMatrix;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages different request-related tasks for the Service Class. Mainly in
 * charge of simple IO tasks and of creating entity managers for persistence
 * purposes.
 * 
 * @author Sebastian
 *
 */
public class RequestHandler {

	/**
	 * The factory used for the creation of document builders.
	 */
	private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	/**
	 * The logger used for request logging.
	 */
	private static final Logger log = Logger.getLogger("Service API");

	/**
	 * The factory used for creating cover output adapters.
	 */
	private static CoverOutputAdapterFactory coverOutputAdapterFactory = new CoverOutputAdapterFactory();

	/**
	 * The factory used for creating centrality output adapters.
	 */
	private static CentralityOutputAdapterFactory centralityOutputAdapterFactory = new CentralityOutputAdapterFactory();
	
	/**
	 * The factory used for creating graph output adapters.
	 */
	private static GraphOutputAdapterFactory graphOutputAdapterFactory = new GraphOutputAdapterFactory();

	/**
	 * The factory used for creating cover input adapters.
	 */
	private static CoverInputAdapterFactory coverInputAdapterFactory = new CoverInputAdapterFactory();
	
	/**
	 * The factory used for creating centrality input adapters.
	 */
	private static CentralityInputAdapterFactory centralityInputAdapterFactory = new CentralityInputAdapterFactory();

	/**
	 * The factory used for creating graph input adapters.
	 */
	private static GraphInputAdapterFactory graphInputAdapterFactory = new GraphInputAdapterFactory();

	/**
	 * Creates a new log entry based on an exception.
	 * 
	 * @param level
	 *            The level of the entry.
	 * @param message
	 *            The entry message.
	 * @param e
	 *            The exception.
	 */
	public synchronized void log(Level level, String message, Exception e) {
		log.log(level, message, e);
	}

	/**
	 * Creates a new log entry.
	 * 
	 * @param level
	 *            The entry message.
	 * @param message
	 *            The entry message.
	 */
	public synchronized void log(Level level, String message) {
		log.log(level, message);
	}

	/**
	 * Returns an error in xml format.
	 * 
	 * @param error
	 *            The error.
	 * @param errorMessage
	 *            An error Message
	 * @return The xml error.
	 */
	/*
	 * Note that this XML document is created "manually" in order to omit any
	 * additional exceptions.
	 */
	public Response writeError(Error error, String errorMessage) {
		if (errorMessage == null) {
			errorMessage = "";
		}
		String message = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + "<Error>" + "<Id>" + error.getId() + "</Id>"
				+ "<Name>" + error.toString() + "</Name>" + "<Message>" + errorMessage + "</Message>" + "</Error>";

		if (error.getId() == 1) {
			return Response.status(Status.BAD_REQUEST).entity(message).build();
		} else if (error.getId() == 2) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
		} else {
			return Response.serverError().build();
		}
	}

	/**
	 * Transforms a parameter xml into a parameter map.
	 * 
	 * @param content
	 *            A parameter xml.
	 * @return The corresponding parameter map.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public Map<String, String> parseParameters(String content)
			throws SAXException, IOException, ParserConfigurationException {
		Map<String, String> parameters = new HashMap<String, String>();
		Document doc = this.parseDocument(content);
		NodeList parameterElts = doc.getElementsByTagName("Parameter");
		for (int i = 0; i < parameterElts.getLength(); i++) {
			Node node = parameterElts.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element elt = (Element) node;
				String name = elt.getElementsByTagName("Name").item(0).getTextContent();
				String value = elt.getElementsByTagName("Value").item(0).getTextContent();
				parameters.put(name, value);
			}
		}
		return parameters;
	}

	/**
	 * Transforms a parameter map into a parameter xml.
	 * 
	 * @param parameters
	 *            A parameter mapping from the name of each parameter to the
	 *            corresponding value.
	 * @return The corresponding parameter xml.
	 * @throws ParserConfigurationException
	 */
	public String writeParameters(Map<String, String> parameters) throws ParserConfigurationException {
		Document doc = getDocument();
		Element paramsElt = doc.createElement("Parameters");
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			Element paramElt = doc.createElement("Parameter");
			Element paramNameElt = doc.createElement("Name");
			paramNameElt.appendChild(doc.createTextNode(entry.getKey()));
			paramElt.appendChild(paramNameElt);
			Element paramValueElt = doc.createElement("Value");
			paramValueElt.appendChild(doc.createTextNode(entry.getValue()));
			paramElt.appendChild(paramValueElt);
			paramsElt.appendChild(paramElt);
		}
		doc.appendChild(paramsElt);
		return this.writeDoc(doc);
	}

	/**
	 * Creates a standard XML document used for confirmation responses.
	 * 
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeConfirmationXml() throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(doc.createElement("Confirmation"));
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing multiple graph ids.
	 * 
	 * @param graphs
	 *            The graphs.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeGraphIds(List<CustomGraph> graphs) throws ParserConfigurationException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for (int i = 0; i < graphs.size(); i++) {
			graphsElt.appendChild(getIdElt(graphs.get(i), doc));
		}
		doc.appendChild(graphsElt);
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing multiple cover ids.
	 * 
	 * @param covers
	 *            The covers.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeCoverIds(List<Cover> covers) throws ParserConfigurationException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for (int i = 0; i < covers.size(); i++) {
			coversElt.appendChild(getIdElt(covers.get(i), doc));
		}
		doc.appendChild(coversElt);
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing multiple CentralityMap ids.
	 * @param maps The CentralityMaps.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeCentralityMapIds(List<CentralityMap> maps) throws ParserConfigurationException {
		Document doc = getDocument();
		Element centralityMapElt = doc.createElement("CentralityMaps");
		for(int i=0; i<maps.size(); i++) {
			centralityMapElt.appendChild(getIdElt(maps.get(i), doc));
		}
		doc.appendChild(centralityMapElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing meta information about multiple
	 * graphs.
	 * 
	 * @param graphs The graphs.
	 * @return The document.
	 * @throws AdapterException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeGraphMetas(List<CustomGraph> graphs) throws AdapterException, ParserConfigurationException,
			IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for (CustomGraph graph : graphs) {
			String metaDocStr = writeGraph(graph, GraphOutputFormat.META_XML);
			Node metaDocNode = parseDocumentToNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			graphsElt.appendChild(importNode);
		}
		doc.appendChild(graphsElt);
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing meta information about multiple
	 * covers.
	 * 
	 * @param covers
	 *            The covers.
	 * @return The document.
	 * @throws AdapterException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeCoverMetas(List<Cover> covers) throws AdapterException, ParserConfigurationException,
			IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for (Cover cover : covers) {
			String metaDocStr = writeCover(cover, CoverOutputFormat.META_XML);
			Node metaDocNode = parseDocumentToNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			coversElt.appendChild(importNode);
		}
		doc.appendChild(coversElt);
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing meta information about multiple centrality maps.
	 * @param maps The centrality maps.
	 * @return The document.
	 * @throws AdapterException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeCentralityMapMetas(List<CentralityMap> maps) throws AdapterException, ParserConfigurationException, IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element mapsElt = doc.createElement("CentralityMaps");
		for(CentralityMap map : maps) {
			String metaDocStr = writeCentralityMap(map, CentralityOutputFormat.META_XML);
			Node metaDocNode = parseDocumentToNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			mapsElt.appendChild(importNode);
		}
		doc.appendChild(mapsElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing the id of a single graph.
	 * 
	 * @param graph
	 *            The graph.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeId(CustomGraph graph) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(graph, doc));
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing the id of a single cover.
	 * 
	 * @param cover
	 *            The cover.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeId(Cover cover) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(cover, doc));
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing the id of a single CentralityMap.
	 * @param map The CentralityMap.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeId(CentralityMap map) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(map, doc));
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing the id of a single metric log.
	 * 
	 * @param metricLog
	 *            The metric log.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeId(OcdMetricLog metricLog) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(metricLog, doc));
		return writeDoc(doc);
	}

	/**
	 * Creates a graph output in a specified format.
	 * 
	 * @param graph
	 *            The graph.
	 * @param outputFormat
	 *            The format.
	 * @return The graph output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeGraph(CustomGraph graph, GraphOutputFormat outputFormat)
			throws AdapterException, InstantiationException, IllegalAccessException {
		GraphOutputAdapter adapter = graphOutputAdapterFactory.getInstance(outputFormat);
		Writer writer = new StringWriter();
		adapter.setWriter(writer);
		adapter.writeGraph(graph);
		return writer.toString();
	}

	/**
	 * Creates a cover output in a specified format.
	 * 
	 * @param cover
	 *            The cover.
	 * @param outputFormat
	 *            The format.
	 * @return The cover output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeCover(Cover cover, CoverOutputFormat outputFormat)
			throws AdapterException, InstantiationException, IllegalAccessException {
		Writer writer = new StringWriter();
		CoverOutputAdapter adapter = coverOutputAdapterFactory.getInstance(outputFormat);
		adapter.setWriter(writer);
		adapter.writeCover(cover);
		return writer.toString();
	}

	/**
	 * Creates a CentralityMap output.
	 * @param map The CentralityMap.
	 * @return The CentralityMap output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParserConfigurationException 
	 */
	public String writeCentralityMap(CentralityMap map, CentralityOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException, ParserConfigurationException {
		Writer writer = new StringWriter();
		CentralityOutputAdapter adapter = centralityOutputAdapterFactory.getInstance(outputFormat);
    	adapter.setWriter(writer);
		adapter.writeCentralityMap(map);
		return writer.toString();
	}
	
	/**
	 * Creates an XML document containing all statistical measure names.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeStatisticalMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(OcdMetricType e : OcdMetricType.class.getEnumConstants()) {
			if(e.correspondsStatisticalMeasure()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all knowledge-driven measure names.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeKnowledgeDrivenMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(OcdMetricType e : OcdMetricType.class.getEnumConstants()) {
			if(e.correspondsKnowledgeDrivenMeasure()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all ground truth benchmark names.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeGroundTruthBenchmarkNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(GraphCreationType e : GraphCreationType.class.getEnumConstants()) {
			if(e.correspondsGroundTruthBenchmark()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all ocd algorithm names.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeAlgorithmNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(CoverCreationType e : CoverCreationType.class.getEnumConstants()) {
			if(e.correspondsAlgorithm()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all centrality measure names.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeCentralityMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(CentralityMeasureType e : CentralityMeasureType.class.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.getDisplayName()));
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all CentralitySimulation names.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeCentralitySimulationNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(CentralitySimulationType e : CentralitySimulationType.class.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.getDisplayName()));
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Parses a graph input using a specified format.
	 * 
	 * @param contentStr
	 *            The graph input.
	 * @param inputFormat
	 *            The format.
	 * @return The graph.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public CustomGraph parseGraph(String contentStr, GraphInputFormat inputFormat)
			throws AdapterException, InstantiationException, IllegalAccessException {
		GraphInputAdapter adapter = graphInputAdapterFactory.getInstance(inputFormat);
		Reader reader = new StringReader(contentStr);
		adapter.setReader(reader);
		return adapter.readGraph();
	}

	/**
	 * Parses a graph input using a specified format.
	 * 
	 * @param contentStr
	 *            The graph input.
	 * @param inputFormat
	 *            The format.
	 * @param param
	 *            Parametes that are passed to the adapters.
	 * @return The graph.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParseException
	 * @throws IllegalArgumentException
	 */
	public CustomGraph parseGraph(String contentStr, GraphInputFormat inputFormat, Map<String, String> param)
			throws AdapterException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			ParseException {
		GraphInputAdapter adapter = graphInputAdapterFactory.getInstance(inputFormat);
		Reader reader = new StringReader(contentStr);
		adapter.setReader(reader);
		adapter.setParameter(param);
		return adapter.readGraph();
	}

	/**
	 * Parses a cover input using a specified format.
	 * 
	 * @param contentStr
	 *            The cover input.
	 * @param graph
	 *            The graph that the cover is based on.
	 * @param inputFormat
	 *            The format.
	 * @return The cover.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Cover parseCover(String contentStr, CustomGraph graph, CoverInputFormat inputFormat)
			throws AdapterException, InstantiationException, IllegalAccessException {
		CoverInputAdapter adapter = coverInputAdapterFactory.getInstance(inputFormat);
		Reader reader = new StringReader(contentStr);
		adapter.setReader(reader);
		return adapter.readCover(graph);
	}
	
	/**
	 * Creates a CentralityMap by parsing a String containing centrality values.
	 * 
	 * @param contentStr
	 *            The centrality input
	 * @param graph
	 *            The graph the centrality values are based on.
	 * @return The CentralityMap.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public CentralityMap parseCentralityMap(String contentStr, CustomGraph graph, CentralityInputFormat inputFormat)
			throws AdapterException, InstantiationException, IllegalAccessException {
		CentralityInputAdapter adapter = centralityInputAdapterFactory.getInstance(inputFormat);
		Reader reader = new StringReader(contentStr);
		adapter.setReader(reader);
		return adapter.readCentrality(graph);
	}

	/**
	 * Returns an XML element node representing the id of a graph.
	 * 
	 * @param graph
	 *            The graph.
	 * @param doc
	 *            The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(CustomGraph graph, Document doc) {
		Element graphElt = doc.createElement("Graph");
		Element graphIdElt = doc.createElement("Id");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(graph.getId())));
		graphElt.appendChild(graphIdElt);
		return graphElt;
	}

	/**
	 * Returns an XML element node representing the id of a cover.
	 * 
	 * @param cover
	 *            The cover.
	 * @param doc
	 *            The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(Cover cover, Document doc) {
		Element coverElt = doc.createElement("Cover");
		Element idElt = doc.createElement("Id");
		Element coverIdElt = doc.createElement("CoverId");
		coverIdElt.appendChild(doc.createTextNode(Long.toString(cover.getId())));
		idElt.appendChild(coverIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(cover.getGraph().getId())));
		idElt.appendChild(graphIdElt);
		coverElt.appendChild(idElt);
		return coverElt;
	}

	/**
	 * Returns an XML element node representing the id of a CentralityMap.
	 * @param cover The CentralityMap.
	 * @param doc The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(CentralityMap map, Document doc) {
		Element centralityMapElt = doc.createElement("CentralityMap");
		Element idElt = doc.createElement("Id");
		Element centralityMapIdElt = doc.createElement("CentralityMapId");
		centralityMapIdElt.appendChild(doc.createTextNode(Long.toString(map.getId())));
		idElt.appendChild(centralityMapIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(map.getGraph().getId())));
		idElt.appendChild(graphIdElt);
		centralityMapElt.appendChild(idElt);
		return centralityMapElt;
	}
	
	/**
	 * Returns an XML element node representing the id of a metric log.
	 * 
	 * @param metricLog
	 *            The metric log.
	 * @param doc
	 *            The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(OcdMetricLog metricLog, Document doc) {
		Element metricElt = doc.createElement("Metric");
		Element idElt = doc.createElement("Id");
		Element metricIdElt = doc.createElement("MetricId");
		metricIdElt.appendChild(doc.createTextNode(Long.toString(metricLog.getId())));
		idElt.appendChild(metricIdElt);
		Element coverIdElt = doc.createElement("CoverId");
		coverIdElt.appendChild(doc.createTextNode(Long.toString(metricLog.getCover().getId())));
		idElt.appendChild(coverIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(metricLog.getCover().getGraph().getId())));
		idElt.appendChild(graphIdElt);
		metricElt.appendChild(idElt);
		return metricElt;
	}

	/**
	 * Transforms a document into a string.
	 * 
	 * @param doc
	 *            The document.
	 * @return The document string.
	 */
	protected String writeDoc(Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
		LSSerializer lsSerializer = domImplementation.createLSSerializer();
		return lsSerializer.writeToString(doc);
	}

	/**
	 * Creates an empty document.
	 * 
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	protected Document getDocument() throws ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.newDocument();
	}

	/**
	 * Transforms an XML document in string form into an actual XML node.
	 * 
	 * @param docString
	 *            The document string.
	 * @return The node.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	protected Node parseDocumentToNode(String docString)
			throws ParserConfigurationException, IOException, SAXException {
		Document doc = parseDocument(docString);
		return doc.getDocumentElement();
	}

	/**
	 * Parses an XML document in string form into an actual XML document.
	 * 
	 * @param docString
	 *            The document string.
	 * @return The document.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	protected Document parseDocument(String docString) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Reader reader = new StringReader(docString);
		Document doc = builder.parse(new InputSource(reader));
		return doc;
	}

	/**
	 * Parses a string into a boolean value. Only the words TRUE and FALSE are
	 * accepted, ignoring the letter cases though.
	 * 
	 * @param valueStr
	 *            The value in string format.
	 * @return The boolean value.
	 */
	public boolean parseBoolean(String valueStr) {
		boolean value = Boolean.parseBoolean(valueStr);
		if (!value) {
			if (!(valueStr).matches("(?iu)false")) {
				throw new IllegalArgumentException();
			}
		}
		return value;
	}

	/**
	 * Creates an XML document containing all enum constant names of a given
	 * enum class.
	 * 
	 * @param enumClass
	 *            The class object of the corresponding enum class.
	 * @param <E>
	 *            An enum subclass type.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public <E extends Enum<E>> String writeEnumNames(final Class<E> enumClass) throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for (E e : enumClass.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}

	/**
	 * Parses a single string into a list of strings by splitting on the "-"
	 * delimiter. Intended for parsing multiple values passed as single request
	 * query parameter.
	 * 
	 * @param paramStr
	 *            A string (possibly containing) "-" delimiters.
	 * @return The string list.
	 */
	public List<String> parseQueryMultiParam(String paramStr) {
		return Arrays.asList(paramStr.split("-"));
	}
	
	/**
	 * Creates an XML document containing the correlation matrix of a number of centrality maps.
	 * @param mapIds The list of centrality map ids.
	 * @param correlationMatrix The matrix containing the correlations for each pair of centrality maps.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeCorrelationMatrix(List<Integer> mapIds, RealMatrix correlationMatrix) throws ParserConfigurationException {
		Document doc = getDocument();
		Element matrixElt = doc.createElement("Matrix");
		int n = mapIds.size();
		Element rowElt;
		for(int i = 0; i < n; i++) {
			rowElt = doc.createElement("Row");
			rowElt.setAttribute("CentralityMapId", Integer.toString(mapIds.get(i)));
			for(int j = 0; j < n; j++) {
				Element cellElt = doc.createElement("Cell");
				cellElt.setAttribute("CentralityMapId", Integer.toString(mapIds.get(j)));
				cellElt.appendChild(doc.createTextNode(Double.toString(correlationMatrix.getEntry(i, j))));
				rowElt.appendChild(cellElt);
			}
			matrixElt.appendChild(rowElt);
		}
		doc.appendChild(matrixElt);
		return writeDoc(doc);
	}
}
