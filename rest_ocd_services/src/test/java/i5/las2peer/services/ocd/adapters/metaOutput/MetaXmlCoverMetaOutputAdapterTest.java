package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MetaXmlCoverMetaOutputAdapterTest {

    @Test
    public void writeCover() {

        String key = "coverTestKey";
        String username = "Alice";
        String name = "testCover";
        Integer numberOfCommunities = 7;
        String graphKey = "graphTestKey";
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
                key,
                name,
                numberOfCommunities,
                graphKey,
                graphName,
                coverCreationLog.getType().getId(),
                coverCreationLog.getStatus().getId());

        try {
            MetaXmlCoverMetaOutputAdapter adapter = new MetaXmlCoverMetaOutputAdapter();
            adapter.setWriter(new FileWriter(OcdTestConstants.testMetaXmlCoverMetaOutputPath));
            adapter.writeCover(coverMeta);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
