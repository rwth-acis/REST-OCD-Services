package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphTimed;
import i5.las2peer.services.ocd.graphs.CustomGraphTimedMeta;
import i5.las2peer.services.ocd.graphs.GraphType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * A graph output adapter for the meta XML format.
 * The output contains meta information about the graph in XML format, but not the actual graph structure or other node or edge related meta data.
 * @author Sebastian
 */
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
			graphIdElt.appendChild(doc.createTextNode(graph.getKey()));
			graphElt.appendChild(graphIdElt);
			Element graphNameElt = doc.createElement("Name");
			graphNameElt.appendChild(doc.createTextNode(graph.getName()));
			graphElt.appendChild(graphNameElt);
//			Element graphDescrElt = doc.createElement("Description");
//			graphDescrElt.appendChild(doc.createTextNode(graph.getDescription()));
//			graphElt.appendChild(graphDescrElt);
			Element graphNodeCountElt = doc.createElement("NodeCount");
			graphNodeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getNodeCount())));
			graphElt.appendChild(graphNodeCountElt);
			Element graphEdgeCountElt = doc.createElement("EdgeCount");
			graphEdgeCountElt.appendChild(doc.createTextNode(Integer.toString(graph.getEdgeCount())));
			graphElt.appendChild(graphEdgeCountElt);

			if (graph instanceof CustomGraphTimed) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				Element graphStartDateElt = doc.createElement("StartDate");
				graphStartDateElt.appendChild(doc.createTextNode(dateFormat.format(((CustomGraphTimed) graph).getStartDate())));
				graphElt.appendChild(graphStartDateElt);
				Element graphEndDateElt = doc.createElement("EndDate");
				graphEndDateElt.appendChild(doc.createTextNode(dateFormat.format(((CustomGraphTimed) graph).getEndDate())));
				graphElt.appendChild(graphEndDateElt);
			}

			Element graphExtraInfoElt = doc.createElement("ExtraInfo");
			String xmlConformExtraInfo = graph.getExtraInfo().toJSONString()
					.replaceAll("\"", "&quot;").replaceAll("'", "&apos;")
					.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
					.replaceAll("&", "&amp;");
			graphExtraInfoElt.appendChild(doc.createTextNode(xmlConformExtraInfo));
			graphElt.appendChild(graphExtraInfoElt);

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
