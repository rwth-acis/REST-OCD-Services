package i5.las2peer.services.ocd.viewer.painters;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.visualOutput.SvgVisualOutputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.viewer.LayoutHandler;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestConstants;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class RandomColorPainterTest {

	@Test
	public void testOnSawmill() throws AdapterException, IOException, InstantiationException, IllegalAccessException, InterruptedException {
		Cover cover = ViewerTestGraphFactory.getSlpaSawmillCover();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(cover, GraphLayoutType.ORGANIC, true, false, 20, 45, CoverPaintingType.RANDOM_COLORS, 10);
		SvgVisualOutputAdapter adapter = new SvgVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.slpaSawmillSvgOutputPath));
		adapter.writeGraph(cover.getGraph());
	}
	
	@Test
	public void testOnDolphins() throws AdapterException, IOException, InstantiationException, IllegalAccessException, InterruptedException {
		Cover cover = ViewerTestGraphFactory.getSlpaDolphinsCover();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(cover, GraphLayoutType.ORGANIC, true, false, 20, 45, CoverPaintingType.RANDOM_COLORS, 10);
		SvgVisualOutputAdapter adapter = new SvgVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.slpaDolphinsSvgOutputPath));
		adapter.writeGraph(cover.getGraph());
	}

}
