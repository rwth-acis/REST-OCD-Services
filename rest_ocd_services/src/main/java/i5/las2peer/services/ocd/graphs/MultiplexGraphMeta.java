package i5.las2peer.services.ocd.graphs;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

/**
 * Instance of this class holds meta information about graphs and is used
 * for efficient requests that don't require accessing full graph
 */
public class MultiplexGraphMeta extends CustomGraphMeta {

    /**
     * Number of layers of the multiplex graph
     */
    private int layerCount;


    @ConstructorProperties({"key","userName","name","nodeCount","edgeCount", "types", "creationTypeId", "creationStatusId", "layerCount"})
    public MultiplexGraphMeta(String key, String userName, String name, Long nodeCount, Long edgeCount, ArrayList<Integer> types, int creationTypeId, int creationStatusId, int layerCount) {
        super(key, userName, name, nodeCount, edgeCount, types, creationTypeId, creationStatusId);
        this.layerCount = layerCount;
    }
    public int getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(int layerCount) {
        this.layerCount = layerCount;
    }

    @Override
    public String toString() {
        return super.toString() + ", layerCount=" + layerCount;
    }
}
