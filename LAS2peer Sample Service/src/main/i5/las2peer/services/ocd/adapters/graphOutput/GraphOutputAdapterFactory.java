package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

public class GraphOutputAdapterFactory implements SimpleFactory<GraphOutputAdapter, GraphOutputFormat>{

	@Override
	public GraphOutputAdapter getInstance(GraphOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
		return outputFormat.getAdapterClass().newInstance();
	}

}
