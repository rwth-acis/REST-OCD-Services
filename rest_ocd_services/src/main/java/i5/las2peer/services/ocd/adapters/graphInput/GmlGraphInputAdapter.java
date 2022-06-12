package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.util.ThreadInterruptedException;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.stream.file.FileSourceGML;
import org.graphstream.stream.file.gml.GMLParser;

/**
 * A graph input adapter for GML format, based on the GMLIOHandler of the yFiles library.
 * In case each node has a label with a unique value node names will be derived from there. Otherwise node names will be set as indices.
 * In case each edge has a label with a numeric value edge weights will be derived from there.
 *
 * @author Sebastian
 */
//TODO: Test for graphstream
public class GmlGraphInputAdapter extends AbstractGraphInputAdapter {

    @Override
    public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

    }

    @Override
    public CustomGraph readGraph() throws AdapterException {
        CustomGraph graph = new CustomGraph();
        FileSourceGML fileSource = new FileSourceGML();
        fileSource.addSink(graph);

        Scanner scanner = new Scanner(reader);
        String inString = scanner.useDelimiter("\\A").next();
        scanner.close();
        try {
            fileSource.begin(inString);
            while (fileSource.nextEvents()) { //TODO: Check if that is necessary here or if we shouldnt just do readAll
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
        } catch (Exception e) {
            throw new AdapterException("ERROR Could not read file: " + e.getMessage());
        }

        /*
         * Check whether node labels are unique names.
         */
        Iterator<Node> nodesIt = graph.iterator();
        Node node;
        HashMap<Node, String> nodenames = new HashMap<Node, String>();
        while (nodesIt.hasNext()) {
            node = nodesIt.next();
            CharSequence name = node.getLabel("label");
            if (name == null) {
                break;
            }
            nodenames.put(node, name.toString());
        }
        nodesIt = graph.iterator();
        /*
         * Node labels are unique.
         */
        if (nodenames.size() == graph.getNodeCount()) {
            while (nodesIt.hasNext()) {
                node = nodesIt.next();
                graph.setNodeName(node, nodenames.get(node));
            }
        }
        /*
         * Node labels are not unique.
         */
        else {
            while (nodesIt.hasNext()) {
                node = nodesIt.next();
                graph.setNodeName(node, node.getId()); //TODO: Changed from Index to Id here, check if that makes sense with how graphstream reads it
            }
        }
        /*
         * Check whether edge labels/weights are numeric.
         */
        Iterator<Edge> edges = graph.edges().iterator();
        Edge edge;
        HashMap<Edge, Double> edgeweights = new HashMap<Edge, Double>();
        while (edges.hasNext()) {
            edge = edges.next();
            Double weight = edge.getNumber("weight");
            if (!weight.isNaN()) {
                edgeweights.put(edge, weight);
            } else {
                break;
            }
        }
        edges = graph.edges().iterator();
        /*
         * all labels correspond numeric
         */
        if (edgeweights.size() == graph.getEdgeCount()) {
            while (edges.hasNext()) {
                edge = edges.next();
                graph.setEdgeWeight(edge, edgeweights.get(edge));
            }
        }
        return graph;
    }

}
