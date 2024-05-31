package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CLCMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MetaXmlCLCMetaOutputAdapter extends AbstractCLCMetaOutputAdapter{

    @Override
    public void writeCLC(CLCMeta clcMeta) throws AdapterException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element clcElt = doc.createElement("CLC");
            doc.appendChild(clcElt);

            // GRAPH
            Element graphElt = doc.createElement("Graph");
            Element graphNameElt = doc.createElement("Name");
            graphNameElt.appendChild(doc.createTextNode(clcMeta.getGraphName()));
            graphElt.appendChild(graphNameElt);

            clcElt.appendChild(graphElt);

            // COVER
            Element coverElt = doc.createElement("Cover");
            Element coverNameElt = doc.createElement("Name");
            coverNameElt.appendChild(doc.createTextNode(clcMeta.getCoverName()));
            coverElt.appendChild(coverNameElt);

            clcElt.appendChild(coverElt);

            // ID
            Element idElt = doc.createElement("Id");

            Element graphIdElt = doc.createElement("GraphId");
            graphIdElt.appendChild(doc.createTextNode(clcMeta.getGraphKey()));
            idElt.appendChild(graphIdElt);

            Element coverIdElt = doc.createElement("CoverId");
            coverIdElt.appendChild(doc.createTextNode(clcMeta.getCoverKey()));
            idElt.appendChild(coverIdElt);

            Element clcIdElt = doc.createElement("ClcId");
            clcIdElt.appendChild(doc.createTextNode(clcMeta.getKey()));
            idElt.appendChild(clcIdElt);

            clcElt.appendChild(idElt);
            // NAME
            Element clcNameElt = doc.createElement("Name");
            clcNameElt.appendChild(doc.createTextNode(clcMeta.getName()));

            clcElt.appendChild(clcNameElt);

            // CREATION METHOD
            Element creationMethodElt = doc.createElement("CreationMethod");

            Element creationMethodTypeElt = doc.createElement("Type");
            creationMethodTypeElt.appendChild(doc.createTextNode(clcMeta.getCreationTypeName()));
            creationMethodElt.appendChild(creationMethodTypeElt);
            creationMethodElt.setAttribute("displayName", clcMeta.getCreationTypeDisplayName());

            Element creationMethodStatusElt = doc.createElement("Status");
            creationMethodStatusElt.appendChild(doc.createTextNode(clcMeta.getCreationStatusName()));
            creationMethodElt.appendChild(creationMethodStatusElt);

            clcElt.appendChild(creationMethodElt);

            // EVENTS
            //Element eventCountElt = doc.createElement("EventCount");
            //eventCountElt.appendChild(doc.createTextNode(Long.toString(clcMeta.getNumberOfEvents())));
            //clcElt.appendChild(eventCountElt);

            // XML Output
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(this.writer);
            transformer.transform(domSource, streamResult);
        }catch (Exception e) {
            throw new AdapterException(e);
        }
    }
}
