package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraphMeta;
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
 * A graph meta information output adapter for the meta XML format. More efficient than MetaXmlGraphOutputAdapter
 * due to not loading full graphs. The output contains meta information about the graph in XML format, but
 * not the actual graph instance or other node or edge related meta data.
 *
 */
public class MetaXmlGraphMetaOutputAdapter extends AbstractGraphMetaOutputAdapter {

    @Override
    public void writeGraph(CustomGraphMeta graphMeta) throws AdapterException {
        //System.out.println("start: writing Graph in MetaXmlGraphMetaOutputAdapter");
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
            graphIdElt.appendChild(doc.createTextNode(Long.toString(graphMeta.getId())));
            graphElt.appendChild(graphIdElt);
            Element graphNameElt = doc.createElement("Name");
            graphNameElt.appendChild(doc.createTextNode(graphMeta.getName()));
            graphElt.appendChild(graphNameElt);
//			Element graphDescrElt = doc.createElement("Description");
//			graphDescrElt.appendChild(doc.createTextNode(graph.getDescription()));
//			graphElt.appendChild(graphDescrElt);
            Element graphNodeCountElt = doc.createElement("NodeCount");
            graphNodeCountElt.appendChild(doc.createTextNode(Long.toString(graphMeta.getNodeCount())));
            graphElt.appendChild(graphNodeCountElt);
            Element graphEdgeCountElt = doc.createElement("EdgeCount");
            graphEdgeCountElt.appendChild(doc.createTextNode(Long.toString(graphMeta.getEdgeCount())));
            graphElt.appendChild(graphEdgeCountElt);
//			Element lastUpdateElt = doc.createElement("LastUpdate");
//			if(graph.getLastUpdate() != null) {
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//				lastUpdateElt.appendChild(doc.createTextNode(dateFormat.format(graph.getLastUpdate())));
//				graphElt.appendChild(lastUpdateElt);
//			}
            /*
             * Graph Types
             */

            Element typesElt = doc.createElement("Types");
            for(Integer type_id : graphMeta.getTypes()) {

                if(type_id != null) { // case where graph has no types
                    GraphType type = GraphType.lookupType(type_id);
                    Element typeElt = doc.createElement("Type");
                    typeElt.appendChild(doc.createTextNode(type.name()));
                    typeElt.setAttribute("displayName", type.getDisplayName());
                    typesElt.appendChild(typeElt);
                }
            }
            graphElt.appendChild(typesElt);
            /*
             * Creation Method
             */
            Element creationMethodElt = doc.createElement("CreationMethod");
            Element creationMethodTypeElt = doc.createElement("Type");
            creationMethodTypeElt.appendChild(doc.createTextNode(graphMeta.getCreationTypeName()));
            creationMethodTypeElt.setAttribute("displayName", graphMeta.getCreationTypeDisplayName());
            creationMethodElt.appendChild(creationMethodTypeElt);
            Element creationMethodStatus = doc.createElement("Status");
            creationMethodStatus.appendChild(doc.createTextNode(graphMeta.getCreationStatusName()));
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
        //System.out.println("end: writing Graph in MetaXmlGraphMetaOutputAdapter");
    }
}
