package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeta;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

public class MetaXmlCentralityMetaOutputAdapterTest {

    @Test
    public void writeCentralityMap() {

        String centralityKey = "centralityTestKey";
        String centralityName = "testCentrality";
        String graphKey = "graphTestKey";
        String graphName = "testGraph";
        HashSet<GraphType> compatibleGraphTypes = new HashSet<GraphType>();
        Long executionTime = Long.valueOf(20);
        compatibleGraphTypes.add(GraphType.WEIGHTED);
        CentralityCreationLog centralityCreationLog = new CentralityCreationLog(CentralityMeasureType.DEGREE_CENTRALITY,  CentralityCreationType.CENTRALITY_MEASURE, new HashMap<String,String>(),compatibleGraphTypes );
        centralityCreationLog.setExecutionTime(1);
        centralityCreationLog.setStatus(ExecutionStatus.COMPLETED);

        CentralityMeta centralityMeta = new CentralityMeta(
                centralityKey,
                centralityName,
                graphKey,
                graphName,
                centralityCreationLog.getCreationType().getId(),
                centralityCreationLog.getStatus().getId(),
                executionTime
        );

        try {
            MetaXmlCentralityMetaOutputAdapter adapter = new MetaXmlCentralityMetaOutputAdapter();
            adapter.setWriter(new FileWriter(OcdTestConstants.testMetaXmlMetricMetaOutputPath));
            adapter.writeCentralityMap(centralityMeta);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
