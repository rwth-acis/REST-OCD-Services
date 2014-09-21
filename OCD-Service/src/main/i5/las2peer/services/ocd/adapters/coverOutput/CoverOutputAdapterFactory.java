package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing cover output adapters using cover output format objects as descriptors.
 * @author Sebastian
 *
 */
public class CoverOutputAdapterFactory implements SimpleFactory<CoverOutputAdapter, CoverOutputFormat>{

	@Override
	public CoverOutputAdapter getInstance(CoverOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
		return outputFormat.getAdapterClass().newInstance();
	}

}
