package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i5.las2peer.services.ocd.graphs.DynamicGraph;
import i5.las2peer.services.ocd.graphs.DynamicInteraction;
import org.checkerframework.checker.units.qual.A;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Node;

/**
 * A graph input adapter for timestamped edge list format.
 * Each line of input contains either a timestamp, node1 and node2 or a timestamp, action, node1 and node2.
 * They are each separated by a space delimiter and in the first case an edge addition "+" is given as an attribute
 * for the edge
 * @author Fabien
 *
 */
public class TimestampedEdgeListInputAdapter extends AbstractGraphInputAdapter {

    /**
     * Creates a new instance setting the reader attribute.
     * @param reader The reader to receive input from.
     */
    public TimestampedEdgeListInputAdapter(Reader reader) {
        this.setReader(reader);
    }

    /**
     * Creates a new instance.
     */
    public TimestampedEdgeListInputAdapter() {
    }

    @Override
    public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException{

    }

    @Override
    public DynamicGraph readGraph() throws AdapterException {
        DynamicGraph graph = new DynamicGraph();
        try {
            Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
            List<String> line = Adapters.readLine(reader);
            /*
             * Reads edges
             */
            if(line.size() == 3) {
                while (line.size() == 3) {
                    String sourceNodeName = line.get(1);
                    Node sourceNode;
                    if (!reverseNodeNames.containsKey(sourceNodeName)) {
                        sourceNode = graph.addNode(sourceNodeName);
                        reverseNodeNames.put(sourceNodeName, sourceNode);
                        graph.setNodeName(sourceNode, sourceNodeName);
                    } else {
                        sourceNode = reverseNodeNames.get(sourceNodeName);
                    }
                    String targetNodeName = line.get(2);
                    Node targetNode;
                    if (!reverseNodeNames.containsKey(targetNodeName)) {
                        targetNode = graph.addNode(targetNodeName);
                        reverseNodeNames.put(targetNodeName, targetNode);
                        graph.setNodeName(targetNode, targetNodeName);
                    } else {
                        targetNode = reverseNodeNames.get(targetNodeName);
                    }
                    String edgeDate = line.get(0);
                    String edgeAction = "+";
                    Edge edge = graph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);
                    graph.addDynamicInteraction(edge, edgeDate, edgeAction);
                    line = Adapters.readLine(reader);
                }
            } else if (line.size() == 4) {
                while (line.size() == 4) {
                    String sourceNodeName = line.get(2);
                    Node sourceNode;
                    if (!reverseNodeNames.containsKey(sourceNodeName)) {
                        sourceNode = graph.addNode(sourceNodeName);
                        reverseNodeNames.put(sourceNodeName, sourceNode);
                        graph.setNodeName(sourceNode, sourceNodeName);
                    } else {
                        sourceNode = reverseNodeNames.get(sourceNodeName);
                    }
                    String targetNodeName = line.get(3);
                    Node targetNode;
                    if (!reverseNodeNames.containsKey(targetNodeName)) {
                        targetNode = graph.addNode(targetNodeName);
                        reverseNodeNames.put(targetNodeName, targetNode);
                        graph.setNodeName(targetNode, targetNodeName);
                    } else {
                        targetNode = reverseNodeNames.get(targetNodeName);
                    }
                    String edgeDate = line.get(0);
                    String edgeAction = line.get(1);
                    if (edgeAction.equals("+")) {
                        Edge edge = graph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);
                        graph.addDynamicInteraction(edge, edgeDate, edgeAction);
                    }else if(edgeAction.equals("-")) {
                        try {
                            Edge edge = graph.removeEdge(sourceNode,targetNode);
                            graph.addDynamicInteraction(edge, edgeDate, edgeAction);
                        } catch (ElementNotFoundException e1) {
                            try {
                                Edge edge = graph.removeEdge(targetNode, sourceNode);
                                graph.addDynamicInteraction(edge, edgeDate, edgeAction);
                            } catch (ElementNotFoundException e2) {
                                System.out.println(e1 + " and/or " + e2);
                            }
                        }
                    }else {
                        throw new AdapterException("Invalid action");
                    }
                    line = Adapters.readLine(reader);
                }
            }
            if(line.size() > 0) {
                throw new AdapterException("Invalid input format");
            }
            return graph;
        }
        catch (Exception e) {
            throw new AdapterException(e);
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
            }
        }
    }

}
