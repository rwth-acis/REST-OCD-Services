package i5.las2peer.services.ocd.adapters.graphOutput;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

public class PropertiesXmlGraphOutputAdapter extends AbstractGraphOutputAdapter {

	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element graphElt = doc.createElement("Properties");
			doc.appendChild(graphElt);

			Element graphIdElt = doc.createElement("Id");
			graphIdElt.appendChild(doc.createTextNode(graph.getId()));
			graphElt.appendChild(graphIdElt);

			Element graphNameElt = doc.createElement("Name");			
			graphNameElt.appendChild(doc.createTextNode(graph.getName()));
			graphElt.appendChild(graphNameElt);

			Element graphNodeCountElt = doc.createElement("Size");
			graphNodeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getNodeCount())));
			graphElt.appendChild(graphNodeCountElt);

			Element graphEdgeCountElt = doc.createElement("Links");
			if (graph.isDirected()) {
				graphEdgeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getEdgeCount())));
			} else {
				graphEdgeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getEdgeCount() / 2)));
			}
			graphElt.appendChild(graphEdgeCountElt);

			for (int i=1; i< GraphProperty.values().length; i++) {
				GraphProperty property = GraphProperty.values()[i];
				Element graphPropertyElt = doc.createElement(property.humanRead());
				graphPropertyElt.appendChild(doc.createTextNode(Double.toString(graph.getProperty(property))));
				graphElt.appendChild(graphPropertyElt);
			}

			/*
			 * XML output
			 */
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource domSource = new DOMSource(doc);
			StreamResult streamResult = new StreamResult(this.writer);
			transformer.transform(domSource, streamResult);
		} catch (Exception e) {
			throw new AdapterException(e);
		}

	}

}
