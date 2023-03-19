package i5.las2peer.services.ocd.adapters.graphSequenceOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraphMeta;
import i5.las2peer.services.ocd.graphs.GraphSequence;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.List;

public class MetaXmlGraphSequenceOutputAdapter extends AbstractGraphSequenceOutputAdapter {

    @Override
    public void writeGraphSequence(Database db, GraphSequence sequence) throws AdapterException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element graphSequenceElt = doc.createElement("GraphSequence");
            doc.appendChild(graphSequenceElt);
            /*
             * Basic Attributes
             */
            Element graphSequenceNameElt = doc.createElement("Name");
            graphSequenceNameElt.appendChild(doc.createTextNode(sequence.getName()));
            graphSequenceElt.appendChild(graphSequenceNameElt);

            Element graphSequenceTimeOrderedElt = doc.createElement("TimeOrdered");
            graphSequenceTimeOrderedElt.appendChild(doc.createTextNode(Boolean.toString(sequence.getTimeOrdered())));
            graphSequenceElt.appendChild(graphSequenceTimeOrderedElt);

            if(sequence.getTimeOrdered()) {
                Element graphSequenceStartDateElt = doc.createElement("StartDate");
                graphSequenceStartDateElt.appendChild(doc.createTextNode(sequence.getStartDate().toString()));
                graphSequenceElt.appendChild(graphSequenceStartDateElt);

                Element graphSequenceEndDateElt = doc.createElement("EndDate");
                graphSequenceEndDateElt.appendChild(doc.createTextNode(sequence.getEndDate().toString()));
                graphSequenceElt.appendChild(graphSequenceEndDateElt);
            }

            /*
             * Contained Graphs
             */
            List<CustomGraphMeta> graphMetas = db.getGraphMetaDataEfficiently(sequence.getUserName(), sequence.getCustomGraphKeys(), List.of(ExecutionStatus.COMPLETED.getId()));
            Element graphsElt = doc.createElement("Graphs");
            for (CustomGraphMeta graphMeta : graphMetas) {
                Element graphElt = doc.createElement("Graph");
                graphElt.setAttribute("Id", graphMeta.getKey());
                Element graphNameElt = doc.createElement("Name");
                graphNameElt.appendChild(doc.createTextNode(graphMeta.getName()));
                graphElt.appendChild(graphNameElt);
                graphsElt.appendChild(graphElt);
            }
            graphSequenceElt.appendChild(graphsElt);

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
