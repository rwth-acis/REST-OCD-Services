package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.graphstream.graph.Node;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.*;

public class ABACUSAlgorithmTest {

    @Test
    public void test() throws InterruptedException {

        CustomGraph representativeGraph = getRepresentativeGraph(6);

        Community mockCommunity1 = mock(Community.class);
        when(mockCommunity1.getMemberIndices()).thenReturn(Arrays.asList(1, 2, 3));
        when(mockCommunity1.getKey()).thenReturn("1");

        Community mockCommunity2 = mock(Community.class);
        when(mockCommunity2.getMemberIndices()).thenReturn(Arrays.asList(1, 2, 3, 4, 5));
        when(mockCommunity2.getKey()).thenReturn("2");

        Community mockCommunity3 = mock(Community.class);
        when(mockCommunity3.getMemberIndices()).thenReturn(Arrays.asList(1, 2, 3));
        when(mockCommunity3.getKey()).thenReturn("3");

        Community mockCommunity4 = mock(Community.class);
        when(mockCommunity4.getMemberIndices()).thenReturn(Arrays.asList(2, 3, 4, 5));
        when(mockCommunity4.getKey()).thenReturn("4");

        Community mockCommunity5 = mock(Community.class);
        when(mockCommunity5.getMemberIndices()).thenReturn(Arrays.asList(0));
        when(mockCommunity5.getKey()).thenReturn("5");

        Cover mockCover = mock(Cover.class);
        when(mockCover.getCommunities()).thenReturn(Arrays.asList(mockCommunity1, mockCommunity2, mockCommunity3, mockCommunity4, mockCommunity5));

        Database mockDB = mock(Database.class);
        when(mockDB.getLayerCovers(anyString())).thenReturn(Collections.singletonList(mockCover));
        ABACUSAlgorithm abacus = new ABACUSAlgorithm();
        abacus.setDatabase(mockDB);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("threshold", "2");
        abacus.setParameters(parameters);

        Cover actualCover = abacus.detectOverlappingCommunities(representativeGraph);
        System.out.println("Actual Cover:");
        System.out.println(actualCover.toString());
    }

    public static CustomGraph getRepresentativeGraph(int numberOfNodes) {
        CustomGraph graph = new CustomGraph();
        Node n[] = new Node[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            n[i] = graph.addNode(Integer.toString(i));
            graph.setNodeName(n[i], Integer.toString(i));
        }

        graph.setNodeEdgeCountColumnFields();
        GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
        log.setStatus(ExecutionStatus.COMPLETED);
        graph.setCreationMethod(log);
        return graph;
    }
}
