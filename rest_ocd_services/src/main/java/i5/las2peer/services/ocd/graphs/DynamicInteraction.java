package i5.las2peer.services.ocd.graphs;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Dynamic interaction extension
 *
 * @author fsaintpreux
 */
public class DynamicInteraction {
    private Node source;
    private Node target;

    /**
     * The edge date as a string
     */
    private String date;

    /**
     * The edge action as a string, either "+" or "-"
     */
    private String action;

    public DynamicInteraction() {
    }


    public DynamicInteraction(DynamicInteraction dynamicInteraction) {
        this.source = dynamicInteraction.source;
        this.target = dynamicInteraction.target;
        this.action = dynamicInteraction.action;
        this.date = dynamicInteraction.date;
    }

    public DynamicInteraction(Edge edge, String date, String action) {
        this.source = edge.getSourceNode();
        this.target = edge.getTargetNode();
        this.date = date;
        this.action = action;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    /**
     * Getter for the date.
     * @return date of the edge
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter for the date.
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Getter for the action.
     * @return action of the edge
     */
    public String getAction() {
        return action;
    }

    /**
     * Setter for the action.
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "DynamicInteraction{" +
                "source=" + source +
                ", target=" + target +
                ", date='" + date + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
