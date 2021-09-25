package i5.las2peer.services.ocd.viewer.layouters;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing graph layouters using graph layout type objects as descriptors.
 * @author Sebastian
 *
 */
public class GraphLayouterFactory implements SimpleFactory<GraphLayouter, GraphLayoutType>{

	@Override
	public GraphLayouter getInstance(GraphLayoutType layoutType) throws InstantiationException, IllegalAccessException {
		return layoutType.getLayouterClass().newInstance();
	}

}
