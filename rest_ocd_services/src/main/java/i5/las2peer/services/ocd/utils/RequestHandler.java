package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputAdapter;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputAdapterFactory;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputFormat;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputAdapter;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputAdapterFactory;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputFormat;
import i5.las2peer.services.ocd.adapters.centralityOutput.DefaultXmlCentralityOutputAdapter;
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
import i5.las2peer.services.ocd.adapters.metaOutput.*;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeta;
import i5.las2peer.services.ocd.centrality.data.CentralitySimulationType;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	//private static final Logger log = Logger.getLogger("Service API");
	private static final GeneralLogger log = new GeneralLogger();

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
		log.getLogger().log(level, message, e);
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
		log.getLogger().log(level, message);
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
	 * Creates an xml document string that holds user, user's content
	 * deletion date and the number of days until the deletion
	 * @param username        User for which the deletion date info is generated
	 * @param deletionDate    Deletion date of the user's content
	 * @return                Xml string of user's content deletion date info
	 * @throws ParserConfigurationException
	 */
	public String writeDeletionDate(String username, LocalDate deletionDate) throws ParserConfigurationException {
		LocalDate currentDate = LocalDate.now();
		long daysTillDeletion = deletionDate.toEpochDay() - currentDate.toEpochDay();

		Document doc = getDocument();
		Element deletionInfoElt = doc.createElement("DeletionInfo");
		Element userElt = doc.createElement("User");
		userElt.appendChild(doc.createTextNode(username));
		deletionInfoElt.appendChild(userElt);
		Element dateElt = doc.createElement("DeletionDate");
		dateElt.appendChild(doc.createTextNode(String.valueOf(deletionDate)));
		deletionInfoElt.appendChild(dateElt);
		Element daysTillDeletionElt = doc.createElement("DaysTillDeletion");
		daysTillDeletionElt.appendChild(doc.createTextNode(String.valueOf(daysTillDeletion)));
		deletionInfoElt.appendChild(daysTillDeletionElt);
		doc.appendChild(deletionInfoElt);
		return writeDoc(doc);
	}
	/////////////////////////

	/**
	 * Transforms a parameter xml into a parameter map.
	 * 
	 * @param content
	 *            A parameter xml.
	 * @return The corresponding parameter map.
	 * @throws SAXException if parsing failed
	 * @throws IOException if reading failed
	 * @throws ParserConfigurationException if parser configuration failed
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
	 * @throws ParserConfigurationException if parser config failed
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
	 * @throws ParserConfigurationException if parser config failed
	 */
	public String writeConfirmationXml() throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(doc.createElement("Confirmation"));
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XMl document containing a single value.
	 * 
	 * @param value The value that is written to the document.
	 * @return The document.
	 * @throws ParserConfigurationException if parser config failed
	 */
	public String writeValueXml(double value) throws ParserConfigurationException {
		Document doc = getDocument();
		Element valueElt = doc.createElement("Value");
		valueElt.appendChild(doc.createTextNode(Double.toString(value)));
		doc.appendChild(valueElt);
		return writeDoc(doc);
	}

	/**
	 * Creates an XML document containing multiple graph ids.
	 * 
	 * @param graphs
	 *            The graphs.
	 * @return The document.
	 * @throws ParserConfigurationException if parser config failed
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
	 * Creates an XML document containing multiple graph ids.
	 * This method uses efficient approach and only loads necessary data
	 * (e.g. no node/edge info is loaded)
	 *
	 * @param graphMetas
	 *            The graph meta instances holding graph meta information.
	 * @return The document.
	 * @throws ParserConfigurationException if parser config failed
	 */
	public String writeGraphIdsEfficiently(List<CustomGraphMeta> graphMetas) throws ParserConfigurationException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for (int i = 0; i < graphMetas.size(); i++) {
			graphsElt.appendChild(getIdElt(graphMetas.get(i), doc));
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
	 * @throws ParserConfigurationException if parser config failed
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
	 * Creates an XML document containing multiple cover ids efficiently,
	 * using CoverMeta instance instead of cover instance.
	 *
	 * @param coverMetas
	 *            The covers.
	 * @return The document.
	 * @throws ParserConfigurationException if parser config failed
	 */
	public String writeCoverIdsEfficiently(List<CoverMeta> coverMetas) throws ParserConfigurationException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for (int i = 0; i < coverMetas.size(); i++) {
			coversElt.appendChild(getIdElt(coverMetas.get(i), doc));
		}
		doc.appendChild(coversElt);
		return writeDoc(doc);
	}


	/**
	 * Creates an XML document containing multiple CentralityMap ids.
	 * @param maps The CentralityMaps.
	 * @return The document.
	 * @throws ParserConfigurationException if parser config failed
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
	 * Creates an XML document containing multiple CentralityMap ids.
	 * @param centralityMetas The meta information list.
	 * @return The document.
	 * @throws ParserConfigurationException if parser config failed
	 */
	public String writeCentralityMapIdsEfficiently(List<CentralityMeta> centralityMetas) throws ParserConfigurationException {
		Document doc = getDocument();
		Element centralityMapElt = doc.createElement("CentralityMaps");
		for(int i=0; i<centralityMetas.size(); i++) {
			centralityMapElt.appendChild(getIdElt(centralityMetas.get(i), doc));
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
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * graphs. This is an efficient method that does not load more data
	 * than necessary (e.g. no node/edge info is loaded)
	 *
	 * @param graphMetass The list of graph meta instances that hold graph meta information.
	 * @return The document.
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public String writeGraphMetasEfficiently(List<CustomGraphMeta> graphMetass) throws AdapterException, ParserConfigurationException,
			IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for (CustomGraphMeta graphMeta : graphMetass) {
			String metaDocStr = writeGraphEfficiently(graphMeta);
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
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * Creates an XML document containing meta information about multiple
	 * covers.
	 *
	 * @param coverMetas
	 *            The covers' meta information.
	 * @return The document.
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public String writeCoverMetasEfficiently(List<CoverMeta> coverMetas) throws AdapterException, ParserConfigurationException,
			IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for (CoverMeta coverMeta : coverMetas) {
			String metaDocStr = writeCoverEfficiently(coverMeta);
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
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * Creates an XML document containing meta information about multiple centrality maps.
	 * @param centralityMetas The centrality meta information list.
	 * @return The document.
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public String writeCentralityMapMetasEfficiently(List<CentralityMeta> centralityMetas) throws AdapterException, ParserConfigurationException, IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element mapsElt = doc.createElement("CentralityMaps");
		for(CentralityMeta centralityMetaInfo : centralityMetas) {
			String metaDocStr = writeCentralityMapEfficiently(centralityMetaInfo);
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
	 * @throws ParserConfigurationException if parsing failed
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
	 * @throws ParserConfigurationException if parsing failed
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
	 * @throws ParserConfigurationException if parsing failed
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
	 * @throws ParserConfigurationException if parsing failed
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
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * Creates a graph output in a MetaXml format. This method uses efficient approach.
	 * Only necessary information is loaded (e.g. no node/edge info)
	 *            The graph.
	 *
	 * @param graphMeta
	 *         Graph meta information
	 * @return The graph output.
	 * @throws AdapterException if adapter failed
	 */
	public String writeGraphEfficiently(CustomGraphMeta graphMeta)
			throws AdapterException{
		GraphMetaOutputAdapter adapter = new MetaXmlGraphMetaOutputAdapter();
		Writer writer = new StringWriter();
		adapter.setWriter(writer);
		adapter.writeGraph(graphMeta);
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
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * Creates a cover output efficiently in MetaXml format
	 *
	 * @param coverMeta
	 *            The cover meta information.
	 * @return The cover output.
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public String writeCoverEfficiently(CoverMeta coverMeta)
			throws AdapterException, InstantiationException, IllegalAccessException {
		Writer writer = new StringWriter();
		CoverMetaOutputAdapter adapter = new MetaXmlCoverMetaOutputAdapter();
		adapter.setWriter(writer);
		adapter.writeCover(coverMeta);
		return writer.toString();

	}

	/**
	 * Creates a CentralityMap output.
	 * @param map The CentralityMap.
	 * @param outputFormat the output format for the centrality
	 * @return The CentralityMap output.
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public String writeCentralityMap(CentralityMap map, CentralityOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException, ParserConfigurationException {
		Writer writer = new StringWriter();
		CentralityOutputAdapter adapter = centralityOutputAdapterFactory.getInstance(outputFormat);
    	adapter.setWriter(writer);
		adapter.writeCentralityMap(map);
		return writer.toString();
	}
	

	/**
	 * Creates a CentralityMap output in MetaXml format.
	 * @param centralityMeta
	 *               Metadata about centrality
	 * @return The CentralityMap output.
	 * @throws AdapterException if adapter failed
	 * @throws ParserConfigurationException if parser config failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public String writeCentralityMapEfficiently(CentralityMeta centralityMeta) throws AdapterException, InstantiationException, IllegalAccessException, ParserConfigurationException {
		Writer writer = new StringWriter();
		CentralityMetaOutputAdapter adapter = new MetaXmlCentralityMetaOutputAdapter();
		adapter.setWriter(writer);
		adapter.writeCentralityMap(centralityMeta);
		return writer.toString();
	}
	
	public String writeCentralityMapTopNodes(CentralityMap map, int k) throws AdapterException, InstantiationException, IllegalAccessException, ParserConfigurationException {
		Writer writer = new StringWriter();
		DefaultXmlCentralityOutputAdapter adapter = new DefaultXmlCentralityOutputAdapter();
    	adapter.setWriter(writer);
		adapter.writeCentralityMapTopNodes(map, k);
		return writer.toString();
	}
	
	/**
	 * Creates an XML document containing all statistical measure names.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writeStatisticalMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(OcdMetricType e : OcdMetricType.class.getEnumConstants()) {
			if(e.correspondsStatisticalMeasure()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				nameElt.setAttribute("displayName", e.getDisplayName());
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all knowledge-driven measure names.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writeKnowledgeDrivenMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(OcdMetricType e : OcdMetricType.class.getEnumConstants()) {
			if(e.correspondsKnowledgeDrivenMeasure()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				nameElt.setAttribute("displayName", e.getDisplayName());
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all ground truth benchmark names.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writeGroundTruthBenchmarkNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(GraphCreationType e : GraphCreationType.class.getEnumConstants()) {
			if(e.correspondsGroundTruthBenchmark()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				nameElt.setAttribute("displayName", e.getDisplayName());
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all ocd algorithm names.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writeAlgorithmNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(CoverCreationType e : CoverCreationType.class.getEnumConstants()) {
			if(e.correspondsAlgorithm()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				nameElt.setAttribute("displayName", e.getDisplayName());
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all centrality measure names.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writeCentralityMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(CentralityMeasureType e : CentralityMeasureType.class.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			nameElt.setAttribute("displayName", e.getDisplayName());
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all CentralitySimulation names.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writeCentralitySimulationNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(CentralitySimulationType e : CentralitySimulationType.class.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			nameElt.setAttribute("displayName", e.getDisplayName());
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
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 * @throws IllegalArgumentException if arguments were faulty
	 * @throws ParseException if parsing failed
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
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
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
	 * @param inputFormat
	 * 			  The input format for the centrality
	 * @return The CentralityMap.
	 * @throws AdapterException if adapter failed
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public CentralityMap parseCentralityMap(String contentStr, CustomGraph graph, CentralityInputFormat inputFormat)
			throws AdapterException, InstantiationException, IllegalAccessException {
		CentralityInputAdapter adapter = centralityInputAdapterFactory.getInstance(inputFormat);
		Reader reader = new StringReader(contentStr);
		adapter.setReader(reader);
		return adapter.readCentrality(graph);
	}

	/**
	 * Returns an XML element node representing the id (key) of a graph.
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
		graphIdElt.appendChild(doc.createTextNode(graph.getKey()));	//done
		graphElt.appendChild(graphIdElt);
		return graphElt;
	}

	/**
	 * Returns an XML element node representing the id of a graph.
	 *
	 * @param graphMeta
	 *            The graph meta information.
	 * @param doc
	 *            The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(CustomGraphMeta graphMeta, Document doc) {
		Element graphElt = doc.createElement("Graph");
		Element graphIdElt = doc.createElement("Id");
		graphIdElt.appendChild(doc.createTextNode(graphMeta.getKey()));
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
		coverIdElt.appendChild(doc.createTextNode(cover.getKey()));	//done
		idElt.appendChild(coverIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(cover.getGraph().getKey()));	//done
		idElt.appendChild(graphIdElt);
		coverElt.appendChild(idElt);
		return coverElt;
	}

	/**
	 * Returns an XML element node representing the id (key) of a cover.
	 *
	 * @param coverMeta
	 *            The cover meta information.
	 * @param doc
	 *            The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(CoverMeta coverMeta, Document doc) {
		Element coverElt = doc.createElement("Cover");
		Element idElt = doc.createElement("Id");
		Element coverIdElt = doc.createElement("CoverId");
		coverIdElt.appendChild(doc.createTextNode(coverMeta.getKey()));
		idElt.appendChild(coverIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(coverMeta.getGraphKey()));
		idElt.appendChild(graphIdElt);
		coverElt.appendChild(idElt);
		return coverElt;
	}

	/**
	 * Returns an XML element node representing the id of a CentralityMap.
	 * @param map The CentralityMap.
	 * @param doc The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(CentralityMap map, Document doc) {
		Element centralityMapElt = doc.createElement("CentralityMap");
		Element idElt = doc.createElement("Id");
		Element centralityMapIdElt = doc.createElement("CentralityMapId");
		centralityMapIdElt.appendChild(doc.createTextNode(map.getKey()));		
		idElt.appendChild(centralityMapIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(map.getGraph().getKey())); //done
		idElt.appendChild(graphIdElt);
		centralityMapElt.appendChild(idElt);
		return centralityMapElt;
	}
	
	/**
	 * Returns an XML element node representing the id of a CentralityMap.
	 * @param centralityMeta The CentralityMap.
	 * @param doc The document to create the element node for.
	 * @return The element node.
	 */
	protected Node getIdElt(CentralityMeta centralityMeta, Document doc) {
		Element centralityMapElt = doc.createElement("CentralityMap");
		Element idElt = doc.createElement("Id");
		Element centralityMapIdElt = doc.createElement("CentralityMapId");
		centralityMapIdElt.appendChild(doc.createTextNode(centralityMeta.getCentralityKey()));
		idElt.appendChild(centralityMapIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(centralityMeta.getGraphKey()));
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
		metricIdElt.appendChild(doc.createTextNode(metricLog.getKey()));  	//done
		idElt.appendChild(metricIdElt);
		Element coverIdElt = doc.createElement("CoverId");
		coverIdElt.appendChild(doc.createTextNode(metricLog.getCover().getKey()));  //done
		idElt.appendChild(coverIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(metricLog.getCover().getGraph().getKey()));	//done
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
	 * @throws ParserConfigurationException if parsing failed
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
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
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
	 * @throws ParserConfigurationException if parser config failed
	 * @throws IOException if reading failed
	 * @throws SAXException if parsing failed
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
	 * @throws ParserConfigurationException if parsing failed
	 */
	public <E extends Enum<E>> String writeEnumNames(final Class<E> enumClass) throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for (E e : enumClass.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			if(e instanceof EnumDisplayNames) {
				nameElt.setAttribute("displayName", ((EnumDisplayNames)e).getDisplayName());
			}
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing all enum constant names from a given set of a specific enum class.
	 * 
	 * @param chosenConstants            
	 *            The constants to be printed
	 * @param <E>
	 *            An enum subclass type.
	 * @return The document.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public <E extends Enum<E>> String writeSpecificEnumNames(Set<E> chosenConstants) throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for (E e : chosenConstants) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			if(e instanceof EnumDisplayNames) {
				nameElt.setAttribute("displayName", ((EnumDisplayNames)e).getDisplayName());
			}
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
	 * @throws ParserConfigurationException if parsing failed
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
	
	/**
	 * Creates an XML containing the results of a precision calculation.
	 * 
	 * @param maps The list of centrality maps whose precision was calculated.
	 * @param precisionVector The vector of precision values.
	 * @return The XML document as a String.
	 * @throws ParserConfigurationException if parsing failed
	 */
	public String writePrecisionResult(List<CentralityMap> maps, double[] precisionVector) throws ParserConfigurationException {
		Document doc = getDocument();
		Element mapsElt = doc.createElement("CentralityMaps");
		doc.appendChild(mapsElt);
		for(int i = 0; i < maps.size(); i++) {
			CentralityMap map = maps.get(i);
			Element mapElt = doc.createElement("CentralityMap");
			mapsElt.appendChild(mapElt);
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(map.getName()));
			mapElt.appendChild(nameElt);
			Element mapIdElt = doc.createElement("CentralityMapId");
			mapIdElt.appendChild(doc.createTextNode(map.getKey()));	//done
			mapElt.appendChild(mapIdElt);
			Element precisionElt = doc.createElement("Precision");
			mapElt.appendChild(precisionElt);
			Element precisionValueElt = doc.createElement("Value");
			precisionValueElt.appendChild(doc.createTextNode(Double.toString(precisionVector[i])));
			precisionElt.appendChild(precisionValueElt);
		}	
		return writeDoc(doc);
	}
}
