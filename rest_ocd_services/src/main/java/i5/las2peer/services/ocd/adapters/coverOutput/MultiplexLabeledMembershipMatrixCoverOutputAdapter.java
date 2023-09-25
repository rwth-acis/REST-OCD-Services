package i5.las2peer.services.ocd.adapters.coverOutput;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Node;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.MultiplexCommunity;

public class MultiplexLabeledMembershipMatrixCoverOutputAdapter extends AbstractCoverOutputAdapter {

    /**
     * Creates a new instance setting the writer attribute.
     * 
     * @param writer The writer used for output.
     */
    public MultiplexLabeledMembershipMatrixCoverOutputAdapter(Writer writer) {
        this.setWriter(writer);
    }

    public MultiplexLabeledMembershipMatrixCoverOutputAdapter() {
    }

    @Override
    public void writeCover(Cover cover) throws AdapterException {
        try {
            CustomGraph graph = cover.getGraph();
            Iterator<Node> nodes = graph.iterator();
            List<String> layers = graph.Layers();
            while (nodes.hasNext()) {
                Node node = nodes.next();
                String nodeName = graph.getNodeName(node);
                if (nodeName.isEmpty()) {
                    nodeName = Integer.toString(node.getIndex());
                }
                for (String layer : layers) {
                    writer.write(layer + " ");
                    writer.write(nodeName + " ");
                    for (int i = 0; i < cover.communityCount(); i++) {
                        MultiplexCommunity multiplexcommunity = (MultiplexCommunity) cover.getCommunities().get(i);
                        double belongingFactor = multiplexcommunity.getMultiplexBelongingFactor(layer, node);
                        writer.write(String.format("%.4f ", belongingFactor).replace(",", "."));
                    }
                        writer.write("\n");
                    
                }
            }
        } catch (Exception e) {
            throw new AdapterException(e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

}
