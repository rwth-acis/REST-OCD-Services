package i5.las2peer.services.ocd.adapters.graphSequenceOutput;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.MetaXmlGraphOutputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphSequence;
import i5.las2peer.services.ocd.graphs.OcdPersistenceLoadException;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Database;

public class MetaXmlGraphSequenceOutputAdapterTest {
    @Test
	public void test() throws IOException, AdapterException, OcdPersistenceLoadException, ParseException {
        Database db = new Database(true);    

		CustomGraph graph1 = OcdTestGraphFactory.getSequenceTestGraph(1);
        graph1.setUserName("testuser");
        String graph1Key = db.storeGraph(graph1);
        graph1 = db.getGraph("testuser", graph1Key);
        CustomGraph graph2 = OcdTestGraphFactory.getSequenceTestGraph(2);
        graph2.setUserName("testuser");
        String graph2Key = db.storeGraph(graph2);
        graph2 = db.getGraph("testuser", graph2Key);
        CustomGraphSequence graphSequence = new CustomGraphSequence(graph1, false);
        graphSequence.addGraphToSequence(1, graph2);

		GraphSequenceOutputAdapter adapter = new MetaXmlGraphSequenceOutputAdapter();
		adapter.setWriter(new FileWriter(OcdTestConstants.sequenceMetaXmlOutputPath));
		adapter.writeGraphSequence(db, graphSequence);
	}
}
