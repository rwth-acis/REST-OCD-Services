package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;

public class UnweightedEdgeListMultiGraphInputAdapter extends
        AbstractGraphInputAdapter {

    public UnweightedEdgeListMultiGraphInputAdapter(Reader reader) {
        this.setReader(reader);
    }

    public UnweightedEdgeListMultiGraphInputAdapter() {

    }

    @Override
    public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

    }

    @Override
    public CustomGraph readGraph() throws AdapterException {
        CustomGraph graph = new CustomGraph();
        Map<String, Node> reverseNodeNames = new HashMap<>();
        try {
            List<String> line = Adapters.readLine(reader);

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

                String layerId = line.get(0);

                Edge edge = graph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);
                graph.setEdgeWeight(edge, 1);
                graph.setEdgeLayerId(edge, layerId);

                Edge reverseEdge = graph.addEdge(UUID.randomUUID().toString(), targetNode, sourceNode);
                graph.setEdgeWeight(reverseEdge, 1);
                graph.setEdgeLayerId(reverseEdge, layerId);

                line = Adapters.readLine(reader);
            }
            if (line.size() > 0) {
                throw new AdapterException("Invalid input format");
            }
            return graph;
        } catch (Exception e) {
            throw new AdapterException(e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {

            }
        }

    }

}
