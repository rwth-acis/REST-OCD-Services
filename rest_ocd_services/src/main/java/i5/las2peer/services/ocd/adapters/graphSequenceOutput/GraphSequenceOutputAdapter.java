package i5.las2peer.services.ocd.adapters.graphSequenceOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.GraphSequence;
import i5.las2peer.services.ocd.utils.Database;

/**
 * The common interface of all graph sequence output adapters.
 * @author Max Kissgen
 *
 */
public interface GraphSequenceOutputAdapter extends OutputAdapter {

    /**
     * Writes a graph sequence and closes the writer.
     * @param graphSequence The graph sequence to write.
     * @throws AdapterException if the adapter failed
     */
    void writeGraphSequence(Database db, GraphSequence graphSequence) throws AdapterException;

}
