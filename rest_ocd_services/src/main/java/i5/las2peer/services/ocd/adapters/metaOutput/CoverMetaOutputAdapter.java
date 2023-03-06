package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CoverMeta;

/**
 * The common interface of graph output adapters.
 *
 */
public interface CoverMetaOutputAdapter extends OutputAdapter {
    /**
     * Writes a cover and closes the writer.
     * @param coverMeta TThe cover meta information to write.
     * @throws AdapterException if the adapter failed
     */
    public void writeCover(CoverMeta coverMeta) throws AdapterException;
}
