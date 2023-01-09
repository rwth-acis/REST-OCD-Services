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
import java.util.UUID;

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
import i5.las2peer.services.ocd.preprocessing.TextProcessor;
import i5.las2peer.services.ocd.utils.DocIndexer;
//import i5.las2peer.services.ocd.utils.DocIndexer;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

public class XMLGraphInputAdapter extends AbstractGraphInputAdapter{

	public XMLGraphInputAdapter(){
		
	}
	
	/////////////////
	////Variables////
	/////////////////
	/**
	* Variable for the beginning of the date interval, the posts have to be issued in
	*/
	private Date startDate = null;
	
	/**
	* Variable for the beginning of the date interval, the posts have to be issued in
	*/
	private Date endDate = null;
	
	private String filePath = "ocd/test/input/stackexAcademia.xml";
	
	
	private String indexPath = null;
	
	@Override
	public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException{
		SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
		
		if(param.containsKey("startDate")){
			startDate = df.parse(param.get("startDate"));
		}
		if(param.containsKey("endDate")){
			endDate = df.parse(param.get("endDate"));
		}
		if(param.containsKey("filePath")){
			filePath = param.get("filePath");
		}
		if(param.containsKey("indexPath")){
			indexPath = param.get("indexPath");
		}
	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException{
		CustomGraph graph = new CustomGraph();
		TextProcessor textProc = new TextProcessor();
		Map<String, Node> nodeNames = new HashMap<String, Node>();
		Map<String, Node> nodeIds = new HashMap<String, Node>();
		Map<String, String> nodeContents = new HashMap<String, String>();
		Map<Node,HashMap<String,Integer>> links = new HashMap<Node, HashMap<String,Integer>>();
		SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd");

		try {
			
			if(indexPath == null){
				throw new AdapterException("No path for saving index");
			}
			graph.setPath(indexPath);
			//File file = new File(filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = factory.newDocumentBuilder();
			//Document doc = docBuilder.parse(file);
			BufferedReader br = new BufferedReader(this.reader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
			    sb.append(line);
			}
			InputSource is = new InputSource(new StringReader(sb.toString()));
			Document doc = docBuilder.parse(is);
			Element docElement = doc.getDocumentElement();
			NodeList nodeList = docElement.getElementsByTagName("row");
			int length = nodeList.getLength();
			
			for(int i = 0; i < length; i++){
				Element e = (Element) nodeList.item(i);
				Date d = df.parse(e.getAttribute("CreationDate"));
				if(!((startDate != null && d.before(startDate)) || (endDate != null && d.after(endDate)))){
					Node node; 
					String customNodeName = e.getAttribute("OwnerUserId");
					if(customNodeName == ""){
						customNodeName = textProc.deletWhiteSpace(e.getAttribute("OwnerDisplayName"));
					}
					String customNodeContent = textProc.preprocText(e.getAttribute("Body"));
					String customNodeId = e.getAttribute("Id");
					String customNodeParent = e.getAttribute("ParentId");
					// node does not yet exist
					if(!nodeNames.containsKey(customNodeName)){
						node = graph.addNode(customNodeName);						//create new node and add attributes
						graph.setNodeName(node , customNodeName);
						nodeIds.put(customNodeId, node);
						nodeContents.put(customNodeName, customNodeContent);
						if(customNodeParent != ""){
							HashMap<String,Integer> temp = new HashMap<String,Integer>();
							temp.put(customNodeParent,1);					// initialize structural weights (number of connections between two nodes)
							links.put(node, temp);	
						}												// temporarly save nodes connections to other nodes
						nodeNames.put(customNodeName, node);
					// node is already create, so content has to be added
					}else{
						node = nodeNames.get(customNodeName);		// get respective node
						//customNodeContent = customNodeContent + " " + graph.getNodeContent(node);	//add further content to the nodes attribute
						nodeContents.merge(customNodeName, " " + customNodeContent, String::concat);
						if(!nodeIds.containsKey(customNodeId)){
							nodeIds.put(customNodeId, node);
						}
						if(customNodeParent != ""){
							HashMap<String,Integer> temp;
							
							if(links.get(node) == null){
								temp = new HashMap<String,Integer>();
							}else{
								temp = links.get(node); // get connections of the node
							}
							
							if (temp.containsKey(customNodeParent)) {
								int r = temp.get(customNodeParent);		// increase weight if link already exists
								r++;
								temp.put(customNodeParent,r);
							}else{
								temp.put(customNodeParent, 1);			// add new link and initialize weight
							}
							links.put(node, temp);
						}
					}
				}
			}
			
			DocIndexer di = new DocIndexer(graph.getPath());
			//create lucene index for content
			for(Entry<String,String> e : nodeContents.entrySet()){
				di.indexDocPerField(e.getKey(), e.getValue());	
			}
			
			//create edges for each entry in the temporary edge list
			for(Entry<Node, HashMap<String,Integer>> entry : links.entrySet()){
				Node curr = entry.getKey();
				HashMap<String,Integer> list = entry.getValue();
				for(Entry<String,Integer> e : list.entrySet()){
					if(nodeIds.containsKey(e.getKey())){
						Edge edge = graph.addEdge(UUID.randomUUID().toString(), curr, nodeIds.get(e.getKey()));
						graph.setEdgeWeight(edge, e.getValue());
					}
				}
			
			}
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return graph;
	}
}
