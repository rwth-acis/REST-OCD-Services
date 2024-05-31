package i5.las2peer.services.ocd.adapters.clcOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.CommunityLifeCycle;

/**
 * The common interface of all clc output adapters.
 *
 */
public interface ClcOutputAdapter extends OutputAdapter {

    /**
     * Writes a clc and closes the writer.
     * @param clc The community life cycle.
     * @throws AdapterException if the adapter failed
     */
    public void writeClc(CommunityLifeCycle clc) throws AdapterException;

}
