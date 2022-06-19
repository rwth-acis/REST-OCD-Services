package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CoverMeta;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
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
 * A cover meta information output adapter for the meta XML format. More efficient than MetaXmlCoverOutputAdapter
 * due to not loading full covers/graphs. The output contains meta information about the cover and corresponding
 * graph in XML format, but not the actual cover/graph instances or other node or edge related meta data.
 *
 */
public class MetaXmlCoverMetaOutputAdapter extends AbstractCoverMetaOutputAdapter{
    @Override
    public void writeCover(CoverMeta coverMeta) throws AdapterException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element coverElt = doc.createElement("Cover");
            doc.appendChild(coverElt);
            /*
             * Basic Attributes
             */
            Element idElt = doc.createElement("Id");
            Element coverIdElt = doc.createElement("CoverId");
            coverIdElt.appendChild(doc.createTextNode(Long.toString(coverMeta.getId())));
            idElt.appendChild(coverIdElt);
            Element graphIdElt = doc.createElement("GraphId");
            graphIdElt.appendChild(doc.createTextNode(Long.toString(coverMeta.getGraphId())));
            idElt.appendChild(graphIdElt);
            coverElt.appendChild(idElt);
            Element coverNameElt = doc.createElement("Name");
            coverNameElt.appendChild(doc.createTextNode(coverMeta.getName()));
            coverElt.appendChild(coverNameElt);
//			Element coverDescrElt = doc.createElement("Description");
//			coverDescrElt.appendChild(doc.createTextNode(cover.getDescription()));
//			coverElt.appendChild(coverDescrElt);
            Element graphElt = doc.createElement("Graph");
            Element graphNameElt = doc.createElement("Name");
            graphNameElt.appendChild(doc.createTextNode(coverMeta.getGraphName()));
            graphElt.appendChild(graphNameElt);
            coverElt.appendChild(graphElt);
//			Element lastUpdateElt = doc.createElement("LastUpdate");
//			if(cover.getLastUpdate() != null) {
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//				lastUpdateElt.appendChild(doc.createTextNode(dateFormat.format(cover.getLastUpdate())));
//				coverElt.appendChild(lastUpdateElt);
//			}
            /*
             * Creation Method
             */
            Element creationMethodElt = doc.createElement("CreationMethod");
            Element creationMethodTypeElt = doc.createElement("Type");
            creationMethodTypeElt.appendChild(doc.createTextNode(coverMeta.getCoverCreationLog().getType().name()));
            creationMethodElt.appendChild(creationMethodTypeElt);
            creationMethodElt.setAttribute("displayName", coverMeta.getCoverCreationLog().getType().getDisplayName());
            Element creationMethodStatusElt = doc.createElement("Status");
            creationMethodStatusElt.appendChild(doc.createTextNode(coverMeta.getCoverCreationLog().getStatus().name()));
            creationMethodElt.appendChild(creationMethodStatusElt);
            coverElt.appendChild(creationMethodElt);
            /*
             * Metrics
             */
            Element metricsElt = doc.createElement("Metrics");
            for(int i=0; i<coverMeta.getMetrics().size(); i++) {
                OcdMetricLog metric = coverMeta.getMetrics().get(i);
                Element metricElt = doc.createElement("Metric");
                Element metricIdElt = doc.createElement("Id");
                metricIdElt.appendChild(doc.createTextNode(Long.toString(metric.getId())));
                metricElt.appendChild(metricIdElt);
                Element metricTypeElt = doc.createElement("Type");
                metricTypeElt.appendChild(doc.createTextNode(metric.getType().name()));
                metricTypeElt.setAttribute("displayName", metric.getType().getDisplayName());
                metricElt.appendChild(metricTypeElt);
                Element metricStatusElt = doc.createElement("Status");
                metricStatusElt.appendChild(doc.createTextNode(metric.getStatus().name()));
                metricElt.appendChild(metricStatusElt);
                Element metricValueElt = doc.createElement("Value");
                metricValueElt.appendChild(doc.createTextNode(Double.toString(metric.getValue())));
                metricElt.appendChild(metricValueElt);
                /*
                 * Metric Parameters
                 */
                Element metricParamsElt = doc.createElement("Parameters");
                for(Map.Entry<String, String> entry: metric.getParameters().entrySet()) {
                    Element metricParamElt = doc.createElement("Parameter");
                    Element metricParamNameElt = doc.createElement("Name");
                    metricParamNameElt.appendChild(doc.createTextNode(entry.getKey()));
                    metricParamElt.appendChild(metricParamNameElt);
                    Element metricParamValueElt = doc.createElement("Value");
                    metricParamValueElt.appendChild(doc.createTextNode(entry.getValue()));
                    metricParamElt.appendChild(metricParamValueElt);
                    metricParamsElt.appendChild(metricParamElt);
                }
                metricElt.appendChild(metricParamsElt);
                metricsElt.appendChild(metricElt);
            }
            coverElt.appendChild(metricsElt);
            /*
             * Communities
             */
            Element communityCountElement = doc.createElement("CommunityCount");
            communityCountElement.appendChild(doc.createTextNode(Integer.toString(coverMeta.getNumberOfCommunities())));
            coverElt.appendChild(communityCountElement);
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
