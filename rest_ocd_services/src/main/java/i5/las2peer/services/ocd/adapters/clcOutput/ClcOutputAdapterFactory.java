package i5.las2peer.services.ocd.adapters.clcOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing clc output adapters using clc output format objects as descriptors.
 *
 */
public class ClcOutputAdapterFactory implements SimpleFactory<ClcOutputAdapter, ClcOutputFormat>{

    @Override
    public ClcOutputAdapter getInstance(ClcOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
        return outputFormat.getAdapterClass().newInstance();
    }

}
