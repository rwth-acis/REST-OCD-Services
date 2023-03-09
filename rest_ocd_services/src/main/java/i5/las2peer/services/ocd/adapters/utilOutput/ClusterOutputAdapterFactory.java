package i5.las2peer.services.ocd.adapters.utilOutput;

import i5.las2peer.services.ocd.adapters.utilOutput.ClusterOutputAdapter;
import i5.las2peer.services.ocd.adapters.utilOutput.ClusterOutputFormat;
import i5.las2peer.services.ocd.utils.SimpleFactory;

import java.lang.reflect.InvocationTargetException;

public class ClusterOutputAdapterFactory implements SimpleFactory<ClusterOutputAdapter, ClusterOutputFormat> {

    @Override
    public ClusterOutputAdapter getInstance(ClusterOutputFormat outputFormat) throws InstantiationException, IllegalAccessException {
        try {
            return outputFormat.getAdapterClass().getDeclaredConstructor().newInstance();
        }
        catch (NoSuchMethodException | InvocationTargetException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

}
