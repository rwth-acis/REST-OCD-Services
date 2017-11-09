package i5.las2peer.services.ocd.adapters.centralityInput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing cover input adapters using centrality input format objects as descriptors.
 * @author Tobias
 *
 */
public class CentralityInputAdapterFactory implements SimpleFactory<CentralityInputAdapter, CentralityInputFormat>{

	@Override
	public CentralityInputAdapter getInstance(CentralityInputFormat inputFormat) throws InstantiationException, IllegalAccessException {
		return inputFormat.getAdapterClass().newInstance();
	}

}
