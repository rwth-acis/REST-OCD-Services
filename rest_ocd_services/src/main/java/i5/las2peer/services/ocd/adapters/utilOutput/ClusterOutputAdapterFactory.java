package i5.las2peer.services.ocd.adapters.utilOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

import java.lang.reflect.InvocationTargetException;

public class ClusterOutputAdapterFactory implements SimpleFactory<ClusterOutputAdapter, ClusterCreationType> {

    @Override
    public ClusterOutputAdapter getInstance(ClusterCreationType outputFormat) throws InstantiationException, IllegalAccessException {
        try {
            return outputFormat.getAdapterClass().getDeclaredConstructor().newInstance();
        }
        catch (NoSuchMethodException | InvocationTargetException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

}
