package i5.las2peer.services.ocd.adapters.utilOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Writer;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The common interface of all graph output adapters.
 * @author Sebastian
 *
 */
public interface ClusterOutputAdapter extends OutputAdapter {

    /**
     * Writes a graph and closes the writer.
     * @throws AdapterException if the adapter failed
     */
    void writeCluster() throws AdapterException;

    /**
     * Sets both the graph and possible additional parameters
     * @param graphParam the graph to use
     * @param param Some additional parameters necessary for producing the clusters, e.g. an attribute to sort by
     * @throws IllegalArgumentException if some parameters are unusable
     */
    void setParameter(CustomGraph graphParam, Map<String, String> param) throws IllegalArgumentException, ParseException;

    default void setListParameter(Map<String, List<String>> listParam) throws IllegalArgumentException, ParseException {};

}