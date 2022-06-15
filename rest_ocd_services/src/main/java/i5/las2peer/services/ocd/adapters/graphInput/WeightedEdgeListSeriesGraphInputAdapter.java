package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Edge;
import y.base.Node;

import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightedEdgeListSeriesGraphInputAdapter extends AbstractGraphInputAdapter {


    @Override
    public CustomGraph readGraph() throws AdapterException {
        CustomGraph dynamicGraph = new CustomGraph();
        CustomGraph staticGraph=new CustomGraph();
        try {
            Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
            List<String> line = Adapters.readLine(reader);
            /*
             * Reads static graphs
             */
            while(line.size() > 0) {
                if(line.size() == 1) {
                    reverseNodeNames = new HashMap<String, Node>();
                    staticGraph = new CustomGraph();
                    String staticGraphName = line.get(0);
                    staticGraph.setName(staticGraphName);
                    dynamicGraph.addGraphIntoGraphSeries(staticGraph);
                    line = Adapters.readLine(reader);
                }else if(line.size() == 3){
                    String sourceNodeName = line.get(0);
                    Node sourceNode;
                    if (!reverseNodeNames.containsKey(sourceNodeName)) {
                        sourceNode = staticGraph.createNode();
                        reverseNodeNames.put(sourceNodeName, sourceNode);
                        staticGraph.setNodeName(sourceNode, sourceNodeName);
                    }
                    else {
                        sourceNode = reverseNodeNames.get(sourceNodeName);
                    }

                    String targetNodeName = line.get(1);
                    Node targetNode;
                    if (!reverseNodeNames.containsKey(targetNodeName)) {
                        targetNode = staticGraph.createNode();
                        reverseNodeNames.put(targetNodeName, targetNode);
                        staticGraph.setNodeName(targetNode, targetNodeName);
                    }
                    else {
                        targetNode = reverseNodeNames.get(targetNodeName);
                    }

                    String edgeWeightString = line.get(2);
                    double edgeWeight = Double.parseDouble(edgeWeightString);
                    Edge edge = staticGraph.createEdge(sourceNode, targetNode);
                    staticGraph.setEdgeWeight(edge, edgeWeight);
                    //dynamicGraph.addGraphIntoGraphSeries(staticGraph);
                    line = Adapters.readLine(reader);
                }
            }
            if(line.size() > 0) {
                throw new AdapterException("Invalid input format");
            }
            return dynamicGraph;
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


    @Override
    public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

    }
}
