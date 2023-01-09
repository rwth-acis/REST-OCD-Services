package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.centrality.data.CentralityMeta;

/**
 * The common interface of centrality output adapters.
 *
 */
public interface CentralityMetaOutputAdapter extends OutputAdapter {

    /**
     * Writes a CentralityMap and closes the writer.
     * @param centralityMeta The CentralityMeta instance holding meta information about centrality.
     * @throws AdapterException if the adapter failed
     */
    public void writeCentralityMap(CentralityMeta centralityMeta) throws AdapterException;
}
