package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing graph input adapters using graph input format objects as descriptors.
 * @author Sebastian
 *
 */
public class GraphInputAdapterFactory implements SimpleFactory<GraphInputAdapter, GraphInputFormat>{

	@Override
	public GraphInputAdapter getInstance(GraphInputFormat inputFormat) throws InstantiationException, IllegalAccessException {
		return inputFormat.getAdapterClass().newInstance();
	}

}
