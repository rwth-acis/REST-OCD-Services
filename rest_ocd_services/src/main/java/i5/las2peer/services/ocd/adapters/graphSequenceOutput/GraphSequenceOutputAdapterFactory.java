package i5.las2peer.services.ocd.adapters.graphSequenceOutput;

import i5.las2peer.services.ocd.adapters.graphSequenceOutput.GraphSequenceOutputAdapter;
import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing graph sequence output adapters using graph output format objects as descriptors.
 * @author Max Kissgen
 *
 */
public class GraphSequenceOutputAdapterFactory implements SimpleFactory<GraphSequenceOutputAdapter, GraphSequenceOutputFormat> {

    @Override
    public GraphSequenceOutputAdapter getInstance(GraphSequenceOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
        return outputFormat.getAdapterClass().newInstance();
    }

}
