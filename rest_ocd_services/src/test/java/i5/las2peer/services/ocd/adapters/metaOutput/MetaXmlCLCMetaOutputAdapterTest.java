package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.graphs.CLCMeta;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MetaXmlCLCMetaOutputAdapterTest {

    @Test
    public void writeCover() {

        String key = "clcTestKey";
        String username = "Alice";
        String name = "testCLC";
        Integer numberOfEvents = 35;
        String graphKey = "graphTestKey";
        String graphName = "testGraph";
        String coverKey = "coverKey";
        String coverName = "testCover";

        ArrayList<Integer> graphTypes = new ArrayList<>();
        graphTypes.add(7);

        HashSet<GraphType> compatibleGraphTypes = new HashSet<>();
        compatibleGraphTypes.add(GraphType.DYNAMIC);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("testParameter1", "0.5");
        parameters.put("testParameter2", "3");
        parameters.put("testParameter3", "0.5");

        CoverCreationLog coverCreationLog = new CoverCreationLog(CoverCreationType.ILCD_ALGORITHM, parameters, compatibleGraphTypes);
        coverCreationLog.setStatus(ExecutionStatus.COMPLETED);

        CLCMeta clcMeta = new CLCMeta(key,name,graphKey,graphName,coverKey,coverName,coverCreationLog.getType().getId(),coverCreationLog.getStatus().getId());

        try{
            MetaXmlCLCMetaOutputAdapter adapter = new MetaXmlCLCMetaOutputAdapter();
            adapter.setWriter(new FileWriter(OcdTestConstants.testMetaXmlCLCMetaOutputPath));
            adapter.writeCLC(clcMeta);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
