package i5.las2peer.services.ocd.adapters.graphInput;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.graphstream.stream.file.FileSourceGraphML;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * A graph input adapter for GraphML format, based on the GraphMLIOHandler of the yFiles library..
 * In case each node has a "name" element containing a CDATA section with a unique value node names will be derived from there. Otherwise node names will be set according to the ids.
 * In case each edge has a "weight" element with a numeric value edge weights will be derived from there.
 * @author Sebastian
 *
 */
//TODO: Test for graphstream
public class GraphMlGraphInputAdapter extends AbstractGraphInputAdapter {

	@Override
	public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException{
		
	}

	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		FileSourceGraphML fileSource = new FileSourceGraphML();
		fileSource.addSink(graph);

		try {
			InputStream inStream = new ByteArrayInputStream(CharStreams.toString(reader)
					.getBytes(Charsets.UTF_8));

			reader.close();

			fileSource.begin(inStream);
			while (fileSource.nextEvents()) { //TODO: Check if that is necessary here or if we shouldnt just do readAll
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
			inStream.close();
		} catch (Exception e) {
			throw new AdapterException("ERROR Could not read file: " + e.getMessage());
		}
			/*
			 * Checks whether node names are unique
			 */
			Iterator<Node> nodes = graph.iterator();
			CharSequence name;
			HashMap<Integer, String> names = new HashMap<Integer, String>();
			JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			while(nodes.hasNext()) {
				Node node = nodes.next();
				name = node.getLabel("label");
				if(name == null || name.toString().isEmpty()) {
					break;
				}
				names.put(node.getIndex(), name.toString());
			}
			nodes = graph.iterator();
			/*
			 * Sets unique node names.
			 */
			if(names.size() == graph.getNodeCount()) {
				while(nodes.hasNext()) {
					Node node = nodes.next();
					graph.setNodeName(node, names.get(node.getIndex()));
				}
			}
			/*
			 * If names not unique sets indices instead.
			 */
			else {
				while(nodes.hasNext()) {
					Node node = nodes.next();
					graph.setNodeName(node, node.getId()); //TODO: Changed from Index to Id here, check if that makes sense with how graphstream reads it
				}
			}

			/*
			 * Add potential extraInfo attributes
			 */
			nodes = graph.nodes().iterator();
			while(nodes.hasNext()) {
				Node node = nodes.next();
				try {
					if (node.getLabel("nodeData") != null) {
						String convertedText = convertSpecialXMLCharacters(node.getLabel("nodeData").toString());
						graph.setNodeExtraInfo(node, (JSONObject) jsonParser.parse(convertedText));
					} else if (node.getLabel("extraInfo")  != null) {
						String convertedText = convertSpecialXMLCharacters(node.getLabel("extraInfo").toString());
						graph.setNodeExtraInfo(node, (JSONObject) jsonParser.parse(convertedText));
					}
				} catch (Exception e) {
					throw new AdapterException("Could not parse extra info for node " + node.getId() + ": " + e.getMessage());
				}
			}

			Iterator<Edge> edges = graph.edges().iterator();
			Edge edge;
			while(edges.hasNext()) {
				edge = edges.next();
				try {
					if (edge.getLabel("edgeData") != null) {
						String convertedText = convertSpecialXMLCharacters(edge.getLabel("edgeData").toString());
						graph.setEdgeExtraInfo(edge, (JSONObject) jsonParser.parse(convertedText));
					} else if (edge.getLabel("extraInfo")  != null) {
						String convertedText = convertSpecialXMLCharacters(edge.getLabel("extraInfo").toString());
						graph.setEdgeExtraInfo(edge, (JSONObject) jsonParser.parse(convertedText));
					}
				} catch (Exception e) {
					throw new AdapterException("Could not parse extra info for edge " + edge.getId() + ": " + e.getMessage());
				}

				double weight = edge.getNumber("weight");
				if(!Double.isNaN(weight)) {
					graph.setEdgeWeight(edge, weight);
				}
				else {
					break;
				}
			}
		return graph;
	}

	/**
	 * Method that does the job of converting character sequences that signify special characters in XML. Essentially does the job that the importer of graphstream should be doing.
	 * @param text some string with special char sequences
	 * @return the adjusted text
	 */
	private String convertSpecialXMLCharacters(String text) {
		text = text.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&apos;","'").replaceAll("&quot;","\"").replaceAll("&","&amp;");
		return text;
	}
}
