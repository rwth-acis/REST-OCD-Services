package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.preprocessing.TextProcessor;
import i5.las2peer.services.ocd.utils.DocIndexer;
import y.base.Edge;
import y.base.Node;

/**
 * A graph input adapter for a node list which includes a content attribute for each node and edges in form of a 
 * threadid or a specified sender and receiver.
 * Each line must contain either a author attribute (e.g. a user name we can use as node name), a content attribute and a threadid attribute,
 * or a sender attribute used as the user name, a receiver attribute to compute the edges and a content attribute. 
 * There can be several lines for one user, so that the content will simply be attached.
 * In the first line the attribute names have to be specified.
 * @author Sabrina 
 */

public class NodeContentEdgeListGraphInputAdapter extends AbstractGraphInputAdapter{
	
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
	
	private String path = "C:\\indexes\\pgsql";
	
	public NodeContentEdgeListGraphInputAdapter(){
		
	}
	
	public NodeContentEdgeListGraphInputAdapter(Reader reader){
		this.reader = reader;
	}
	
	@Override
	public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException{
		SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
		
		if(param.containsKey("startDate")){
			startDate = df.parse(param.get("startDate"));
		}
		if(param.containsKey("endDate")){
			endDate = df.parse(param.get("endDate"));
		}
		if(param.containsKey("path")){
			path = param.get("path");
		}
	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		
		CustomGraph graph = new CustomGraph();
		try{
			
			List<String> line = Adapters.readLine(reader);
			int nameIndex = -1;
			int contentIndex = -1;
			int senderIndex = -1;
			int receiverIndex = -1;
			int threadIndex = -1;
			int dateIndex = -1;
			int index = 0;
			
			if(line.size() <= 0){
				throw new AdapterException("Input format invalid");
			}
			
			for(Iterator<String> it = line.iterator(); it.hasNext();){ 
				String curr = it.next();
				
				switch(curr.toUpperCase()){
					case "AUTHOR":
						nameIndex = index;
						break;
					case "CONTENT":
						contentIndex = index;
						break;
					case "SENT_BY":
						senderIndex = index;
						break;
					case "REPLIES_TO":
						receiverIndex = index;
						break;
					case "THREAD_ID":
						threadIndex = index;
						break;
					case "DATE":
						dateIndex = index;
						break;
						
				}
				
				index++;
			}
			
			if(nameIndex == -1 && senderIndex == -1){
				throw new AdapterException("No name attribute");
			}
			
			if(contentIndex == -1){
				throw new AdapterException("No content attribute");
			}
			
			
			if(receiverIndex == -1){
				if(threadIndex == -1){
					throw new AdapterException("No attribute to generate links");
				}else{
					graph = readThreadGraph(nameIndex, contentIndex, dateIndex, threadIndex);
				}
			}else{
				graph = readSenderReceiverGraph(senderIndex, receiverIndex, contentIndex, dateIndex, line.size());
			}

			
		}catch(Exception e){
			throw new AdapterException(e);
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
		return graph;
	}

	private CustomGraph readSenderReceiverGraph(int senderIndex, int receiverIndex, int contentIndex, int dateIndex, int lineLength) throws IOException, AdapterException {
		
		TextProcessor textProc = new TextProcessor();
		Map<String, Node> nodeNames = new HashMap<String, Node>();
		Map<String, String> nodeContents = new HashMap<String, String>();
		CustomGraph graph = new CustomGraph();
		Map<Node,HashMap<String,Integer>> links = new HashMap<Node, HashMap<String,Integer>>();
		SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
		
		try{
		// read first content line
		List<String> line = Adapters.readLineTabIgnoreLineBreak(reader,lineLength);
		graph.setPath(path);
		// create nodes
		while(line.size() > 0){
	
			Date d = df.parse(line.get(dateIndex));
			/*if(startDate != null || endDate != null){
				if(d.after(endDate)){			//assuming that we have a dataset sorted according to date
					break;
				}
			}*/
			if(!((startDate != null && d.before(startDate)) || (endDate != null && d.after(endDate)))){
				Node node; 
				String customNodeName = line.get(senderIndex);
				String customNodeContent = textProc.preprocText(line.get(contentIndex));
				String customNodeReceiver = line.get(receiverIndex);
				// node does not yet exist
				if(!nodeNames.containsKey(customNodeName)){
					node = graph.createNode();						//create new node and add attributes
					graph.setNodeName(node , customNodeName);
					nodeContents.put(customNodeName, customNodeContent);
					//graph.setNodeContent(node, customNodeContent);
					HashMap<String,Integer> temp = new HashMap<String,Integer>();
					temp.put(customNodeReceiver,1);					// initialize structural weights (number of connections between two nodes)
					links.put(node, temp);							// temporarly save nodes connections to other nodes
					nodeNames.put(customNodeName, node);
				// node is already create, so content has to be added
				}else{
					node = nodeNames.get(customNodeName);		// get respective node
					//customNodeContent = customNodeContent + " " + graph.getNodeContent(node);	//add further content to the nodes attribute
					nodeContents.merge(customNodeName, " " + customNodeContent, String::concat);
					//graph.setNodeContent(node, customNodeContent);
					HashMap<String,Integer> temp = links.get(node); // get connections of the node
					if (temp.containsKey(customNodeReceiver)) {
						int r = temp.get(customNodeReceiver);		// increase weight if link already exists
						r++;
						temp.put(customNodeReceiver,r);
					}else{
						temp.put(customNodeReceiver, 1);			// add new link and initialize weight
					}
					links.put(node, temp);
				}
			}
			//read next content line
			line = Adapters.readLineTabIgnoreLineBreak(reader,lineLength);
			
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
				if(nodeNames.containsKey(e.getKey())){
					Edge edge = graph.createEdge(curr, nodeNames.get(e.getKey()));
					graph.setEdgeWeight(edge, e.getValue());
				}
			}
		
		}
		
		}catch(Exception e){
			throw new AdapterException(e);
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
		return graph;
	}

	private CustomGraph readThreadGraph(int nameIndex, int contentIndex, int dateIndex, int threadIndex) throws IOException, AdapterException {
		
		TextProcessor textProc = new TextProcessor();
		Map<String, Node> nodeNames = new HashMap<String, Node>();
		Map<String,String> nodeContents = new HashMap<String,String>();
		Map<Node, LinkedList<String>> nodeThreads = new HashMap<Node, LinkedList<String>>();
		CustomGraph graph = new CustomGraph();
		SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
		
		try{
		List<String> line = Adapters.readLineTab(reader);
		graph.setPath(path);
		// create nodes
		while(line.size() > 0){
			
			Date d = df.parse(line.get(dateIndex));
			
			if(!((startDate != null && d.before(startDate)) || (endDate != null && d.after(endDate)))){
			
				Node node; 
				String customNodeName = textProc.deletWhiteSpace(line.get(nameIndex));
				String customNodeContent = textProc.preprocText(line.get(contentIndex));
				String customNodeThread = line.get(threadIndex);
				if(!nodeNames.containsKey(customNodeName)){
					node = graph.createNode();
					graph.setNodeName(node , customNodeName);
					nodeContents.put(customNodeName, customNodeContent);
					//graph.setNodeContent(node, customNodeContent);
					nodeNames.put(customNodeName, node);
					LinkedList<String> th = new LinkedList<String>();
					th.add(customNodeThread);
					nodeThreads.put(node, th);
				}else{
					node = nodeNames.get(customNodeName);
					
					//customNodeContent = customNodeContent + " " + graph.getNodeContent(node);
					//graph.setNodeContent(node, customNodeContent);
					nodeContents.merge(customNodeName, " " + customNodeContent, String::concat);
					LinkedList<String> thr = nodeThreads.get(node);
					thr.add(customNodeThread);
					nodeThreads.put(node, thr);
				}
			}
			
			line = Adapters.readLineTab(reader);
		}
		
			DocIndexer di = new DocIndexer(graph.getPath());
			//create lucene index for content
			for(Entry<String,String> e : nodeContents.entrySet()){
				di.indexDocPerField(e.getKey(), e.getValue());	
			}
						
			//create edges for each entry in the temporary edge list
			for(Entry<Node, LinkedList<String>> entry : nodeThreads.entrySet()){
				Node curr = entry.getKey();
				LinkedList<String> list = entry.getValue();
				for(String str:list){
					for(Entry<Node, LinkedList<String>> reciever : nodeThreads.entrySet()){
						if(curr != reciever.getKey() && reciever.getValue().contains(str)){
						graph.createEdge(curr, reciever.getKey());
						//graph.setEdgeWeight(edge, reciever.getValue());
						}
					}
				}
			
			}
			
		
		
		}catch(Exception e){
			throw new AdapterException(e);
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
		return graph;
	}
}
