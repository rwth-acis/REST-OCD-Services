package i5.las2peer.services.ocd.adapters.centralityOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

import java.util.Iterator;

/**
 * Output class for centralities into csv files. Simply has one column for node names and one for the centrality values
 */
public class ValueCsvOutputAdapter extends AbstractCentralityOutputAdapter {

    @Override
    public void writeCentralityMap(CentralityMap map) throws AdapterException {
        try {
            CustomGraph graph = map.getGraph();
            Iterator<Node> nodes = graph.nodes().iterator();
            Node node;
            while(nodes.hasNext()) {
                node = nodes.next();

                writer.write(graph.getNodeName(node).replace(" ", "_") + "," + ((Double)map.getNodeValue(node)));
                if (nodes.hasNext()) {
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
