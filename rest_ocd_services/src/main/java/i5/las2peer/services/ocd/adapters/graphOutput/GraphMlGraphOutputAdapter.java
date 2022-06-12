package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkGraphML;

/**
 * A graph output adapter for GraphML format, based on the GraphMLIOHandler of the graphstream library.
 * Node names will be written into a CDATA Section in a "name" element. //TODO
 * Edge weights will be written into a "weight" element. //TODO
 * @author Sebastian
 *
 */
//TODO: Check how output of customgraph attributes looks
public class GraphMlGraphOutputAdapter extends AbstractGraphOutputAdapter {
	
	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {
		FileSinkGraphML fileSink = new FileSinkGraphML();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		//write node names and edge weights into attributes
		for (Node node : graph) {
			node.setAttribute("name", graph.getNodeName(node));
		}
		Iterator<Edge> edgesIt = graph.edges().iterator();
		while(edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			edge.setAttribute("weight", graph.getEdgeWeight(edge));
		}

		try {
			fileSink.writeAll(graph, outStream);
			String outString = outStream.toString();
			writer.write(outString);
		}
		catch(IOException e) {
			throw new AdapterException(e);
		} finally {
			try {
				outStream.close();
			}
			catch(IOException e) {
			}
			try {
				writer.close();
			}
			catch(IOException e) {
			}
		}
	}

}
