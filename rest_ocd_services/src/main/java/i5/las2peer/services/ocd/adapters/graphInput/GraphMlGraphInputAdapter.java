package i5.las2peer.services.ocd.adapters.graphInput;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

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
			 * Checks whether node names are unique.
			 */
			Iterator<Node> nodes = graph.iterator();
			CharSequence name;
			HashMap<Integer, String> names = new HashMap<Integer, String>();
			while(nodes.hasNext()) {
				Node node = nodes.next();
				name = node.getLabel("ui.label");
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
			Iterator<Edge> edges = graph.edges().iterator();
			Edge edge;
			while(edges.hasNext()) {
				edge = edges.next();
				Double weight = edge.getNumber("weight");
				if(!weight.isNaN()) {
					graph.setEdgeWeight(edge, weight);
				}
				else {
					break;
				}
			}
		return graph;
	}

}
