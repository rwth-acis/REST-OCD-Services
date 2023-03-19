package i5.las2peer.services.ocd.graphs;

import net.minidev.json.JSONObject;

import java.beans.ConstructorProperties;

/**
 * Instance of this class holds meta information about nodes and is used
 * for efficient requests that don't require accessing the full graph of the node
 */
public class CustomNodeMeta {

    /**
     * database key of the CustomNode to which metadata belongs
     */
    private String key;

    private String graphKey;

    /**
     * The name of the node.
     */
    private String name;

    /**
     * The nodes extraInfo attribute
     */
    private JSONObject extraInfo = new JSONObject();




    /**
     * Constructor that is used to generate a CustomNodeMeta instance
     * using the JSON input resulting from ArangoDB queries
     *
     * @param key               Key of the node
     * @param name              Name of the node
     * @param graphKey          Key of the nodes graph
     * @param extraInfo         Extra info of the node
     */
    @ConstructorProperties({"key", "name", "graphKey", "extraInfo"})
    public CustomNodeMeta(String key, String name, String graphKey, JSONObject extraInfo) {
        this.key = key;
        this.name = name;
        this.graphKey = graphKey;
        this.extraInfo = extraInfo;
    }

    public String getKey() {return key;}

    public void setKey(String key) {this.key = key;}

    public String getUserName() {
        return name;
    }

    public void setUserName(String userName) {
        this.name = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGraphKey() {
        return graphKey;
    }

    public void setGraphKey(String graphKey) {
        this.graphKey = graphKey;
    }

    public JSONObject getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(JSONObject extraInfo) {
        this.extraInfo = extraInfo;
    }

    @Override
    public String toString() {
        return "CustomNodeMeta{" +
                "key=" + key +
                ", name='" + name + '\'' +
                ", graphKey='" + graphKey + '\'' +
                ",\n extraInfo=" + extraInfo.toJSONString() +
                '}';
    }
}
