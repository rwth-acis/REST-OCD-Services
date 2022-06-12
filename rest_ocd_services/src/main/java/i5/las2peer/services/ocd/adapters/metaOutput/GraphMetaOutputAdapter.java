package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraphMeta;


/**
 * The common interface of graph output adapters.
 *
 */
public interface GraphMetaOutputAdapter extends OutputAdapter {

    /**
     * Writes a graph meta and closes the writer.
     * @param graphMeta The graph meta information to write.
     * @throws AdapterException if the adapter failed
     */
    public void writeGraph(CustomGraphMeta graphMeta) throws AdapterException;

}
