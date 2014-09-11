package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.benchmarks.BenchmarkType;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.MetricType;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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

public class RequestHandler {
	
	private static final String serviceLogFilename = "ocd\\log\\OcdServiceLog";
	
	private static final String defaultPersistenceUnitName = "ocd";
	
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory(defaultPersistenceUnitName);
	
	private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	
	private static final Logger log = Logger.getLogger("Service API");
	
	/**
	 * Sets the persistence unit for all request handlers.
	 * @param persistenceUnitName The name of the persistence unit.
	 */
	public static void setPersistenceUnit(String persistenceUnitName) {
		emf = Persistence.createEntityManagerFactory(persistenceUnitName);
	}
	
	public RequestHandler() {
		/*
		 * Init database connection
		 */		
		EntityManager em = emf.createEntityManager();
		em.close();
		/*
		 * Init log
		 */
		try {
			FileHandler fh = new FileHandler(serviceLogFilename, true);
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		} catch (SecurityException | IOException e) {
		}
	}
	
	public synchronized void log(Level level, String message, Exception e) {
		log.log(level, message, e);
	}
	
	public synchronized void log(Level level, String message) {
		log.log(level, message);
	}
	
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
	 * Note that this xml is created manually in order to omit any additional exceptions.
	 */
	public String getError(Error error, String errorMessage) {
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
	public Map<String, String> readParameters(String content) throws SAXException, IOException, ParserConfigurationException {
		Map<String, String> parameters = new HashMap<String, String>();
		Document doc = this.getDocumentFromString(content);
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
		return this.getDocAsString(doc);
	}
	
	public String getConfirmationXml() throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(doc.createElement("Confirmation"));
		return getDocAsString(doc);
	}
	
	public String getGraphIds(List<CustomGraph> graphs) throws ParserConfigurationException {
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for(int i=0; i<graphs.size(); i++) {
			graphsElt.appendChild(getIdElt(graphs.get(i), doc));
		}
		doc.appendChild(graphsElt);
		return getDocAsString(doc);
	}
	
	public String getCoverIds(List<Cover> covers) throws ParserConfigurationException {
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for(int i=0; i<covers.size(); i++) {
			coversElt.appendChild(getIdElt(covers.get(i), doc));
		}
		doc.appendChild(coversElt);
		return getDocAsString(doc);
	}
	
	public String getGraphMetas(List<CustomGraph> graphs) throws AdapterException, ParserConfigurationException, IOException, SAXException {
		GraphOutputAdapter adapter = GraphOutputFormat.META_XML.getAdapterInstance();
		Document doc = getDocument();
		Element graphsElt = doc.createElement("Graphs");
		for(CustomGraph graph : graphs) {
			String metaDocStr = getGraph(graph, adapter);
			Node metaDocNode = getDocumentFromStringAsNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			graphsElt.appendChild(importNode);
		}
		doc.appendChild(graphsElt);
		return getDocAsString(doc);
	}
	
	public String getCoverMetas(List<Cover> covers) throws AdapterException, ParserConfigurationException, IOException, SAXException {
		CoverOutputAdapter adapter = CoverOutputFormat.META_XML.getAdapterInstance();
		Document doc = getDocument();
		Element coversElt = doc.createElement("Covers");
		for(Cover cover : covers) {
			String metaDocStr = getCover(cover, adapter);
			Node metaDocNode = getDocumentFromStringAsNode(metaDocStr);
			Node importNode = doc.importNode(metaDocNode, true);
			coversElt.appendChild(importNode);
		}
		doc.appendChild(coversElt);
		return getDocAsString(doc);
	}
	
	public String getId(CustomGraph graph) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(graph, doc));
		return getDocAsString(doc);
	}
	
	public String getId(Cover cover) throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(getIdElt(cover, doc));
		return getDocAsString(doc);
	}
	
	public String getGraph(CustomGraph graph, GraphOutputAdapter adapter) throws AdapterException {
    	Writer writer = new StringWriter();
    	adapter.setWriter(writer);
		adapter.writeGraph(graph);
		return writer.toString();
	}
	
	public String getCover(Cover cover, CoverOutputAdapter adapter) throws AdapterException {
		Writer writer = new StringWriter();
    	adapter.setWriter(writer);
		adapter.writeCover(cover);
		return writer.toString();
	}
	
	private Node getIdElt(CustomGraph graph, Document doc) {
		Element graphElt = doc.createElement("Graph");
		Element graphIdElt = doc.createElement("Id");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(graph.getId())));
		graphElt.appendChild(graphIdElt);
		return graphElt;
	}
	
	private Node getIdElt(Cover cover, Document doc) {
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
	
	private String getDocAsString(Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(doc);
	}
	
	private Document getDocument() throws ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.newDocument();
	}
	
	private Node getDocumentFromStringAsNode(String docString) throws ParserConfigurationException, IOException, SAXException {
		Document doc = getDocumentFromString(docString);
		return doc.getDocumentElement();
	}
	
	private Document getDocumentFromString(String docString) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Reader reader = new StringReader(docString);
		Document doc = builder.parse(new InputSource(reader));
		return doc;
	}
	
	public boolean parseBoolean(String valueStr) {
		boolean value = Boolean.parseBoolean(valueStr);
		if(!value) {
			 if(!(valueStr).matches("(?iu)false")) {
				 throw new IllegalArgumentException();
			 }
		}
		return value;
	}
	
	public <E extends Enum<E>> String getEnumNames(final Class<E> enumClass) throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(E e : enumClass.getEnumConstants()) {
			Element nameElt = doc.createElement("Name");
			nameElt.appendChild(doc.createTextNode(e.name()));
			namesElt.appendChild(nameElt);
		}
		doc.appendChild(namesElt);
		return getDocAsString(doc);
	}
	
	public String getStatisticalMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(MetricType e : MetricType.class.getEnumConstants()) {
			if(e.isStatisticalMeasure()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return getDocAsString(doc);
	}
	
	public String getKnowledgeDrivenMeasureNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(MetricType e : MetricType.class.getEnumConstants()) {
			if(e.isKnowledgeDrivenMeasure()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return getDocAsString(doc);
	}
	
	public String getGroundTruthBenchmarkNames() throws ParserConfigurationException {
		Document doc = getDocument();
		Element namesElt = doc.createElement("Names");
		for(BenchmarkType e : BenchmarkType.class.getEnumConstants()) {
			if(e.isGroundTruthBenchmark()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return getDocAsString(doc);
	}
	
	public List<String> getQueryMultiParam(String paramStr) {
		return Arrays.asList(paramStr.split("-"));
	}
	
}
