package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CLCMeta;

/**
 * The common interface of graph output adapters.
 *
 */
public interface CLCMetaOutputAdapter extends OutputAdapter {
    /**
     * Writes a clc and closes the writer.
     * @param clcMeta TThe cover meta information to write.
     * @throws AdapterException if the adapter failed
     */
    public void writeCLC(CLCMeta clcMeta) throws AdapterException;
}
