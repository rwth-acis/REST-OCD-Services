package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;
import net.minidev.json.JSONValue;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;


import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class JsonVisualOutputAdapter extends AbstractVisualOutputAdapter {
	/**
	 * Creates a new instance setting the writer attribute.
	 * @param writer The writer used for output.
	 */
	public JsonVisualOutputAdapter(Writer writer) {
		this.setWriter(writer);
	}
	
	/**
	 * Creates a new instance.
	 */
	public JsonVisualOutputAdapter() {
	}
	
	@Override
	//TODO: Check if color values still need to be multiplied with 255
	public void writeGraph(CustomGraph graph) throws AdapterException {
		JSONObject obj = new JSONObject();
		// Document doc = builder.newDocument();
		ArrayList<JSONObject> nodes = new ArrayList<JSONObject>();
		Iterator<Node> nodesIt = graph.iterator();
		while (nodesIt.hasNext()) {
			Node n = nodesIt.next();
			HashMap<String, Object> tmp = new HashMap<String, Object>();
			tmp.put("id", n.getIndex());
			if(graph instanceof CustomGraph)
			{
				tmp.put("name", graph.getNodeName(n));
			}
			
			//TODO: Check whether default coloring makes sense here
			//Color
			//rgba(r,g,b,a)
			Color nodeColor = new Color(0.f,0.f,1.f, 0.6f);
			if(n.getAttribute("ui.ui.fill-color") != null) {
				tmp.put("color", n.getLabel("ui.fill-color").toString());
			}
			else {
				tmp.put("color", "rgba(0," + nodeColor.getGreen() + "," + nodeColor.getBlue() + "," + nodeColor.getAlpha() + ")");
			}

			//TODO: Check whether default size makes sense here
			//Size
			//As the force graph representation uses circles and height and width are the same in our layoutHandler, this suffices
			double nodeSize = .3f;
			if(n.getAttribute("ui.size") != null) {
				tmp.put("size", n.getAttribute("ui.size").toString());
			}
			else {
				tmp.put("size", nodeSize);
			}
			
			//Label
			tmp.put("label", n.getLabel("ui.label").toString());

			JSONObject jsonNode = (JSONObject) JSONValue.parse(JSONValue.toJSONString(tmp));

			nodes.add(jsonNode);
		}
				
		
		ArrayList<JSONObject> edges = new ArrayList<JSONObject>();
		Iterator<Edge> edgesIt = graph.edges().iterator();
		while (edgesIt.hasNext()) {
			Edge e = edgesIt.next();
			HashMap<String, Object> tmp = new HashMap<String, Object>();
			tmp.put("source", e.getSourceNode().getIndex());
			tmp.put("target", e.getTargetNode().getIndex());
			
			// LINE_STYLE = 0; DASHED_STYLE = 1; DOTTED_STYLE = 2; DASHED_DOTTED_STYLE = 3;
			if(e.getAttribute("ui.stroke-mode") != null) {
				String lineType = e.getAttribute("ui.stroke-mode").toString();
				if (lineType.equals("dashes")) {
					tmp.put("style", 1);
				} else if (lineType.equals("dots")) {
					tmp.put("style", 2);
				}
			}
			else
			{
				tmp.put("style", 0);
			}

			JSONObject jsonEdge = (JSONObject) JSONValue.parse(JSONValue.toJSONString(tmp));

			edges.add(jsonEdge);
		}
		
		HashMap<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("nodes", nodes);
		tmp.put("links", edges);
		obj = (JSONObject) JSONValue.parse(JSONValue.toJSONString(tmp));
		
		try {
			StringWriter out = new StringWriter();
			obj.writeJSONString(out);
			
			writer.write(out.toString());
		} catch (IOException e) {
			throw new AdapterException(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				//TODO: Check what to throw here
			}
		}
	}
}
