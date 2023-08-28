package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.graphs.CustomGraphMeta;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MetaXmlGraphMetaOutputAdapterTest {

    @Test
    public void writeGraph() {


        String key = "testKey";
        String username = "Alice";
        String name = "testGraph";
        Long nodeCount = Long.valueOf(5);
        Long edgeCount = Long.valueOf(10);
        ArrayList<Integer> graphTypes = new ArrayList<Integer>();
        graphTypes.add(0);
        graphTypes.add(1);
        GraphCreationLog graphCreationLog = new GraphCreationLog(GraphCreationType.REAL_WORLD, new HashMap<String,String>());
        graphCreationLog.setStatus(ExecutionStatus.COMPLETED);

        CustomGraphMeta graphMeta = new CustomGraphMeta(
                key,
                username,
                name,
                nodeCount,
                edgeCount,
                graphTypes,
                graphCreationLog.getType().getId(),
                graphCreationLog.getStatus().getId()
        );

        try {
            MetaXmlGraphMetaOutputAdapter adapter = new MetaXmlGraphMetaOutputAdapter();
            adapter.setWriter(new FileWriter(OcdTestConstants.testMetaXmlGraphMetaOutputPath));
            adapter.writeGraph(graphMeta);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
