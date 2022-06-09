package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

public class CoverSeriesListCoverOutputAdapter extends AbstractCoverOutputAdapter{

    @Override
    public void writeCover(Cover cover) throws AdapterException {
        try {
            int order = 1;
            for(Cover staticCover : cover.getCoverSeries()) {

                writer.write(order + "_");
                order++;
                writer.write(String.valueOf(staticCover.getId())+"_");
                writer.write(String.valueOf(staticCover.getName())+"_");
                writer.write(String.valueOf(staticCover.getGraph().getId())+"_");
                writer.write(String.valueOf(staticCover.getGraph().getName()));

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
