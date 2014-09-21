package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.adapters.AdapterException;
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
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages different request-related tasks for the Service Class.
 * Mainly in charge of simple IO tasks and of creating entity managers for persistence purposes.
 * @author Sebastian
 *
 */
public class RequestHandler {
	
	/**
	 * Default name of the persistence unit used for the creation of entity managers.
	 */
	private static final String defaultPersistenceUnitName = "ocd";
	
	/**
	 * The factory used for the creation of entity managers.
	 */
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory(defaultPersistenceUnitName);
	
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
	 * The factory used for creating graph output adapters.
	 */
	private static GraphOutputAdapterFactory graphOutputAdapterFactory = new GraphOutputAdapterFactory();
	
	/**
	 * The factory used for creating cover input adapters.
	 */
	private static CoverInputAdapterFactory coverInputAdapterFactory = new CoverInputAdapterFactory();
	
	/**
	 * The factory used for creating graph input adapters.
	 */
	private static GraphInputAdapterFactory graphInputAdapterFactory = new GraphInputAdapterFactory();
	
	/**
	 * Sets the persistence unit for entity managers produced by any request handler.
	 * @param persistenceUnitName The name of the persistence unit.
	 */
	public static void setPersistenceUnit(String persistenceUnitName) {
		emf = Persistence.createEntityManagerFactory(persistenceUnitName);
	}
	
	/**
	 * Creates a new instance.
	 * Also initiates the database connection.
	 */
	public RequestHandler() {
		/*
		 * Init database connection
		 */		
		EntityManager em = emf.createEntityManager();
		em.close();
	}
	
	/**
	 * Creates a new log entry based on an exception.
	 * @param level The level of the entry.
	 * @param message The entry message.
	 * @param e The exception.
	 */
	public synchronized void log(Level level, String message, Exception e) {
		log.log(level, message, e);
	}
	
	/**
	 * Creates a new log entry.
	 * @param level The entry message.
	 * @param message The entry message.
	 */
	public synchronized void log(Level level, String message) {
		log.log(level, message);
	}
	
	/**
	 * Creates a new entity manager.
	 * @return The entity manager.
	 */
	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	/**
	 * Returns an error in xml format.
	 * @param error The error.
	 * @param errorMessage An error Message
	 * @return The xml error.
	 */
	/*
	 * Note that this XML document is created "manually" in order to omit any additional exceptions.
	 */
	public String writeError(Error error, String errorMessage) {
		if(errorMessage == null) {
			errorMessage = "";
		}
		return "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
			+ "<Error>"
			+ "<Id>"+ error.getId() +"</Id>"
			+ "<Name>"+ error.toString() +"</Name>"
			+ "<Message>"+ errorMessage +"</Message>"
			+ "</Error>";
	}

