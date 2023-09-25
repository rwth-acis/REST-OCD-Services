package i5.las2peer.services.ocd.adapters.graphOutput;

import java.util.Iterator;

import org.graphstream.graph.Edge;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

public class UnweightedEdgeListMultiGraphOutputAdapter extends AbstractGraphOutputAdapter{


@Override
public void writeGraph(CustomGraph graph) throws AdapterException {
    try {
        Iterator<Edge> edges = graph.edges().iterator();
        Edge edge;
        while(edges.hasNext()) {
            edge = edges.next();
            writer.write(graph.getEdgeLayerId(edge) + " ");
            writer.write(graph.getNodeName(edge.getSourceNode()) + " ");
            writer.write(graph.getNodeName(edge.getTargetNode()));
            if(edges.hasNext()) {
                writer.write("\n");
            }

        }
        
    }
    catch (Exception e) {
        throw new AdapterException(e);
    }
    finally {
        try {
            writer.close();
        }
        catch (Exception e) {
        }
    }
}
 
    
}
