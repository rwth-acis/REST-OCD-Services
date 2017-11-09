package i5.las2peer.services.ocd.adapters.centralityOutput;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MetaXmlCentralityOutputAdapter extends AbstractCentralityOutputAdapter {

	@Override
	public void writeCentralityMap(CentralityMap map) throws AdapterException {
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
			/*
			 * Metrics
			 */
			/*Element metricsElt = doc.createElement("Metrics");
			for(int i=0; i<cover.getMetrics().size(); i++) {
				OcdMetricLog metric = cover.getMetrics().get(i);
				Element metricElt = doc.createElement("Metric");
				Element metricIdElt = doc.createElement("Id");
				metricIdElt.appendChild(doc.createTextNode(Long.toString(metric.getId())));
				metricElt.appendChild(metricIdElt);
				Element metricTypeElt = doc.createElement("Type");
				metricTypeElt.appendChild(doc.createTextNode(metric.getType().name()));
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
				/*Element metricParamsElt = doc.createElement("Parameters");
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
			mapElt.appendChild(metricsElt);
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
