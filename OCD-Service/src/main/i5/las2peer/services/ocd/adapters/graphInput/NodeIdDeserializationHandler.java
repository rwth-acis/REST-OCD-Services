package i5.las2peer.services.ocd.adapters.graphInput;

import java.util.HashMap;

import org.w3c.dom.Element;

import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseException;

public class NodeIdDeserializationHandler implements DeserializationHandler {

	private HashMap<Integer, String> nodeIds = new HashMap<Integer, String>();
	int i=0;
	
	@Override
	public void onHandleDeserialization(DeserializationEvent event)
			throws GraphMLParseException {
	    // get the element to parse  
	    org.w3c.dom.Node xmlNode = event.getXmlNode();
	    
	    // if the element can be parsed  
	    // create a new instance 
	    if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE  
	            && "node".equals(xmlNode.getLocalName())) {  
	      // create a new instance with the value of the "value" attribute
	      String id = new String(((Element) xmlNode).getAttribute("id"));  
	      // pass the new instance as result  
	      // Note: setting the result already marks the event as handled  
	      nodeIds.put(i, id);
	      i++;
	    }  
	    
	}

}
