package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.preprocessing.TextProcessor;
import y.base.Node;

/**
 * A graph input adapter for a node list which includes a content attribute for each node but no edges.
 * Each line must contain a author attribute (e.g. a user name we can use as node name) and a content attribute. 
 * There can be several lines for one user, so that the content will simply be attached.
 * In the first line the attribute names have to be specified.
 * @author Sabrina
 *
 */

public class NodeContentListGraphInputAdapter extends AbstractGraphInputAdapter{
	
	public NodeContentListGraphInputAdapter(Reader reader){
		this.reader = reader;
	}
	
	public NodeContentListGraphInputAdapter(){
		
	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		TextProcessor textProc = new TextProcessor();
		CustomGraph graph = new CustomGraph();
		try{
			Map<String, Node> nodeNames = new HashMap<String, Node>();
			List<String> line = Adapters.readLine(reader);
			int nameIndex = -1;
			int contentIndex = -1;
			int index = 0;
			
			if(line.size() <= 0){
				throw new AdapterException("Input format invalid");
			}
			
			for(Iterator<String> it = line.iterator(); it.hasNext();){ 
				String curr = it.next();
				
				if(curr.toUpperCase().equals("AUTHOR")){
					nameIndex = index;
				}
				if(curr.toUpperCase().equals("CONTENT")){
					contentIndex = index;
				}
				index++;
			}
			
			if(nameIndex == -1){
				throw new AdapterException("No name attribute");
			}
			
			if(contentIndex == -1){
				throw new AdapterException("No content attribute");
			}
			
			line = Adapters.readLineTab(reader);
			
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
