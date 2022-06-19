package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeta;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.junit.Test;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

public class MetaXmlCentralityMetaOutputAdapterTest {

    @Test
    public void writeCentralityMap() {

        Long id = Long.valueOf(1);

        String centralityName = "testCentrality";
        Long graphId = Long.valueOf(1);
        Long graphSize = Long.valueOf(10);
        String graphName = "testGraph";
        HashSet<GraphType> compatibleGraphTypes = new HashSet<GraphType>();
        compatibleGraphTypes.add(GraphType.WEIGHTED);
        CentralityCreationLog centralityCreationLog = new CentralityCreationLog(CentralityMeasureType.DEGREE_CENTRALITY,  CentralityCreationType.CENTRALITY_MEASURE, new HashMap<String,String>(),compatibleGraphTypes );
        centralityCreationLog.setExecutionTime(1);
        centralityCreationLog.setStatus(ExecutionStatus.COMPLETED);

        CentralityMeta centralityMeta = new CentralityMeta(
                id,
                centralityName,
                centralityCreationLog,
                graphId,
                graphName,
                graphSize
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