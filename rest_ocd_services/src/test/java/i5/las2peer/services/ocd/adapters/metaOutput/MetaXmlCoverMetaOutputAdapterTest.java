package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CoverMeta;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.junit.Test;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MetaXmlCoverMetaOutputAdapterTest {

    @Test
    public void writeCover() {

        Long id = Long.valueOf(3);
        String username = "Alice";
        String name = "testCover";
        Integer numberOfCommunities = 7;
        Long graphId = Long.valueOf(1);
        String graphName = "testGraph";
        ArrayList<Integer> graphTypes = new ArrayList<Integer>();
        graphTypes.add(0);
        graphTypes.add(1);
        HashSet<GraphType> compatibleGraphTypes = new HashSet<GraphType>();
        compatibleGraphTypes.add(GraphType.DIRECTED);
        compatibleGraphTypes.add(GraphType.WEIGHTED);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("testParameter1", "0.5");
        parameters.put("testparameter2", "0.85");
        CoverCreationLog coverCreationLog = new CoverCreationLog(CoverCreationType.GROUND_TRUTH, parameters,compatibleGraphTypes);
        coverCreationLog.setStatus(ExecutionStatus.COMPLETED);


        CoverMeta coverMeta = new CoverMeta(
                id,
                name,
                numberOfCommunities,
                graphId,
                graphName,
                coverCreationLog,
                new ArrayList<OcdMetricLog>());

        try {
            MetaXmlCoverMetaOutputAdapter adapter = new MetaXmlCoverMetaOutputAdapter();
            adapter.setWriter(new FileWriter(OcdTestConstants.testMetaXmlCoverMetaOutputPath));
            adapter.writeCover(coverMeta);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}