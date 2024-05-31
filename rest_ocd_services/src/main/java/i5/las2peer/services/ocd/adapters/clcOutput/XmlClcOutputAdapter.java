package i5.las2peer.services.ocd.adapters.clcOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.utils.CommunityEvent;
import i5.las2peer.services.ocd.utils.CommunityEventType;
import i5.las2peer.services.ocd.utils.CommunityLifeCycle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Map;

public class XmlClcOutputAdapter extends AbstractClcOutputAdapter{

    @Override
    public void writeClc(CommunityLifeCycle clc) throws AdapterException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try{
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element clcElt = doc.createElement("CommunityLifeCycle");
            doc.appendChild(clcElt);

            Element clcNameElt = doc.createElement("Name");
            clcNameElt.appendChild(doc.createTextNode(clc.getName()));
            clcElt.appendChild(clcNameElt);

            Element graphElt = doc.createElement("Graph");
            Element graphNameElt = doc.createElement("Name");
            graphNameElt.appendChild(doc.createTextNode(clc.getGraph().getName()));
            graphElt.appendChild(graphNameElt);
            clcElt.appendChild(graphElt);

            Element coverElt = doc.createElement("Cover");
            Element coverNameElt = doc.createElement("Name");
            coverNameElt.appendChild(doc.createTextNode(clc.getCover().getName()));
            coverElt.appendChild(coverNameElt);
            clcElt.appendChild(coverElt);

            /*
             * Creation Method
             */
            Element creationMethodElt = doc.createElement("CreationMethod");
            Element creationMethodTypeElt = doc.createElement("Type");
            creationMethodTypeElt.appendChild(doc.createTextNode(clc.getCover().getCreationMethod().getType().name()));
            creationMethodElt.appendChild(creationMethodTypeElt);
            Element creationMethodStatusElt = doc.createElement("Status");
            creationMethodStatusElt.appendChild(doc.createTextNode(clc.getCover().getCreationMethod().getStatus().name()));
            creationMethodElt.appendChild(creationMethodStatusElt);
            /*
             * Creation Method Parameters
             */
            Element creationMethodParamsElt = doc.createElement("Parameters");
            for(Map.Entry<String, String> entry: clc.getCover().getCreationMethod().getParameters().entrySet()) {
                Element creationMethodParamElt = doc.createElement("Parameter");
                Element creationMethodNameElt = doc.createElement("Name");
                creationMethodNameElt.appendChild(doc.createTextNode(entry.getKey()));
                creationMethodParamElt.appendChild(creationMethodNameElt);
                Element creationMethodParamValueElt = doc.createElement("Value");
                creationMethodParamValueElt.appendChild(doc.createTextNode(entry.getValue()));
                creationMethodParamElt.appendChild(creationMethodParamValueElt);
                creationMethodParamsElt.appendChild(creationMethodParamElt);
            }
            creationMethodElt.appendChild(creationMethodParamsElt);
            clcElt.appendChild(creationMethodElt);

            // Events

            Element eventCountElt = doc.createElement("EventCount");
            eventCountElt.appendChild(doc.createTextNode(Integer.toString(clc.getEvents().size())));
            clcElt.appendChild(eventCountElt);
            Element eventsElt = doc.createElement("Events");
            int i = 0;
            for(CommunityEvent event: clc.getEvents()){
                Element eventElt = doc.createElement("Event");
                Element eventIdElt = doc.createElement("Index");
                eventIdElt.appendChild(doc.createTextNode(Integer.toString(i)));
                eventElt.appendChild(eventIdElt);
                i++;
                Element dateElt = doc.createElement("Timestamp");
                dateElt.appendChild(doc.createTextNode(event.getDate()));
                eventElt.appendChild(dateElt);

                Element typeElt = doc.createElement("EventType");
                typeElt.appendChild(doc.createTextNode(CommunityEventType.lookupType(event.getEventType()).getDisplayName()));
                eventElt.appendChild(typeElt);

                Element communitiesElt = doc.createElement("CommunitiesInvolved");
                for(int com: event.getCommunitiesInvolved()){
                    Element communityElt = doc.createElement("Community");
                    communityElt.appendChild(doc.createTextNode(Integer.toString(com)));
                    communitiesElt.appendChild(communityElt);
                }
                eventElt.appendChild(communitiesElt);

                Element nodesElt = doc.createElement("NodesInvolved");
                for(String node: event.getNodesInvolved()){
                    Element nodeElt = doc.createElement("Node");
                    nodeElt.appendChild(doc.createTextNode(node));
                    nodesElt.appendChild(nodeElt);
                }
                eventElt.appendChild(nodesElt);

                eventsElt.appendChild(eventElt);
            }
            clcElt.appendChild(eventsElt);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(this.writer);
            transformer.transform(domSource, streamResult);
        }catch (Exception e){
            throw new AdapterException(e);
        }
    }
}
