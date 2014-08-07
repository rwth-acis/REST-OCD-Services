package i5.las2peer.services.servicePackage.adapters.coverOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.Community;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.metrics.MetricLog;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import y.base.Node;


public class XmlCoverOutputAdapter extends AbstractCoverOutputAdapter {

	@Override
	public void writeCover(Cover cover) throws AdapterException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element coverElt = doc.createElement("Cover");
			doc.appendChild(coverElt);
			/*
			 * Basic Attributes
			 */
			Element coverIdElt = doc.createElement("Id");
			coverIdElt.appendChild(doc.createTextNode(Long.toString(cover.getId())));
			coverElt.appendChild(coverIdElt);
			Element coverNameElt = doc.createElement("Name");
			coverNameElt.appendChild(doc.createTextNode(cover.getName()));
			coverElt.appendChild(coverNameElt);
			Element graphElt = doc.createElement("Graph");
			graphElt.appendChild(doc.createTextNode(Long.toString(cover.getGraph().getId())));
			coverElt.appendChild(graphElt);
			Element coverDescrElt = doc.createElement("Description");
			coverDescrElt.appendChild(doc.createTextNode(cover.getDescription()));
			coverElt.appendChild(coverDescrElt);
			Element lastUpdateElt = doc.createElement("LastUpdate");
			if(cover.getLastUpdate() != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
				lastUpdateElt.appendChild(doc.createTextNode(dateFormat.format(cover.getLastUpdate())));
				coverElt.appendChild(lastUpdateElt);
			}
			/*
			 * Algorithm
			 */
			Element algorithmElt = doc.createElement("Algorithm");
			Element algoIdElt = doc.createElement("Id");
			algoIdElt.appendChild(doc.createTextNode(Long.toString(cover.getAlgorithm().getId())));
			algorithmElt.appendChild(algoIdElt);
			Element algoTypeElt = doc.createElement("Type");
			Element algoTypeId = doc.createElement("Id");
			algoTypeId.appendChild(doc.createTextNode(Integer.toString(cover.getAlgorithm().getType().getId())));
			algoTypeElt.appendChild(algoTypeId);
			Element algoTypeNameElt = doc.createElement("Name");
			algoTypeNameElt.appendChild(doc.createTextNode(cover.getAlgorithm().getType().name()));
			algoTypeElt.appendChild(algoTypeNameElt);
			algorithmElt.appendChild(algoTypeElt);
			/*
			 * Algorithm Parameters
			 */
			Element algoParamsElt = doc.createElement("Parameters");
			for(Map.Entry<String, String> entry: cover.getAlgorithm().getParameters().entrySet()) {
				Element algoParamElt = doc.createElement("Parameter");
				Element algoParamNameElt = doc.createElement("Name");
				algoParamNameElt.appendChild(doc.createTextNode(entry.getKey()));
				algoParamElt.appendChild(algoParamNameElt);
				Element algoParamValueElt = doc.createElement("Value");
				algoParamValueElt.appendChild(doc.createTextNode(entry.getValue()));
				algoParamElt.appendChild(algoParamValueElt);
				algoParamsElt.appendChild(algoParamElt);
			}
			algorithmElt.appendChild(algoParamsElt);
			coverElt.appendChild(algorithmElt);
			/*
			 * Metrics
			 */
			Element metricsElt = doc.createElement("Metrics");
			for(int i=0; i<cover.getMetrics().size(); i++) {
				MetricLog metric = cover.getMetrics().get(i);
				Element metricElt = doc.createElement("Metric");
				Element metricIdElt = doc.createElement("Id");
				metricIdElt.appendChild(doc.createTextNode(Long.toString(metric.getId())));
				metricElt.appendChild(metricIdElt);
				Element metricTypeElt = doc.createElement("Type");
				Element metricTypeId = doc.createElement("Id");
				metricTypeId.appendChild(doc.createTextNode(Integer.toString(metric.getType().getId())));
				metricTypeElt.appendChild(metricTypeId);
				Element metricTypeNameElt = doc.createElement("Name");
				metricTypeNameElt.appendChild(doc.createTextNode(metric.getType().name()));
				metricTypeElt.appendChild(metricTypeNameElt);
				metricElt.appendChild(metricTypeElt);
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
			Element communitiesElt = doc.createElement("Communities");
			for(int i=0; i<cover.getCommunities().size(); i++) {
				Community com = cover.getCommunities().get(i);
				Element communityElt = doc.createElement("Community");
				Element communityIdElt = doc.createElement("Index");
				communityIdElt.appendChild(doc.createTextNode(Integer.toString(i)));
				communityElt.appendChild(communityIdElt);
				Element communityNameElt = doc.createElement("Name");
				communityNameElt.appendChild(doc.createTextNode(com.getName()));
				communityElt.appendChild(communityNameElt);
				/*
				 * Community Color
				 */
				Element communityColorElt = doc.createElement("Color");
				Element communityColorRedElt = doc.createElement("Red");
				communityColorRedElt.appendChild(doc.createTextNode(Integer.toString(com.getColor().getRed())));
				communityColorElt.appendChild(communityColorRedElt);
				Element communityColorGreenElt = doc.createElement("Green");
				communityColorGreenElt.appendChild(doc.createTextNode(Integer.toString(com.getColor().getGreen())));
				communityColorElt.appendChild(communityColorGreenElt);
				Element communityColorBlueElt = doc.createElement("Blue");
				communityColorBlueElt.appendChild(doc.createTextNode(Integer.toString(com.getColor().getBlue())));
				communityColorElt.appendChild(communityColorBlueElt);
				communityElt.appendChild(communityColorElt);
				/*
				 * Community Memberships
				 */
				Element membershipsElt = doc.createElement("Memberships");
				for(Map.Entry<Node, Double> entry : com.getMemberships().entrySet()) {
					Element membershipElt = doc.createElement("Membership");
					Element memberIdElt = doc.createElement("Name");
					memberIdElt.appendChild(doc.createTextNode(Integer.toString(cover.getGraph().getNodeId(entry.getKey()))));
					membershipElt.appendChild(memberIdElt);
					Element belongingFactorElt = doc.createElement("BelongingFactor");
					belongingFactorElt.appendChild(doc.createTextNode(String.format("%.5f\n", entry.getValue())));
					membershipElt.appendChild(belongingFactorElt);
					membershipsElt.appendChild(membershipElt);
				}
				communityElt.appendChild(membershipsElt);
				communitiesElt.appendChild(communityElt);
			}
			coverElt.appendChild(communitiesElt);
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