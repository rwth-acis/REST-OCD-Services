package i5.las2peer.services.ocd.adapters.centralityOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing centrality output adapters using centrality output format objects as descriptors.
 *
 */
public class CentralityOutputAdapterFactory implements SimpleFactory<CentralityOutputAdapter, CentralityOutputFormat> {

	@Override
	public CentralityOutputAdapter getInstance(CentralityOutputFormat outputFormat)
			throws InstantiationException, IllegalAccessException {
		return outputFormat.getAdapterClass().newInstance();
	}

}
