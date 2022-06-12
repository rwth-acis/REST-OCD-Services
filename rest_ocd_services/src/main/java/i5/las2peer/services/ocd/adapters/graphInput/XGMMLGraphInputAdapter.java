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
import org.w3c.dom.Text;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
//import i5.las2peer.services.ocd.utils.DocIndexer;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.LineType;

import java.io.Reader;
import java.io.FileReader;
import java.util.UUID;

//TODO: Currently only for the youtube graph, make more general
//TODO: Be able to have more Attributes for nodes(at least string id's) and maybe edges(at least type) in general
public class XGMMLGraphInputAdapter extends AbstractGraphInputAdapter {

	public XGMMLGraphInputAdapter() {

	}

	/////////////////
	//// Variables////
	/////////////////
	/**
	 * Variables for to check for different edge types
	 */
	private String type1 = "";

	private String type2 = "";

	private String type3 = "";

	/**
	 * Variable to look for edge type indicators in values, if empty string, the
	 * type indicators are understood as keys
	 */
	private String key = "";

	@Override
	public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

//		if (param.containsKey("key")) {
//			key = param.get("key");
//		}
//		if (param.containsKey("type1")) {
//			type1 = param.get("type1");
//		}
//		if (param.containsKey("type2")) {
//			type2 = param.get("type2");
//		}
//		if (param.containsKey("type3")) {
//			type3 = param.get("type3");
//		}
	}
	
	// Ignore for now as LineTypes are not stored in persistence for some reason
	public void setLineType(Element edgeElement, Edge edge, CustomGraph graph) {
		if (type1 != "" || type2 != "" || type3 != "") {
			EdgeRealizer eRealizer = graph.getRealizer(edge);

			NodeList atts = edgeElement.getChildNodes();
			if (atts.getLength() != 0) {	
				if (key.contentEquals("")) {
					for (int u = 0; u < atts.getLength(); u++) {
						if(atts.item(u).getNodeType() == 1) {
							Element e = (Element) atts.item(u);
							System.out.println(e.getAttribute(type2));
							System.out.println(e.getAttribute("name"));
							if (type1 != "" && e.hasAttribute(type1)) {
								eRealizer.setLineType(LineType.LINE_1);
								break;
							} else if (type2 != "" && e.hasAttribute(type2)) {
								eRealizer.setLineType(LineType.DASHED_1);
								System.out.println(eRealizer.getLineType().equals(LineType.DASHED_1));
								break;
							} else if (type3 != "" && e.hasAttribute(type3)) {
								eRealizer.setLineType(LineType.DOTTED_1);
								break;
							}
						}
					}
				} else {
					for (int u = 0; u < atts.getLength(); u++) {
						if(atts.item(u).getNodeType() == 1) {
							Element e = (Element) atts.item(u);
							
							if (type1 != "" && e.getAttribute(key).contentEquals(type1)) {
								eRealizer.setLineType(LineType.LINE_1);
								break;
							} else if (type2 != "" && e.getAttribute(key).contentEquals(type2)) {
								eRealizer.setLineType(LineType.DASHED_1);								
								break;
							} else if (type3 != "" && e.getAttribute(key).contentEquals(type3)) {
								eRealizer.setLineType(LineType.DOTTED_1);
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		Map<String, Node> nodeIds = new HashMap<String, Node>();
		// Map<String, String> nodeContents = new HashMap<String, String>();

		try {
			// File file = new File(filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = factory.newDocumentBuilder();

			//this.reader = new FileReader(filePath); //TODO: Only for tests, find a better way to run those and to not have to comment/uncomment this everytime
			BufferedReader br = new BufferedReader(this.reader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			InputSource is = new InputSource(new StringReader(sb.toString()));
			// System.out.println(sb.toString());
			Document doc = docBuilder.parse(is);
			Element docElement = doc.getDocumentElement();
			boolean undirected = false;
			if (Integer.parseInt(docElement.getAttribute("directed")) == 0) {
				undirected = true;
			}

			NodeList nodeList = docElement.getElementsByTagName("node");
			int nodeListLength = nodeList.getLength();
			//System.out.println("GRAPH LEN: " + nodeListLength);

			for (int i = 0; i < nodeListLength; i++) {
				Element e = (Element) nodeList.item(i);
				// Date d = df.parse(e.getAttribute("CreationDate"));
				Node node;

				// String customNodeContent = textProc.preprocText(e.getAttribute("Body"));
				String customNodeId = e.getAttribute("id");
				String customNodeName = "";
				// String customNodeParent = e.getAttribute("ParentId");
				// TODO: Get rid of customNodeName, apparently not able to add more attributes
				NodeList attributes = e.getElementsByTagName("att");
				for (int a = 0; a < attributes.getLength(); a++) {
					if (((Element) attributes.item(a)).getAttribute("name") == "snippet") {
						NodeList snippetAttributes = e.getElementsByTagName("att");
						for (int b = 0; b < snippetAttributes.getLength(); b++) {
							if (((Element) snippetAttributes.item(a)).getAttribute("name") == "title") {
								customNodeName = ((Element) snippetAttributes.item(a)).getAttribute("name");
							} else
								break;
						}
						break;
					}
				}
				if (customNodeName == "") {
					customNodeName = customNodeId;
				}
				// node does not yet exist
				if (!nodeIds.containsKey(customNodeId)) {
					node = graph.addNode(customNodeId); // create new node and add attributes
					graph.setNodeName(node, customNodeId);
					nodeIds.put(customNodeId, node);
					// nodeContents.put(customNodeName, customNodeContent);
				}
				// TODO: Maybe do an else case
			}

			// A bit confusing due to the class
			NodeList edgeList = docElement.getElementsByTagName("edge");
			int edgeListLength = edgeList.getLength();
			Map<String, Edge> edgeMap = new HashMap<String, Edge>();

			// create edges for each entry in the temporary edge list
			for (int i = 0; i < edgeListLength; i++) {
				Element e = (Element) edgeList.item(i);
				
				if (nodeIds.containsKey(e.getAttribute("source")) && nodeIds.containsKey(e.getAttribute("target"))) {
					if (!edgeMap.containsKey(e.getAttribute("label"))) {
						Edge edge = graph.addEdge(UUID.randomUUID().toString(), nodeIds.get(e.getAttribute("source")), nodeIds.get(e.getAttribute("target")));
						//setLineType(e, edge, graph);
						
						if (undirected) {
							Edge reverseEdge = graph.addEdge(UUID.randomUUID().toString(), nodeIds.get(e.getAttribute("target")), nodeIds.get(e.getAttribute("source")));
							//graph.getRealizer(reverseEdge).setLineType(graph.getRealizer(edge).getLineType());
						}
						edgeMap.put(e.getAttribute("source") + e.getAttribute("target"), edge);
					}
				} else {
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