	/**
	 * Transforms a parameter xml into a parameter map.
	 * @param content A parameter xml.
	 * @return The corresponding parameter map.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public Map<String, String> parseParameters(String content) throws SAXException, IOException, ParserConfigurationException {
		Map<String, String> parameters = new HashMap<String, String>();
		Document doc = this.parseDocument(content);
		NodeList parameterElts = doc.getElementsByTagName("Parameter");
		for(int i=0; i<parameterElts.getLength(); i++) {
			Node node = parameterElts.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
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
	 * @param parameters A parameter mapping from the name of each parameter to the corresponding value.
	 * @return The corresponding parameter xml.
	 * @throws ParserConfigurationException
	 */
	public String writeParameters(Map<String, String> parameters) throws ParserConfigurationException {
		Document doc = getDocument();
		Element paramsElt = doc.createElement("Parameters");
		for(Map.Entry<String, String> entry: parameters.entrySet()) {
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
	 * @param graphs The graphs.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeGraphIds(List<CustomGraph> graphs) throws ParserConfigurationException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for(int i=0; i<graphs.size(); i++) {
			graphsElt.appendChild(getIdElt(graphs.get(i), doc));
		}
		doc.appendChild(graphsElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing multiple cover ids.
	 * @param covers The covers.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeCoverIds(List<Cover> covers) throws ParserConfigurationException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for(int i=0; i<covers.size(); i++) {
			coversElt.appendChild(getIdElt(covers.get(i), doc));
		}
		doc.appendChild(coversElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing meta information about multiple graphs.
	 * @param graphs The graphs.
	 * @return The document.
	 * @throws AdapterException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeGraphMetas(List<CustomGraph> graphs) throws AdapterException, ParserConfigurationException, IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for(CustomGraph graph : graphs) {
			String metaDocStr = writeGraph(graph, GraphOutputFormat.META_XML);
			Node metaDocNode = parseDocumentToNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			graphsElt.appendChild(importNode);
		}
		doc.appendChild(graphsElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing meta information about multiple covers.
	 * @param covers The covers.
	 * @return The document.
	 * @throws AdapterException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeCoverMetas(List<Cover> covers) throws AdapterException, ParserConfigurationException, IOException, SAXException, InstantiationException, IllegalAccessException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for(Cover cover : covers) {
			String metaDocStr = writeCover(cover, CoverOutputFormat.META_XML);
			Node metaDocNode = parseDocumentToNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			coversElt.appendChild(importNode);
		}
		doc.appendChild(coversElt);
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing the id of a single graph.
	 * @param graph The graph.
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
	 * @param cover The cover.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public String writeId(Cover cover) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(cover, doc));
		return writeDoc(doc);
	}
	
	/**
	 * Creates an XML document containing the id of a single metric log.
	 * @param metricLog The metric log.
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
	 * @param graph The graph.
	 * @param outputFormat The format.
	 * @return The graph output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeGraph(CustomGraph graph, GraphOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		GraphOutputAdapter adapter = graphOutputAdapterFactory.getInstance(outputFormat);
    	Writer writer = new StringWriter();
    	adapter.setWriter(writer);
		adapter.writeGraph(graph);
		return writer.toString();
	}
	
	/**
	 * Creates a cover output in a specified format.
	 * @param cover The cover.
	 * @param outputFormat The format.
	 * @return The cover output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public String writeCover(Cover cover, CoverOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		Writer writer = new StringWriter();
		CoverOutputAdapter adapter = coverOutputAdapterFactory.getInstance(outputFormat);
    	adapter.setWriter(writer);
		adapter.writeCover(cover);
		return writer.toString();
	}
	
	/**
	 * Parses a graph input using a specified format.
	 * @param contentStr The graph input.
	 * @param inputFormat The format.
	 * @return The graph.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public CustomGraph parseGraph(String contentStr, GraphInputFormat inputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		GraphInputAdapter adapter = graphInputAdapterFactory.getInstance(inputFormat);
	    Reader reader = new StringReader(contentStr);
	    adapter.setReader(reader);
		return adapter.readGraph();
	}
	
	/**
	 * Parses a cover input using a specified format.
	 * @param contentStr The cover input.
	 * @param graph The graph that the cover is based on.
	 * @param inputFormat The format.
	 * @return The cover.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Cover parseCover(String contentStr, CustomGraph graph, CoverInputFormat inputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		CoverInputAdapter adapter = coverInputAdapterFactory.getInstance(inputFormat);
	    Reader reader = new StringReader(contentStr);
	    adapter.setReader(reader);
		return adapter.readCover(graph);
	}
	
	/**
	 * Returns an XML element node representing the id of a graph.
	 * @param graph The graph.
	 * @param doc The document to create the element node for.
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
	 * @param cover The cover.
	 * @param doc The document to create the element node for.
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
	 * Returns an XML element node representing the id of a metric log.
	 * @param metricLog The metric log.
	 * @param doc The document to create the element node for.
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
	 * @param doc The document.
	 * @return The document string.
	 */
	protected String writeDoc(Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(doc);
	}
	
	/**
	 * Creates an empty document.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	protected Document getDocument() throws ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.newDocument();
	}
	
	/**
	 * Transforms an XML document in string form into an actual XML node.
	 * @param docString The document string.
	 * @return The node.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	protected Node parseDocumentToNode(String docString) throws ParserConfigurationException, IOException, SAXException {
		Document doc = parseDocument(docString);
		return doc.getDocumentElement();
	}
	
	/**
	 * Parses an XML document in string form into an actual XML document.
	 * @param docString The document string.
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
	 * Parses a string into a boolean value. Only the words TRUE and FALSE are accepted, ignoring the letter cases though.
	 * @param valueStr The value in string format.
	 * @return The boolean value.
	 */
	public boolean parseBoolean(String valueStr) {
		boolean value = Boolean.parseBoolean(valueStr);
		if(!value) {
			 if(!(valueStr).matches("(?iu)false")) {
				 throw new IllegalArgumentException();
			 }
		}
		return value;
	}
	
	/**
	 * Creates an XML document containing all enum constant names of a given enum class.
	 * @param enumClass The class object of the corresponding enum class.
	 * @param <E> An enum subclass type.
	 * @return The document.
	 * @throws ParserConfigurationException
	 */
	public <E extends Enum<E>> String writeEnumNames(final Class<E> enumClass) throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(E e : enumClass.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
	/**
	 * Parses a single string into a list of strings by splitting on the "-" delimiter.
	 * Intended for parsing multiple values passed as single request query parameter.
	 * @param paramStr A string (possibly containing) "-" delimiters.
	 * @return The string list.
	 */
	public List<String> parseQueryMultiParam(String paramStr) {
		return Arrays.asList(paramStr.split("-"));
	}
	
}
