package i5.las2peer.services.ocd.graphs;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic interaction extension to CustomEdge's
 *
 * @author fsaintpreux
 */
public class DynamicInteraction extends CustomEdge{

    /**
     * The column name for the timestamp.
     */
    private static final String dateColumnName = "DATE";
    /**
     * The column name for the action.
     */
    private static final String actionColumnName = "ACTION";
    /**
     * The collection name in the database for DynamicInteractions.
     */
    public static final String collectionName = "dynamicinteraction";
    /**
     * The edge date as a string
     */
    private String date;
    /**
     * The edge action as a string, either "+" or "-"
     */
    private String action;

    /**
     * Creates a new instance.
     */
    public DynamicInteraction() {
    }

    /**
     * Creates a new instance with every attribute as input.
     * @param source
     * @param target
     * @param date
     * @param action
     */
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

    /**
     * Updates the graph attribute before persisting DynamicInteraction.
     * @param graph
     */
    public void update(DynamicGraph graph) {
        this.setGraph(graph);
    }

    /**
     * Handles storing a DynamicInteractin into the database
     * @param db
     * @param opt
     */
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

    /**
     * Handles loading a DynamicInteractioj from the database
     * @param key
     * @param graph
     * @param customNodeKeyMap
     * @param db
     * @param opt
     * @return
     */
    public static DynamicInteraction load(String key, DynamicGraph graph, Map<String, CustomNode> customNodeKeyMap, ArangoDatabase db, DocumentReadOptions opt) {
        DynamicInteraction dynamicInteraction = new DynamicInteraction();
        ArangoCollection collection = db.collection(collectionName);
        BaseEdgeDocument bed = collection.getDocument(key, BaseEdgeDocument.class, opt);
        if(bed != null) {
            dynamicInteraction.key = bed.getKey();
            dynamicInteraction.setGraph(graph);
            CustomNode source = customNodeKeyMap.get(bed.getFrom());
            CustomNode target = customNodeKeyMap.get(bed.getTo());
            dynamicInteraction.setSource(source);
            dynamicInteraction.setTarget(target);
            dynamicInteraction.date = bed.getAttribute(dateColumnName).toString();
            dynamicInteraction.action = bed.getAttribute(actionColumnName).toString();
        } else {
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
