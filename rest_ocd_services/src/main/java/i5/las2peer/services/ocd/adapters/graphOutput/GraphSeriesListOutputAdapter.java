package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Edge;
import y.base.EdgeCursor;

import java.io.Writer;
import java.util.List;

public class GraphSeriesListOutputAdapter extends AbstractGraphOutputAdapter {


    @Override
    public void writeGraph(CustomGraph graph) throws AdapterException {
        try {
            int order = 1;
            for(CustomGraph staticGraph : graph.getGraphSeries()) {
                writer.write(order + "_");
                order++;
                writer.write(String.valueOf(staticGraph.getId())+"_");
                writer.write(staticGraph.getName());

                writer.write("\n");
            }
        }
        catch (Exception e) {
            throw new AdapterException(e);
        }
        finally {
            try {
                writer.close();
            }
            catch (Exception e) {
            }
        }
    }
}
