package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
//import i5.las2peer.services.ocd.utils.DocIndexer;
import y.base.Edge;
import y.base.Node;

//TODO: Currently only for the youtube graph, make more general
//TODO: Be able to have more Attributes for nodes(at least string id's) and maybe edges(at least type) in general
public class XGMMLGraphInputAdapter extends AbstractGraphInputAdapter {

	public XGMMLGraphInputAdapter() {

	}

	/////////////////
	//// Variables////
	/////////////////

	private String filePath = "ocd/test/input/stackexAcademia.xml";

	private String indexPath = null;

	@Override
	public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

		if (param.containsKey("filePath")) {
			filePath = param.get("filePath");
		}
		if (param.containsKey("indexPath")) {
			indexPath = param.get("indexPath");
		}
	}

	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		Map<String, Node> nodeIds = new HashMap<String, Node>();
		// Map<String, String> nodeContents = new HashMap<String, String>();

		try {
			if (indexPath == null) {
				throw new AdapterException("No path for saving index");
			}
			graph.setPath(indexPath);
			// File file = new File(filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = factory.newDocumentBuilder();
			// Document doc = docBuilder.parse(file);
			BufferedReader br = new BufferedReader(this.reader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			InputSource is = new InputSource(new StringReader(sb.toString()));
			Document doc = docBuilder.parse(is);
			Element docElement = doc.getDocumentElement();
			
			NodeList nodeList = docElement.getElementsByTagName("node");
			int nodeListLength = nodeList.getLength();

			for (int i = 0; i < nodeListLength; i++) {
				Element e = (Element) nodeList.item(i);
				//Date d = df.parse(e.getAttribute("CreationDate"));
				Node node;
				
				// String customNodeContent = textProc.preprocText(e.getAttribute("Body"));
				String customNodeId = e.getAttribute("label");
				String customNodeName = "";
				//String customNodeParent = e.getAttribute("ParentId");
				
				NodeList attributes = e.getElementsByTagName("att");
				for (int a = 0; a < attributes.getLength(); a++) {
					if(((Element) attributes.item(a)).getAttribute("name") == "snippet")
					{
						NodeList snippetAttributes = e.getElementsByTagName("att");
						for (int b = 0; b < snippetAttributes.getLength(); b++) {
							if(((Element) snippetAttributes.item(a)).getAttribute("name") == "title")
							{
								customNodeName = ((Element) snippetAttributes.item(a)).getAttribute("name");
							}
							else break;
						}
						break;
					}
				}
				if (customNodeName == "") {
					customNodeName = customNodeId;
				}
				// node does not yet exist
				if (!nodeIds.containsKey(customNodeId)) {
					node = graph.createNode(); // create new node and add attributes
					graph.setNodeName(node, customNodeId); //TODO: Find a way to save both title and ID, as ID is not int
					nodeIds.put(customNodeId, node);
					// nodeContents.put(customNodeName, customNodeContent);
				} 
				//TODO: Maybe do an else case
			}

			//A bit confusing due to the class
			NodeList edgeList = docElement.getElementsByTagName("edge");
			int edgeListLength = edgeList.getLength();
			
			// create edges for each entry in the temporary edge list
			for (int i = 0; i < edgeListLength; i++) {
				Element e = (Element) edgeList.item(i);
				
				if(nodeIds.containsKey(e.getAttribute("source")) && nodeIds.containsKey(e.getAttribute("target")))
				{
					graph.createEdge(nodeIds.get(e.getAttribute("source")), nodeIds.get(e.getAttribute("target")));
				}
				else
				{
					continue;
				}		
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}
}
