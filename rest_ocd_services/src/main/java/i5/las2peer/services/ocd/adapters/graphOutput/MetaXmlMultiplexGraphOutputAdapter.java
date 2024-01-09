package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A graph output adapter for the meta XML format.
 * The output contains meta information about the graph in XML format, but not the actual graph structure or other node or edge related meta data.
 * @author Sebastian
 *
 */
public class MetaXmlMultiplexGraphOutputAdapter extends AbstractMultiplexGraphOutputAdapter {

	@Override
	public void writeGraph(MultiplexGraph graph) throws AdapterException {
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
			graphIdElt.appendChild(doc.createTextNode(graph.getKey()));
			graphElt.appendChild(graphIdElt);
			Element graphNameElt = doc.createElement("Name");
			graphNameElt.appendChild(doc.createTextNode(graph.getName()));
			graphElt.appendChild(graphNameElt);
			Element graphNodeCountElt = doc.createElement("NodeCount");
			graphNodeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getNodeCount())));
			graphElt.appendChild(graphNodeCountElt);
			Element graphEdgeCountElt = doc.createElement("EdgeCount");
			graphEdgeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getEdgeCount())));
			graphElt.appendChild(graphEdgeCountElt);
			Element graphLayerCountElt = doc.createElement("LayerCount");
			graphLayerCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getLayerCount())));
			graphElt.appendChild(graphLayerCountElt);
			/*
			 * Graph Types
			 */
			Element typesElt = doc.createElement("Types");
			for(GraphType type : graph.getTypes()) {
				Element typeElt = doc.createElement("Type");
				typeElt.appendChild(doc.createTextNode(type.name()));
				typeElt.setAttribute("displayName", type.getDisplayName());
				typesElt.appendChild(typeElt);
			}
			graphElt.appendChild(typesElt);
			/*
			 * Creation Method
			 */
			Element creationMethodElt = doc.createElement("CreationMethod");
			Element creationMethodTypeElt = doc.createElement("Type");
			creationMethodTypeElt.appendChild(doc.createTextNode(graph.getCreationMethod().getType().name()));
			creationMethodTypeElt.setAttribute("displayName", graph.getCreationMethod().getType().getDisplayName());
			creationMethodElt.appendChild(creationMethodTypeElt);
			Element creationMethodStatus = doc.createElement("Status");
			creationMethodStatus.appendChild(doc.createTextNode(graph.getCreationMethod().getStatus().name()));
			creationMethodElt.appendChild(creationMethodStatus);
			graphElt.appendChild(creationMethodElt);
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
