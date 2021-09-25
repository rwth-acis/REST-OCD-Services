package i5.las2peer.services.ocd.viewer.painters;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing cover painters using cover cover painter objects as descriptors.
 * @author Sebastian
 *
 */
public class CoverPainterFactory implements SimpleFactory<CoverPainter, CoverPaintingType>{

	@Override
	public CoverPainter getInstance(CoverPaintingType paintingType) throws InstantiationException, IllegalAccessException {
		return paintingType.getPainterClass().newInstance();
	}

}
