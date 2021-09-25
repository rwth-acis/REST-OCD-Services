package i5.las2peer.services.ocd.adapters.centralityOutput;

import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import y.base.Node;
import y.base.NodeCursor;

public class DefaultXmlCentralityOutputAdapter extends AbstractCentralityOutputAdapter {

	@Override
	public void writeCentralityMap(CentralityMap map) throws AdapterException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element mapElt = doc.createElement("CentralityMap");
			doc.appendChild(mapElt);
			writeMetaInfo(map, doc, mapElt);
			/*
			 * Centrality Values
			 */
			Element centralityValuesElt = doc.createElement("CentralityValues");
			if(map.getCreationMethod().getStatus() == ExecutionStatus.COMPLETED) {
				NodeCursor nodes = map.getGraph().nodes();
				Node node;
				while(nodes.ok()) {
					node = nodes.node();
					Element nodeElt = doc.createElement("Node");
					Element nodeIdElt = doc.createElement("Name");
					nodeIdElt.appendChild(doc.createTextNode(map.getGraph().getNodeName(node)) );
					nodeElt.appendChild(nodeIdElt);
					Element nodeValueElt = doc.createElement("Value");
					nodeValueElt.appendChild(doc.createTextNode( ((Double)map.getNodeValue(node)).toString()) );
					nodeElt.appendChild(nodeValueElt);
					centralityValuesElt.appendChild(nodeElt);
					nodes.next();
				}
			}
			mapElt.appendChild(centralityValuesElt);
			xmlOutput(doc);
		}
		catch(Exception e) {
			throw new AdapterException(e);
		}
	}
	
	public void writeCentralityMapTopNodes(CentralityMap map, int k) throws AdapterException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element mapElt = doc.createElement("CentralityMap");
			doc.appendChild(mapElt);
			writeMetaInfo(map, doc, mapElt);
			/*
			 * Centrality Values
			 */
			Element centralityValuesElt = doc.createElement("CentralityValues");
			if(map.getCreationMethod().getStatus() == ExecutionStatus.COMPLETED) {
				List<String> topNodesList = map.getTopNodes(k);
				for(String nodeName : topNodesList) {
					Element nodeElt = doc.createElement("Node");
					Element nodeIdElt = doc.createElement("Name");
					nodeIdElt.appendChild(doc.createTextNode(nodeName) );
					nodeElt.appendChild(nodeIdElt);
					Element nodeValueElt = doc.createElement("Value");
					nodeValueElt.appendChild(doc.createTextNode( ((Double)map.getNodeValue(nodeName)).toString()) );
					nodeElt.appendChild(nodeValueElt);
					centralityValuesElt.appendChild(nodeElt);
				}
			}
			mapElt.appendChild(centralityValuesElt);
			xmlOutput(doc);
		}
		catch(Exception e) {
			throw new AdapterException(e);
		}
	}
	
	private void writeMetaInfo(CentralityMap map, Document doc, Element mapElt) {
		/*
		 * Basic Attributes
		 */
		Element nameElt = doc.createElement("Name");
		nameElt.appendChild(doc.createTextNode(map.getName()));
		mapElt.appendChild(nameElt);
		Element idElt = doc.createElement("Id");
		Element mapIdElt = doc.createElement("CentralityMapId");
		mapIdElt.appendChild(doc.createTextNode(Long.toString(map.getId())));
		idElt.appendChild(mapIdElt);
		Element graphIdElt = doc.createElement("GraphId");
		graphIdElt.appendChild(doc.createTextNode(Long.toString(map.getGraph().getId())));
		idElt.appendChild(graphIdElt);
		mapElt.appendChild(idElt);
		Element graphElt = doc.createElement("Graph");
		Element graphNameElt = doc.createElement("GraphName");
		graphNameElt.appendChild(doc.createTextNode(map.getGraph().getName()));
		graphElt.appendChild(graphNameElt);
		Element graphSizeElt = doc.createElement("GraphSize");
		graphSizeElt.appendChild(doc.createTextNode(Integer.toString(map.getGraph().nodeCount())));
		graphElt.appendChild(graphSizeElt);
		mapElt.appendChild(graphElt);
		/*
		 * Creation Method
		 */
		Element creationMethodElt = doc.createElement("CreationMethod");
		Element creationMethodTypeElt = doc.createElement("Type");
		creationMethodTypeElt.appendChild(doc.createTextNode(map.getCreationMethod().getCreationType().name()));
		creationMethodElt.appendChild(creationMethodTypeElt);
		/*
		 * Parameters
		 */
		Element creationMethodParameters = doc.createElement("Parameters");
		Map<String, String> parameters = map.getCreationMethod().getParameters();
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
		creationMethodStatusElt.appendChild(doc.createTextNode(map.getCreationMethod().getStatus().name()));
		creationMethodElt.appendChild(creationMethodStatusElt);
		/*
		 * Execution Time
		 */
		Element creationMethodExecutionTimeElt = doc.createElement("ExecutionTime");
		creationMethodExecutionTimeElt.appendChild(doc.createTextNode(Long.toString(map.getCreationMethod().getExecutionTime())));
		creationMethodElt.appendChild(creationMethodExecutionTimeElt);
		
		mapElt.appendChild(creationMethodElt);
	}
	
	private void xmlOutput(Document doc) throws TransformerException {
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

}
