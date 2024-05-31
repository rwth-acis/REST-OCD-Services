package i5.las2peer.services.ocd.utils;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.graphs.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class represents the Community Life Cycle as described in "Quantifying social group evolution" by Palla et al.
 * [doi:10.1038/nature05670]
 * It handles the 6 types of events presented GROWTH, BIRTH, CONTRACTION, DEATH, FUSION and SPLIT.
 * The handle methods are to be integrated into dynamic algorithms.
 */
public class CommunityLifeCycle {
    ////////////////////////////// DATABASE COLUMN NAMES //////////////////////////////////
    public static final String nameColumnName = "NAME";

    public static final String collectionName = "clc";

    public static final String graphKeyColumnName = "GRAPH_KEY";

    public static final String creationMethodKeyColumnName = "CREATION_METHOD_KEY";

    public static final String coverKeyColumnName = "COVER_KEY";

    public static final String eventKeysColumnName = "EVENT_KEYS";

    /////////////////////////// ATTRIBUTES ////////////////////////////
    /**
     * System generated persistence key
     */
    private String key = "";
    /**
     * The graph the community life cycle is based on
     */
    private DynamicGraph graph = new DynamicGraph();
    /**
     * The cover the community life cycle is based on
     */
    private Cover cover = new Cover(this.graph);
    /**
     * Logged data about the algorithm that created the clc
     */
    private CoverCreationLog creationMethod = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(), new HashSet<GraphType>());
    /**
     * The name of the clc
     */
    private String name = "";
    /**
     * The list of events
     */
    private List<CommunityEvent> events = new ArrayList<>();

    public CommunityLifeCycle() {
    }

    public CommunityLifeCycle(Cover c, DynamicGraph g){
        this.graph = g;
        this.cover = c;
    }

    public String getKey() {
        return key;
    }

    public List<CommunityEvent> getEvents() {
        return events;
    }

    public void setCreationMethod(CoverCreationLog creationMethod) {
        if(creationMethod != null) {
            this.creationMethod = creationMethod;
        } else{
            this.creationMethod = new CoverCreationLog(CoverCreationType.UNDEFINED, new HashMap<String, String>(),
                    new HashSet<GraphType>());
        }

    }

    public DynamicGraph getGraph() {
        return graph;
    }

    public Cover getCover() {
        return cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGraph(DynamicGraph graph) {
        this.graph = graph;
    }

    public void setCover(Cover cover) {
        this.cover = cover;
    }

    /**
     * Handles an incoming growth event from the dynamic algorithm
     * @param current_timestamp the timestamp the event is taking place
     * @param community_id the involved community
     * @param node_name the node added to the community
     */
    public void handleGrowth(String current_timestamp, int community_id, String node_name){
        // If the community has already had a growth event at this timestep, only add the node to the event
        boolean added = false;
        for(CommunityEvent event: events){
            if(event.getEventType() == CommunityEventType.GROWTH.getId() && event.getDate() == current_timestamp && event.getCommunitiesInvolved().get(0) == community_id){
                event.getNodesInvolved().add(node_name);
                added = true;
            }
        }
        // if new growth event add new event
        if(!added) {
            List<Integer> communitiesInvolved = new ArrayList<>();
            List<String> nodesInvolved = new ArrayList<>();

            communitiesInvolved.add(community_id);
            nodesInvolved.add(node_name);

            CommunityEvent new_event = new CommunityEvent(this, current_timestamp, communitiesInvolved, nodesInvolved);
            new_event.addEventType(CommunityEventType.GROWTH);


            events.add(new_event);
        }
    }

    /**
     * Handles an incoming birth event from the algorithm
     * @param current_timestamp the timestamp the event is taking place
     * @param community_id the involved community
     * @param node_names the nodes in the birthed community
     */
    public void handleBirth(String current_timestamp, int community_id, List<String> node_names) {
        List<Integer> communitiesInvolved = new ArrayList<>();

        communitiesInvolved.add(community_id);

        CommunityEvent new_event = new CommunityEvent(this, current_timestamp, communitiesInvolved, node_names);
        new_event.addEventType(CommunityEventType.BIRTH);
        events.add(new_event);
    }

    /**
     * Handles an incoming contraction event from the algorithm
     * @param current_timestamp the timestamp the event is taking place
     * @param community_id the involved community
     * @param node_name the node removed from the community
     */
    public void handleContraction(String current_timestamp, int community_id, String node_name) {
        // If the community has already had a contraction event at this timestep, only add the node to the event
        boolean added = false;
        for(CommunityEvent event: events) {
            if(event.getEventType() == CommunityEventType.CONTRACTION.getId() && event.getDate() == current_timestamp && event.getCommunitiesInvolved().get(0) == community_id){
                event.getNodesInvolved().add(node_name);
                added = true;
            }
        }
        // if new contraction event add new event
        if(!added) {
            List<Integer> communitiesInvolved = new ArrayList<>();
            List<String> nodesInvolved = new ArrayList<>();

            communitiesInvolved.add(community_id);
            nodesInvolved.add(node_name);

            CommunityEvent new_event = new CommunityEvent(this, current_timestamp, communitiesInvolved, nodesInvolved);
            new_event.addEventType(CommunityEventType.CONTRACTION);
            events.add(new_event);
        }
    }

    /**
     * Handles an incoming death event from the dynamic algorithm
     * @param current_timestamp the timestamp the event is taking place
     * @param community_id the involved community
     */
    public void handleDeath(String current_timestamp, int community_id) {
        // If the community was born in this timestamp delete the birth event
        boolean born = false;
        List<CommunityEvent> eventCopy = new ArrayList<>(this.events);
        for(CommunityEvent event: eventCopy){
            if(event.getDate() == current_timestamp && event.getCommunitiesInvolved().get(0) == community_id && event.getEventType() == CommunityEventType.BIRTH.getId()){
                born = true;
                events.remove(event);
            }
        }
        //if not add new death event to events
        if(!born) {
            List<Integer> communitiesInvolved = new ArrayList<>();

            communitiesInvolved.add(community_id);

            CommunityEvent new_event = new CommunityEvent(this, current_timestamp, communitiesInvolved);
            new_event.addEventType(CommunityEventType.DEATH);
            events.add(new_event);
        }
    }

    /**
     * Handles an incoming fusion event from the dynamic algorithm
     * @param current_timestamp the timestamp the event is taking place
     * @param communitiesInvolved the communities involved sorted by age
     */
    public void handleFusion(String current_timestamp, List<Integer> communitiesInvolved){
        CommunityEvent new_event = new CommunityEvent(this, current_timestamp, communitiesInvolved);
        new_event.addEventType(CommunityEventType.FUSION);
        events.add(new_event);
    }

    /**
     * Handles an incoming split event from the dynamic algorithm
     * @param current_timestamp the timestamp the event is taking place
     * @param communitiesInvolved the communities involved sorted by age
     */
    public void handleSplit(String current_timestamp, List<Integer> communitiesInvolved) {
        CommunityEvent new_event = new CommunityEvent(this, current_timestamp, communitiesInvolved);
        new_event.addEventType((CommunityEventType.SPLIT));
        events.add(new_event);
    }

    ///////////////////////// DATABASE FUNCTIONS /////////////////////////////
    public void persist(ArangoDatabase db, String transId){
        ArangoCollection collection = db.collection(collectionName);
        BaseDocument bd = new BaseDocument();
        DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
        DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
        if(this.graph == null){
            throw new IllegalArgumentException("graph attribute of the clc to be persisted does not exist");
        }else if(this.graph.getKey().equals("")){
            throw new IllegalArgumentException("the graph of the clc is not persisted yet");
        } else if (this.cover.getKey().equals("")) {
            throw new IllegalArgumentException("the cover of the clc is not persisted yet");
        }
        bd.addAttribute(graphKeyColumnName, this.graph.getKey());
        bd.addAttribute(coverKeyColumnName, this.cover.getKey());
        bd.addAttribute(nameColumnName, this.name);
        bd.addAttribute(creationMethodKeyColumnName, this.cover.getCreationMethod().getKey());
        collection.insertDocument(bd, createOptions);
        this.key = bd.getKey();

        bd = new BaseDocument();
        List<String> eventKeyList = new ArrayList<>();
        for(CommunityEvent event: this.events){
            event.persist(db, createOptions);
            eventKeyList.add(event.getKey());
        }
        bd.addAttribute(eventKeysColumnName, eventKeyList);
        collection.updateDocument(this.key, bd, updateOptions);
    }

    public static CommunityLifeCycle load(String key, Cover c, DynamicGraph g, ArangoDatabase db, String transId){
        CommunityLifeCycle clc = null;
        ArangoCollection collection = db.collection(collectionName);
        DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
        BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);

        if(bd != null) {
            clc = new CommunityLifeCycle(c, g);
            ObjectMapper om = new ObjectMapper();

            String graphKey = bd.getAttribute(graphKeyColumnName).toString();
            String coverKey = bd.getAttribute(coverKeyColumnName).toString();
            if(!graphKey.equals(g.getKey())){
                System.out.println("graph with key: " + g.getKey() + " does not fit to cover with GraphKey: " + graphKey);
                return null;
            }
            if(!coverKey.equals(c.getKey())){
                System.out.println("cover with key: " + c.getKey() + " does not fit to cover with coverKey: " + coverKey);
                return null;
            }
            String creationMethodKey = bd.getAttribute(creationMethodKeyColumnName).toString();
            Object objEventKeys = bd.getAttribute(eventKeysColumnName);
            List<String> eventKeys = om.convertValue(objEventKeys, List.class);

            clc.key = key;
            clc.name = bd.getAttribute(nameColumnName).toString();
            clc.creationMethod = CoverCreationLog.load(creationMethodKey, db, readOpt);
            for(String eventKey: eventKeys){
                CommunityEvent event = CommunityEvent.load(eventKey, clc, db, readOpt);
                clc.events.add(event);
            }

        }else{
            System.out.println("empty clc document");
        }
        return clc;
    }

    @Override
    public String toString() {
        return "CommunityLifeCycle{" +
                "key='" + key + '\'' +
                ", graph=" + graph +
                ", cover=" + cover +
                ", creationMethod=" + creationMethod +
                ", name='" + name + '\'' +
                ", events=" + events +
                '}';
    }
}
