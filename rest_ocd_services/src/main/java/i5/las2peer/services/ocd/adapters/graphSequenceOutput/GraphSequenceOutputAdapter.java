package i5.las2peer.services.ocd.adapters.graphSequenceOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraphSequence;
import i5.las2peer.services.ocd.utils.Database;

/**
 * The common interface of all graph sequence output adapters.
 * @author Max Kissgen
 *
 */
public interface GraphSequenceOutputAdapter extends OutputAdapter {

    /**
     * Writes a graph sequence and closes the writer.
     * @param customGraphSequence The graph sequence to write.
     * @throws AdapterException if the adapter failed
     */
    void writeGraphSequence(Database db, CustomGraphSequence customGraphSequence) throws AdapterException;

}
