package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

public class GraphInputAdapterFactory implements SimpleFactory<GraphInputAdapter, GraphInputFormat>{

	@Override
	public GraphInputAdapter getInstance(GraphInputFormat inputFormat) throws InstantiationException, IllegalAccessException {
		return inputFormat.getAdapterClass().newInstance();
	}

}
