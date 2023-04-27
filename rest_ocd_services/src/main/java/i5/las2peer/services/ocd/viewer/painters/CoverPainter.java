package i5.las2peer.services.ocd.viewer.painters;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraphSequence;

/**
 * The common interface for all cover painters
 * Define and set the community colors of a cover.
 * @author Sebastian
 *
 */
public interface CoverPainter {
	
	/**
	 * Sets the community colors of a cover;
	 * @param cover The cover.
	 */
	void doPaint(Cover cover);

	void doPaintSequence(Cover cover, CustomGraphSequence customGraphSequence);
}
