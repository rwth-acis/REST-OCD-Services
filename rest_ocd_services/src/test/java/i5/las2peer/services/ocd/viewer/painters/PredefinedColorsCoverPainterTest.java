package i5.las2peer.services.ocd.viewer.painters;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.visualOutput.SvgVisualOutputAdapter;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphSequence;
import i5.las2peer.services.ocd.graphs.OcdPersistenceLoadException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.viewer.LayoutHandler;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestConstants;
import i5.las2peer.services.ocd.viewer.testsUtil.ViewerTestGraphFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

public class PredefinedColorsCoverPainterTest {

	@Test
	public void testOnSawmill() throws AdapterException, IOException, InstantiationException, IllegalAccessException, InterruptedException {
		Cover cover = ViewerTestGraphFactory.getSlpaSawmillCover();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(cover, GraphLayoutType.ORGANIC, true, false, 20, 45, CoverPaintingType.PREDEFINED_COLORS);
		SvgVisualOutputAdapter adapter = new SvgVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.slpaSawmillSvgOutputPath));
		adapter.writeGraph(cover.getGraph());
	}
	
	@Test
	public void testOnDolphins() throws AdapterException, IOException, InstantiationException, IllegalAccessException, InterruptedException {
		Cover cover = ViewerTestGraphFactory.getSlpaDolphinsCover();
		LayoutHandler handler = new LayoutHandler();
		handler.doLayout(cover, GraphLayoutType.ORGANIC, true, false, 20, 45, CoverPaintingType.PREDEFINED_COLORS);
		SvgVisualOutputAdapter adapter = new SvgVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.slpaDolphinsSvgOutputPath));
		adapter.writeGraph(cover.getGraph());
	}

	@Test
	public void testOnSequence() throws IOException, InstantiationException, IllegalAccessException, InterruptedException, OcdPersistenceLoadException, AdapterException, ParseException {
		Database db = new Database(true);

		CustomGraph graph1 = OcdTestGraphFactory.getSequenceTestGraph(1);
        graph1.setUserName("testuser");
        String graph1Key = db.storeGraph(graph1);
        graph1 = db.getGraph("testuser", graph1Key);
        Cover cover1 = OcdTestGraphFactory.getSequenceTestCover(graph1,1);
        cover1 = db.getCover("testuser", graph1Key, db.storeCover(cover1));

        CustomGraph graph2 = OcdTestGraphFactory.getSequenceTestGraph(2);
        graph2.setUserName("testuser");
        String graph2Key = db.storeGraph(graph2);
        graph2 = db.getGraph("testuser", graph2Key);
        Cover cover2 = OcdTestGraphFactory.getSequenceTestCover(graph2,2);
        cover2 = db.getCover("testuser", graph2Key, db.storeCover(cover2));

        CustomGraphSequence sequence = new CustomGraphSequence(graph1, false);
        sequence.addGraphToSequence(1, graph2Key);

        // Generate sequence communities
        sequence.generateSequenceCommunities(db, "testuser", 0.2);

		LayoutHandler handler = new LayoutHandler();
		handler.doLayoutSequence(cover1, sequence, GraphLayoutType.ORGANIC, true, false, 20, 45, CoverPaintingType.PREDEFINED_COLORS);
		handler.doLayoutSequence(cover2, sequence, GraphLayoutType.ORGANIC, true, false, 20, 45, CoverPaintingType.PREDEFINED_COLORS);
		SvgVisualOutputAdapter adapter = new SvgVisualOutputAdapter();
		adapter.setWriter(new FileWriter(ViewerTestConstants.sequenceCover1OutputPath));
		adapter.writeGraph(cover1.getGraph());
		adapter.setWriter(new FileWriter(ViewerTestConstants.sequenceCover2OutputPath));
		adapter.writeGraph(cover2.getGraph());
	}
}
