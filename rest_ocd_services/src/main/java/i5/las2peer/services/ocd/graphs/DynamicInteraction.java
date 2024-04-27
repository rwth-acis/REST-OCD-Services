package i5.las2peer.services.ocd.graphs;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Dynamic interaction extension
 *
 * @author fsaintpreux
 */
public class DynamicInteraction extends CustomEdge{

    private static final String dateColumnName = "DATE";
    private static final String actionColumnName = "ACTION";

    public static final String collectionName = "dynamicinteraction";

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

    public DynamicInteraction(CustomNode source, CustomNode target, String date, String action) {
        this.setSource(source);
        this.setTarget(target);
        this.date = date;
        this.action = action;
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

    public void update(DynamicGraph graph) {
        this.setGraph(graph);
    }
    @Override
    public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
        ArangoCollection collection = db.collection(collectionName);
        BaseEdgeDocument bed = new BaseEdgeDocument();
        bed.addAttribute(graphKeyColumnName, this.getGraph().getKey());
        bed.addAttribute(dateColumnName, this.date);
        bed.addAttribute(actionColumnName, this.action);
        bed.setFrom(CustomNode.collectionName + "/" + this.getSource().getKey());
        bed.setTo(CustomNode.collectionName + "/" + this.getTarget().getKey());

        collection.insertDocument(bed, opt);
        this.key = bed.getKey();
    }

    public static DynamicInteraction load(BaseEdgeDocument bed, CustomNode source, CustomNode target, DynamicGraph graph, ArangoDatabase db) {
        DynamicInteraction dynamicInteraction = new DynamicInteraction();

        if (bed != null) {
            dynamicInteraction.key = bed.getKey();
            dynamicInteraction.setGraph(graph);
            if(bed.getAttribute(dateColumnName)!=null) {
                dynamicInteraction.date = bed.getAttribute(dateColumnName).toString();
            }
            if(bed.getAttribute(actionColumnName) != null) {
                dynamicInteraction.action = bed.getAttribute(actionColumnName).toString();
            }
            dynamicInteraction.setSource(source);
            dynamicInteraction.setTarget(target);
        }
        else {
            System.out.println("Empty Document");
        }
        return dynamicInteraction;

    }

    @Override
    public String toString() {
        return "DynamicInteraction{" +
                "source=" + this.getSource() +
                ", target=" + this.getTarget() +
                ", date='" + date + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
