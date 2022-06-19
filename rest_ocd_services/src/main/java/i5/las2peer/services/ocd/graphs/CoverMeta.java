package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;

import java.util.ArrayList;

/**
 * Instance of this class holds meta information about covers and is used
 * for efficient requests that don't require accessing full cover
 */
public class CoverMeta {

    /**
     * The id of the cover
     */
    private long id;

    /**
     * The name of the cover
     */
    private String name = "";

    /**
     * The number of communities of the cover
     */
    private Integer numberOfCommunities;

    /**
     * The id of the graph.
     */
    private Long graphId;

    /**
     * The name of the graph
     */
    private String graphName = "";

    /**
     * The graph creation log of the graph.
     */
    private CoverCreationLog coverCreationLog;

    /**
     * Metrics of the cover
     */
    private ArrayList<OcdMetricLog> metrics;

    public CoverMeta(long id, String name, Integer numberOfCommunities, Long graphId, String graphName, CoverCreationLog coverCreationLog, ArrayList<OcdMetricLog> metrics) {
        this.id = id;
        this.name = name;
        this.numberOfCommunities = numberOfCommunities;
        this.graphId = graphId;
        this.graphName = graphName;
        this.coverCreationLog = coverCreationLog;

        if(metrics != null) {
            this.metrics = metrics;
        }else{
            this.metrics = new ArrayList<>();
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getNumberOfCommunities() {
        return numberOfCommunities;
    }

    public Long getGraphId() {
        return graphId;
    }

    public String getGraphName() {
        return graphName;
    }

    public CoverCreationLog getCoverCreationLog() {
        return coverCreationLog;
    }

    public ArrayList<OcdMetricLog> getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return "CoverMeta{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", numberOfCommunities=" + numberOfCommunities +
                ", graphId=" + graphId +
                ", graphName='" + graphName + '\'' +
                ", graphCreationLog=" + coverCreationLog.getType().getDisplayName() +
                ", metrics=" + metrics +
                '}';
    }
}
