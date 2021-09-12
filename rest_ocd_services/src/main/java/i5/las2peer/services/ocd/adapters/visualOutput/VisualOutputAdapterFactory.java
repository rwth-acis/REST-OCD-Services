package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing visual output adapters using visual output format objects as descriptors.
 * @author Sebastian
 *
 */
public class VisualOutputAdapterFactory implements SimpleFactory<VisualOutputAdapter, VisualOutputFormat>{

	@Override
	public VisualOutputAdapter getInstance(VisualOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
		return outputFormat.getAdapterClass().newInstance();
	}

}
