package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomNodeMeta;
import i5.las2peer.services.ocd.utils.Database;

/**
 * The common interface of all node output adapters.
 * @author Max Kissgen
 *
 */
public interface NodeMetaOutputAdapter extends OutputAdapter {
    /**
     * Writes a node and closes the writer.
     * @param nodeMeta The node to write.
     * @throws AdapterException if the adapter failed
     */
    void writeNodeMeta(CustomNodeMeta nodeMeta) throws AdapterException;
}
