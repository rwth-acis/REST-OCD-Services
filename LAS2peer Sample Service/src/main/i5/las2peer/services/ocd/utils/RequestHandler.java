package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.ServiceClass;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.graph.CustomGraph;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
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
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class RequestHandler {
	
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("ocd");
	
	private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	
	private static final Logger log = Logger.getLogger(ServiceClass.class.getName());
	
	/*
	 * Class Initialization
	 */
	static {
		/*
		 * Init database connection
		 */		
		EntityManager em = emf.createEntityManager();
		em.close();
		/*
		 * Init log
		 */
		try {
			FileHandler fh = new FileHandler("ocd\\log\\OcdServiceLog", true);
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		} catch (SecurityException | IOException e) {
		}
	}
	
	public RequestHandler() {
	}
	
	public void log(Level level, String message, Exception e) {
		log.log(level, message, e);
	}
	
	public void log(Level level, String message) {
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
	
	public String getConfirmationXml() throws ParserConfigurationException {
		Document doc = getDocument();
		doc.appendChild(doc.createElement("Confirmation"));
		return getDocAsString(doc);
	}
	
	public String getIds(List<CustomGraph> graphs) throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element graphsElt = doc.createElement("Graphs");
		for(int i=0; i<graphs.size(); i++) {
			graphsElt.appendChild(getIdElt(graphs.get(i), doc));
		}
		doc.appendChild(graphsElt);
		return getDocAsString(doc);
	}
	
	public String getId(CustomGraph graph) throws ParserConfigurationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.appendChild(getIdElt(graph, doc));
		return getDocAsString(doc);
	}
	
	public String getGraph(CustomGraph graph, GraphOutputAdapter adapter) throws AdapterException {
    	Writer writer = new StringWriter();
    	adapter.setWriter(writer);
		adapter.writeGraph(graph);
		return writer.toString();
	}
	
	private Node getIdElt(CustomGraph graph, Document doc) {
		Element graphElt = doc.createElement("Graph");
		Element graphIdElt = doc.createElement("Id");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(graph.getId())));
		graphElt.appendChild(graphIdElt);
		return graphElt;
	}
	
	private String getDocAsString(Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    return lsSerializer.writeToString(doc);
	}
	
	public Document getDocument() throws ParserConfigurationException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		return builder.newDocument();
	}
	

}
