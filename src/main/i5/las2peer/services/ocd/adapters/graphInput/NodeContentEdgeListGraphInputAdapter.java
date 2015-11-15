package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.preprocessing.TextProcessor;
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
	
	public NodeContentEdgeListGraphInputAdapter(){
		
	}
	
	public NodeContentEdgeListGraphInputAdapter(Reader reader){
		this.reader = reader;
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
					case "SENDER":
						senderIndex = index;
						break;
					case "RECEIVER":
						receiverIndex = index;
						break;
					case "THREADID":
						threadIndex = index;
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
			
			if(threadIndex == -1){
				if(receiverIndex == -1){
				
					throw new AdapterException("No attribute to generate links");
				}else{
					graph = readSenderReceiverGraph(senderIndex, receiverIndex, contentIndex);
				}
			}else{
				graph = readThreadGraph(nameIndex, contentIndex, threadIndex);
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

	private CustomGraph readSenderReceiverGraph(int senderIndex, int receiverIndex, int contentIndex) throws IOException, AdapterException {
		
		TextProcessor textProc = new TextProcessor();
		Map<String, Node> nodeNames = new HashMap<String, Node>();
		CustomGraph graph = new CustomGraph();
		Map<Node,HashMap<String,Integer>> links = new HashMap<Node, HashMap<String,Integer>>();
		
		try{
		List<String> line = Adapters.readLineTab(reader);
		
		// create nodes
		while(line.size() > 0){
			
			Node node; 
			String customNodeName = line.get(senderIndex);
			String customNodeContent = textProc.preprocText(line.get(contentIndex));
			String customNodeReceiver = line.get(receiverIndex);
			if(!nodeNames.containsKey(customNodeName)){
				node = graph.createNode();
				graph.setNodeName(node , customNodeName);
				graph.setNodeContent(node, customNodeContent);
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				temp.put(customNodeReceiver,1);
				links.put(node, temp);
				nodeNames.put(customNodeName, node);
			}else{
				node = nodeNames.get(customNodeName);
				customNodeContent = customNodeContent + " " + graph.getNodeContent(node);
				graph.setNodeContent(node, customNodeContent);
				HashMap<String,Integer> temp = links.get(node);
				if (temp.containsKey(customNodeReceiver)) {
					int r = temp.get(customNodeReceiver);
					r++;
					temp.put(customNodeReceiver,r);
				}else{
					temp.put(customNodeReceiver, 1);
				}
				links.put(node, temp);
			}
			
			line = Adapters.readLineTab(reader);
			
		}
		
		//create edges
		for(Entry<Node, HashMap<String,Integer>> entry : links.entrySet()){
			Node curr = entry.getKey();
			HashMap<String,Integer> list = entry.getValue();
			for(Entry<String,Integer> e : list.entrySet()){
				Edge edge = graph.createEdge(curr, nodeNames.get(e.getKey()));
				graph.setEdgeWeight(edge, e.getValue());
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

	private CustomGraph readThreadGraph(int nameIndex, int contentIndex, int threadIndex) throws IOException, AdapterException {
		
		TextProcessor textProc = new TextProcessor();
		Map<String, Node> nodeNames = new HashMap<String, Node>();
		CustomGraph graph = new CustomGraph();
		
		try{
		List<String> line = Adapters.readLineTab(reader);
		while(line.size() > 0){
			
			Node node; 
			String customNodeName = line.get(nameIndex);
			String customNodeContent = textProc.preprocText(line.get(contentIndex));
			if(!nodeNames.containsKey(customNodeName)){
				node = graph.createNode();
				graph.setNodeName(node , customNodeName);
				graph.setNodeContent(node, customNodeContent);
				nodeNames.put(customNodeName, node);
			}else{
				node = nodeNames.get(customNodeName);
				customNodeContent = customNodeContent + " " + graph.getNodeContent(node);
				graph.setNodeContent(node, customNodeContent);
			}
			
			line = Adapters.readLineTab(reader);
			
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
