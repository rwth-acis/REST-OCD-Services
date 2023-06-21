package i5.las2peer.services.ocd.graphs;

import java.beans.ConstructorProperties;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class CustomGraphTimedMeta extends CustomGraphMeta{
    /**
     * The start date of the timed graph
     */
    private Date startDate;

    /**
     * The end date of the timed graph
     */
    private Date endDate;

    /**
     * Constructor that is used to generate a CustomGraphTimedMeta instance
     * using the JSON input resulting from ArangoDB queries
     *
     * @param key              Key of the graph
     * @param userName         Creator of the graph
     * @param name             Name of the graph
     * @param nodeCount        Node count of the graph
     * @param edgeCount        Edge count of the graph
     * @param types            Array of graph types
     * @param creationTypeId   Id of the graph creation log
     * @param creationStatusId Status of the graph creation log
     * @param startDate Start date of the timed graph
     * @param endDate End date of the timed graph
     */
    @ConstructorProperties({"key","userName","name","nodeCount","edgeCount", "types", "startDate", "endDate", "creationTypeId", "creationStatusId"})
    public CustomGraphTimedMeta(String key, String userName, String name, Long nodeCount, Long edgeCount, ArrayList<Integer> types, Date startDate, Date endDate, int creationTypeId, int creationStatusId) {
        super(key, userName, name, nodeCount, edgeCount, types, creationTypeId, creationStatusId);
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE_TIME;
        return "CustomGraphMeta{" +
                "key=" + this.getKey() +
                ", userName='" + this.getUserName() + '\'' +
                ", name='" + this.getName() + '\'' +
                ", nodeCount=" + this.getNodeCount() +
                ", edgeCount=" + this.getEdgeCount() +
                ", types=" + types +
                ", startDate=" + dateFormatter.format(startDate.toInstant()) +
                ", endDate=" + dateFormatter.format(endDate.toInstant()) +
                ", creationType=" + creationTypeId +
                ", creationStatus=" + creationStatusId +
                '}';
    }
}
