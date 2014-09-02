package i5.las2peer.services.ocd.adapters.graphOutput;

import java.text.SimpleDateFormat;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.graph.GraphType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MetaXmlGraphOutputAdapter extends AbstractGraphOutputAdapter {

	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element graphElt = doc.createElement("Graph");
			doc.appendChild(graphElt);
			/*
			 * Basic Attributes
			 */
			Element graphIdElt = doc.createElement("Id");
			graphIdElt.appendChild(doc.createTextNode(Long.toString(graph.getId())));
			graphElt.appendChild(graphIdElt);
			Element graphNameElt = doc.createElement("Name");
			graphNameElt.appendChild(doc.createTextNode(graph.getName()));
			graphElt.appendChild(graphNameElt);
			Element graphDescrElt = doc.createElement("Description");
			graphDescrElt.appendChild(doc.createTextNode(graph.getDescription()));
			graphElt.appendChild(graphDescrElt);
			Element graphNodeCountElt = doc.createElement("NodeCount");
			graphNodeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.nodeCount())));
			graphElt.appendChild(graphNodeCountElt);
			Element graphEdgeCountElt = doc.createElement("EdgeCount");
			graphEdgeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.edgeCount())));
			graphElt.appendChild(graphEdgeCountElt);
			Element lastUpdateElt = doc.createElement("LastUpdate");
			if(graph.getLastUpdate() != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
				lastUpdateElt.appendChild(doc.createTextNode(dateFormat.format(graph.getLastUpdate())));
				graphElt.appendChild(lastUpdateElt);
			}
			Element typesElt = doc.createElement("Types");
			for(GraphType type : graph.getTypes()) {
				Element typeElt = doc.createElement("Type");
				typeElt.appendChild(doc.createTextNode(Integer.toString(type.getId())));
				typesElt.appendChild(typeElt);
			}
			graphElt.appendChild(typesElt);
			/*
			 * XML output
			 */
			TransformerFactory transformerFactory = TransformerFactory.newInstance();  
			Transformer transformer = transformerFactory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource domSource = new DOMSource(doc);  
			StreamResult streamResult = new StreamResult(this.writer);  
			transformer.transform(domSource, streamResult);
		}
		catch(Exception e) {
			throw new AdapterException(e);
		}
	}

}
