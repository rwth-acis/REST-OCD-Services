package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.MetricLog;

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
import y.base.NodeCursor;


public class DefaultXmlCoverOutputAdapter extends AbstractCoverOutputAdapter {

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
			Element coverNameElt = doc.createElement("Name");
			coverNameElt.appendChild(doc.createTextNode(cover.getName()));
			coverElt.appendChild(coverNameElt);
			Element coverDescrElt = doc.createElement("Description");
			coverDescrElt.appendChild(doc.createTextNode(cover.getDescription()));
			coverElt.appendChild(coverDescrElt);
			CustomGraph graph = cover.getGraph();
			Element graphElt = doc.createElement("Graph");
			Element graphNameElt = doc.createElement("Name");
			graphNameElt.appendChild(doc.createTextNode(graph.getName()));
			graphElt.appendChild(graphNameElt);
			coverElt.appendChild(graphElt);
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
			Element algoTypeElt = doc.createElement("Type");
			algoTypeElt.appendChild(doc.createTextNode(cover.getAlgorithm().getType().name()));
			algorithmElt.appendChild(algoTypeElt);
			Element algoStatusElt = doc.createElement("Status");
			algoStatusElt.appendChild(doc.createTextNode(cover.getAlgorithm().getStatus().name()));
			algorithmElt.appendChild(algoStatusElt);
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
			communityCountElement.appendChild(doc.createTextNode(Integer.toString(cover.communityCount())));
			coverElt.appendChild(communityCountElement);
			Element communitiesElt = doc.createElement("Communities");
			for(int i=0; i<cover.communityCount(); i++) {
				Element communityElt = doc.createElement("Community");
				Element communityIdElt = doc.createElement("Index");
				communityIdElt.appendChild(doc.createTextNode(Integer.toString(i)));
				communityElt.appendChild(communityIdElt);
				Element communityNameElt = doc.createElement("Name");
				communityNameElt.appendChild(doc.createTextNode(cover.getCommunityName(i)));
				communityElt.appendChild(communityNameElt);
				/*
				 * Community Color
				 */
				Element communityColorElt = doc.createElement("Color");
				Element communityColorRedElt = doc.createElement("Red");
				communityColorRedElt.appendChild(doc.createTextNode(Integer.toString(cover.getCommunityColor(i).getRed())));
				communityColorElt.appendChild(communityColorRedElt);
				Element communityColorGreenElt = doc.createElement("Green");
				communityColorGreenElt.appendChild(doc.createTextNode(Integer.toString(cover.getCommunityColor(i).getGreen())));
				communityColorElt.appendChild(communityColorGreenElt);
				Element communityColorBlueElt = doc.createElement("Blue");
				communityColorBlueElt.appendChild(doc.createTextNode(Integer.toString(cover.getCommunityColor(i).getBlue())));
				communityColorElt.appendChild(communityColorBlueElt);
				communityElt.appendChild(communityColorElt);
				/*
				 * Community Memberships
				 */
				Element membershipsElt = doc.createElement("Memberships");
				NodeCursor nodes = cover.getGraph().nodes();
				Node node;
				while(nodes.ok()) {
					node = nodes.node();
					Element membershipElt = doc.createElement("Membership");
					Element memberIdElt = doc.createElement("Name");
					memberIdElt.appendChild(doc.createTextNode(graph.getNodeName(node)));
					membershipElt.appendChild(memberIdElt);
					Element belongingFactorElt = doc.createElement("BelongingFactor");
					belongingFactorElt.appendChild(doc.createTextNode(String.format("%.5f\n", cover.getBelongingFactor(node, i))));
					membershipElt.appendChild(belongingFactorElt);
					membershipsElt.appendChild(membershipElt);
					nodes.next();
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