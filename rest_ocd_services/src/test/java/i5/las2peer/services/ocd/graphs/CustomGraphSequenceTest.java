package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.ThreadHandler;
import org.graphstream.graph.Node;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class CustomGraphSequenceTest {

    @Test(expected = OcdPersistenceLoadException.class)
    public void graphRemovalTest() throws ParseException, OcdPersistenceLoadException {
        Database db = new Database(true);

        CustomGraph graph = new CustomGraph();
        Node node0 = graph.addNode("0");
        Node node1 = graph.addNode("1");
        graph.addEdge(UUID.randomUUID().toString(), node0, node1);
        graph.addEdge(UUID.randomUUID().toString(), node1, node0);
        graph.addEdge(UUID.randomUUID().toString(), node0, node0);
        Node node2 = graph.addNode("2");
        graph.addEdge(UUID.randomUUID().toString(), node0, node2);
        graph.addEdge(UUID.randomUUID().toString(), node1, node2);
        graph.setUserName("testuser");
        String graphKey = db.storeGraph(graph);
        graph = db.getGraph("testuser",graphKey);

        CustomGraph graph2 = new CustomGraph(graph);
        String graph2Key = db.storeGraph(graph2);

        CustomGraphSequence sequence = new CustomGraphSequence(graph, false);
        sequence.addGraphToSequence(1, graph2Key);
        sequence.setUserName("testuser");
        String sequenceKey = db.storeGraphSequence(sequence);

        sequence.deleteGraphFromSequence(db, graphKey, new ArrayList<Cover>(), new ThreadHandler());
        assertEquals(sequence.getCustomGraphKeys().size(), 1);
        sequence.deleteGraphFromSequence(db, graph2Key, new ArrayList<Cover>(), new ThreadHandler());
        assertEquals(sequence.getCustomGraphKeys().size(), 0);
        db.getGraphSequence("testuser", sequenceKey);

        db.deleteDatabase();
    }

    @Test
    public void generateSequenceCommunitiesTest() throws Exception {
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
        sequence.generateSequenceCommunities(db, "testuser", 0.2); //TODO: Test similarity threshold, make it settable
        db.storeGraphSequence(sequence);

        System.out.println(sequence.getSequenceCommunityColorMap() + " " + sequence.getSequenceCommunityColorMap().isEmpty());
        System.out.println(sequence.getCommunitySequenceCommunityMap());
        assertTrue(sequence.getCommunitySequenceCommunityMap().get(cover1.getCommunities().get(0).getKey())
            == sequence.getCommunitySequenceCommunityMap().get(cover2.getCommunities().get(0).getKey())); // Check that communities are assigned the same sequence community

        db.deleteDatabase();
    }
}
