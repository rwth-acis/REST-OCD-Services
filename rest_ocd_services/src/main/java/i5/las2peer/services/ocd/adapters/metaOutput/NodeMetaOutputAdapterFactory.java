package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing graph sequence output adapters using graph output format objects as descriptors.
 * @author Max Kissgen
 *
 */
public class NodeMetaOutputAdapterFactory implements SimpleFactory<NodeMetaOutputAdapter, NodeMetaOutputFormat> {

    @Override
    public NodeMetaOutputAdapter getInstance(NodeMetaOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
        return outputFormat.getAdapterClass().newInstance();
    }

}
