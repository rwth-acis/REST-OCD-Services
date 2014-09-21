package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.algorithms.CoverCreationType;
import i5.las2peer.services.ocd.benchmarks.GraphCreationType;
import i5.las2peer.services.ocd.metrics.OcdMetricType;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manages different request-related tasks for the Service Class particularly for the OCD service.
 * Mainly in charge of simple IO tasks and of creating entity managers for persistence purposes.
 * @author Sebastian
 *
 */
public class OcdRequestHandler extends RequestHandler {

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
			if(e.isAlgorithm()) {
				Element nameElt = doc.createElement("Name");
				nameElt.appendChild(doc.createTextNode(e.name()));
				namesElt.appendChild(nameElt);
			}
		}
		doc.appendChild(namesElt);
		return writeDoc(doc);
	}
	
}
