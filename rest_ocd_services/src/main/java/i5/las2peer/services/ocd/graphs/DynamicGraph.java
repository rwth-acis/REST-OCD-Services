package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.MultiNode;

import java.util.*;

public class DynamicGraph extends CustomGraph{
    /**
     * Dynamic graph extension
     * Extends CustomGraph's by a list of interactions
     */

    public static final String dynInKeysColumnName = "DYNAMICINTERACTION_KEYS";
    private List<DynamicInteraction> dynamicInteractions = new ArrayList<>();

    public DynamicGraph() {
    }

    public DynamicGraph(CustomGraph graph, List<DynamicInteraction> dynamicInteractions) {
        super(graph);
        this.dynamicInteractions = dynamicInteractions;
    }

    public DynamicGraph(String key) {
        super(key);
    }

    public DynamicGraph(AbstractGraph graph) {
        super(graph);
    }

    public DynamicGraph(DynamicGraph graph) {
        super(graph);
        this.dynamicInteractions = graph.dynamicInteractions;
    }

    /**
     * The getter for the list of dynamic interactions
     * @return list
     */
    public List<DynamicInteraction> getDynamicInteractions() {
        return dynamicInteractions;
    }

    /**
     * The setter for the list of dynamic interactions
     * @param dynamicInteractions
     */
    public void setDynamicInteractions(List<DynamicInteraction> dynamicInteractions) {
        this.dynamicInteractions = dynamicInteractions;
    }

    /**
     * @return true if the graph is dynamic
     */
    public boolean isDynamic() {
        return isOfType(GraphType.DYNAMIC);
    }

    public void addDynamicInteraction(Edge edge, String date, String action) {
        CustomNode source = this.getCustomNode(edge.getSourceNode());
        CustomNode target = this.getCustomNode(edge.getTargetNode());
        DynamicInteraction dynamicInteraction = new DynamicInteraction(source, target, date, action);
        this.dynamicInteractions.add(dynamicInteraction);
    }

    @Override
    public void persist(ArangoDatabase db, String transId) throws InterruptedException {
        super.persist(db, transId);
        for(DynamicInteraction dynIn: dynamicInteractions) {
            dynIn.update(this);
        }
        ArangoCollection collection = db.collection(collectionName);
        BaseDocument bd = new BaseDocument();
        DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
        DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);

        List<String> dynInteractionKeyList = new ArrayList<String>();
        for(DynamicInteraction di : this.dynamicInteractions) {
            di.persist(db, createOptions);
            dynInteractionKeyList.add(di.getKey());
        }

        bd.addAttribute(dynInKeysColumnName, dynInteractionKeyList);

        collection.updateDocument(this.getKey(), bd, updateOptions);
    }
}
