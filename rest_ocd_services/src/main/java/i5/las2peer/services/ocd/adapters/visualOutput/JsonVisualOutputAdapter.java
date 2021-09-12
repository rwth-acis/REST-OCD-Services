package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;
import net.minidev.json.JSONValue;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import y.base.Node;
import y.base.Edge;
import y.view.Graph2D;

import y.view.NodeLabel;
import java.awt.Color;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.LineType;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

//TODO: Add labels
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
	public void writeGraph(Graph2D graph) throws AdapterException {
		JSONObject obj = new JSONObject();
		// Document doc = builder.newDocument();
		ArrayList<JSONObject> nodes = new ArrayList<JSONObject>();
		for (Node n : graph.getNodeArray()) {
			HashMap<String, Object> tmp = new HashMap<String, Object>();
			tmp.put("id", n.index());
			if(graph instanceof CustomGraph)
			{
				tmp.put("name", ((CustomGraph)graph).getNodeName(n));
			}
			
			NodeRealizer nRealizer = graph.getRealizer(n);
			//TODO: Check what the color is when not set
			//Color			
			//rgba(r,g,b,a)
			float[] nodeColor = new float[4];
			nodeColor = nRealizer.getFillColor().getRGBComponents(nodeColor);
			tmp.put("color", "rgba(" + nodeColor[0]*255 + "," + nodeColor[1]*255 + "," + nodeColor[2]*255 + "," + nodeColor[3] + ")");
			
			//TODO: Check what the size is when not set
			//Size
			//As the force graph representation uses circles and height and width are the same in our layoutHandler, this suffices
			double nodeSize = 0.0f;
			nodeSize = nRealizer.getHeight();
			tmp.put("size", nodeSize);	
			
			//Label
			String nodeLabel = "";
			nodeLabel = nRealizer.getLabelText();
			tmp.put("label", nodeLabel);

			JSONObject jsonNode = (JSONObject) JSONValue.parse(JSONValue.toJSONString(tmp));

			nodes.add(jsonNode);
		}
				
		
		ArrayList<JSONObject> edges = new ArrayList<JSONObject>();
		for (Edge e : graph.getEdgeArray()) {
			HashMap<String, Object> tmp = new HashMap<String, Object>();
			tmp.put("source", e.source().index());
			tmp.put("target", e.target().index());
			
			// LINE_STYLE = 0; DASHED_STYLE = 1; DOTTED_STYLE = 2; DASHED_DOTTED_STYLE = 3;
			EdgeRealizer eRealizer = graph.getRealizer(e);
			LineType lineType = LineType.LINE_1;
			lineType = eRealizer.getLineType();
			if(lineType.equals(LineType.DASHED_1))
			{
				System.out.println("WORKED");
				tmp.put("style", 1);
			}
			else if(lineType.equals(LineType.DOTTED_1))
			{
				tmp.put("style", 2);
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
