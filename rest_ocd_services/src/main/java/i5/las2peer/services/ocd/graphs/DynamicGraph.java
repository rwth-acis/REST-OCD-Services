package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import org.glassfish.jersey.internal.inject.Custom;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.MultiNode;

import java.util.*;
/**
 * Dynamic graph extension
 * Extends CustomGraph's by a list of interactions
 *
 * @author fsaintpreux
 */
public class DynamicGraph extends CustomGraph{
    /**
     *  Column name in the database for the keys of the graphs dynamic interactions.
     */
    public static final String dynInKeysColumnName = "DYNAMICINTERACTION_KEYS";
    /**
     *  List of dynamic interactions with source, target, timestamp and action.
     */
    private List<DynamicInteraction> dynamicInteractions = new ArrayList<>();

    /**
     *  Creates a new instance
     */
    public DynamicGraph() {
    }

    /**
     * Creates a new instance from a given CustomGraph and a list of dynamic interactions
     * @param graph
     * @param dynamicInteractions
     */
    public DynamicGraph(CustomGraph graph, List<DynamicInteraction> dynamicInteractions) {
        super(graph);
        this.dynamicInteractions = dynamicInteractions;
    }

    /**
     * Creates a new instance with a given CustomGraph. Used for loading DynamicGraphs from the database.
     * @param graph
     */
    public DynamicGraph(CustomGraph graph){
        super(graph);
    }

    /**
     * Creates a new instance with given persistence key. The name attribute will be a random UUID
     */
    public DynamicGraph(String key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param graph
     *            The graph to copy.
     */
    public DynamicGraph(AbstractGraph graph) {
        super(graph);
    }

    /**
     * Copy constructor
     * @param graph
     */
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

    /**
     * Adds a new dynamic interaction to the list of dynamic interactions.
     * @param edge  the edge which is added or removed from the graph
     * @param date the timestamp
     * @param action the action of the interaction
     */
    public void addDynamicInteraction(Edge edge, String date, String action) {
        CustomNode source = this.getCustomNode(edge.getSourceNode());
        CustomNode target = this.getCustomNode(edge.getTargetNode());
        DynamicInteraction dynamicInteraction = new DynamicInteraction(source, target, date, action);
        this.dynamicInteractions.add(dynamicInteraction);
    }

    /**
     * Handles uploading a DynamicGraph to the database.
     * @param db
     * @param transId
     * @throws InterruptedException
     */
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

    /**
     * Handles loading a graph from the database.
     * @param key
     * @param db
     * @param transId
     * @return
     */
    public static DynamicGraph load(String key, ArangoDatabase db, String transId) {

        // Load underlying CustomGraph
        DynamicGraph graph = new DynamicGraph(CustomGraph.load(key, db, transId));

        ArangoCollection collection = db.collection(collectionName);
        DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
        AqlQueryOptions queryOpt = new AqlQueryOptions().streamTransactionId(transId);
        BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);

        if(bd != null){
            ObjectMapper om = new ObjectMapper();

            // Get Map from CustomNode db keys to CustomNode objects, to set Interaction source/target
            Map<String, CustomNode> customNodeKeyMap = new HashMap<String, CustomNode>();
            String query = "FOR node IN " + CustomNode.collectionName + " FILTER node."
                    + CustomNode.graphKeyColumnName +" == \"" + key +"\" RETURN node";
            ArangoCursor<BaseDocument> nodeDocuments = db.query(query, queryOpt, BaseDocument.class);

            while(nodeDocuments.hasNext()) {
                BaseDocument nodeDocument = nodeDocuments.next();
                CustomNode node = CustomNode.load(nodeDocument, graph);
                customNodeKeyMap.put(CustomNode.collectionName +"/"+node.getKey(), node);
            }

            // for each key in the graphs interaction key list load DynamicInteraction and add to List
            Object objDynInKeys = bd.getAttribute(dynInKeysColumnName);
            List<String> dynInKeys = om.convertValue(objDynInKeys, List.class);

            for(String dynInKey : dynInKeys) {
                DynamicInteraction dynamicInteraction = DynamicInteraction.load(dynInKey, graph, customNodeKeyMap, db, readOpt);
                graph.dynamicInteractions.add(dynamicInteraction);
            }
        }else {
            System.out.println("Empty Graph document");
        }
        return graph;
    }
}
