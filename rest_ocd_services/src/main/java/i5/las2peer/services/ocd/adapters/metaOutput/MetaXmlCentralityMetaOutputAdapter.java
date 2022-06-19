package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.centrality.data.CentralityMeta;
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

/**
 * A centrality meta information output adapter for the meta XML format. More efficient than
 * MetaXmlCentralityOutputAdapter due to not loading full centrality details, but only
 * necessary metadata. The output contains meta information about the centrality and corresponding
 * graph in XML format.
 *
 */
public class MetaXmlCentralityMetaOutputAdapter extends AbstractCentralityMetaOutputAdapter{

    @Override
    public void writeCentralityMap(CentralityMeta centralityMeta) throws AdapterException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element mapElt = doc.createElement("CentralityMap");
            doc.appendChild(mapElt);
            /*
             * Basic Attributes
             */
            Element nameElt = doc.createElement("Name");
            nameElt.appendChild(doc.createTextNode(centralityMeta.getCentralityName()));
            mapElt.appendChild(nameElt);
            Element idElt = doc.createElement("Id");
            Element mapIdElt = doc.createElement("CentralityMapId");
            mapIdElt.appendChild(doc.createTextNode(Long.toString(centralityMeta.getCentralityId())));
            idElt.appendChild(mapIdElt);
            Element graphIdElt = doc.createElement("GraphId");
            graphIdElt.appendChild(doc.createTextNode(Long.toString(centralityMeta.getGraphId())));
            idElt.appendChild(graphIdElt);
            mapElt.appendChild(idElt);
            Element graphElt = doc.createElement("Graph");
            Element graphNameElt = doc.createElement("GraphName");
            graphNameElt.appendChild(doc.createTextNode(centralityMeta.getGraphName()));
            graphElt.appendChild(graphNameElt);
            Element graphSizeElt = doc.createElement("GraphSize");
            graphSizeElt.appendChild(doc.createTextNode(Integer.toString(Math.toIntExact(centralityMeta.getGraphSize()))));
            graphElt.appendChild(graphSizeElt);
            mapElt.appendChild(graphElt);
            /*
             * Creation Method
             */
            Element creationMethodElt = doc.createElement("CreationMethod");
            Element creationMethodTypeElt = doc.createElement("Type");
            creationMethodTypeElt.appendChild(doc.createTextNode(centralityMeta.getCentralityCreationLog().getCreationType().name()));
            creationMethodTypeElt.setAttribute("displayName", centralityMeta.getCentralityCreationLog().getCreationType().getDisplayName());
            creationMethodElt.appendChild(creationMethodTypeElt);
            /*
             * Parameters
             */
            Element creationMethodParameters = doc.createElement("Parameters");
            Map<String, String> parameters = centralityMeta.getCentralityCreationLog().getParameters();
            for(String parameter : parameters.keySet()) {
                Element creationMethodParameter = doc.createElement("Parameter");
                Element creationMethodParameterName = doc.createElement("ParameterName");
                creationMethodParameterName.appendChild(doc.createTextNode(parameter));
                Element creationMethodParameterValue = doc.createElement("ParameterValue");
                creationMethodParameterValue.appendChild(doc.createTextNode(parameters.get(parameter)));
                creationMethodParameter.appendChild(creationMethodParameterName);
                creationMethodParameter.appendChild(creationMethodParameterValue);
                creationMethodParameters.appendChild(creationMethodParameter);
            }
            creationMethodElt.appendChild(creationMethodParameters);
            /*
             * Status
             */
            Element creationMethodStatusElt = doc.createElement("Status");
            creationMethodStatusElt.appendChild(doc.createTextNode(centralityMeta.getCentralityCreationLog().getStatus().name()));
            creationMethodElt.appendChild(creationMethodStatusElt);
            /*
             * Execution Time
             */
            Element creationMethodExecutionTimeElt = doc.createElement("ExecutionTime");
            creationMethodExecutionTimeElt.appendChild(doc.createTextNode(Long.toString(centralityMeta.getCentralityCreationLog().getExecutionTime())));
            creationMethodElt.appendChild(creationMethodExecutionTimeElt);

            mapElt.appendChild(creationMethodElt);
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
