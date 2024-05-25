package i5.las2peer.services.ocd.utils;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.graphs.Community;

import java.util.ArrayList;
import java.util.List;

/**
 * The class corresponding to community events in the community life cycle.
 */
public class CommunityEvent {
    public static final String clcKeyColumnName = "CLC_KEY";
    public static final String collectionName = "communityEvent";
    public static final String dateColumnName = "date";
    public static final String eventTypeColumnName = "eventType";
    public static final String communitiesColumnName = "communitiesInvolved";
    public static final String nodesColumnName = "nodesInvolved";
    /**
     * System generated persistence key
     */
    private String key;
    /**
     * The date of the event
     */
    private String date;
    /**
     * The event type by id
     */
    private int eventType;
    /**
     * The list of communities involved in the event
     */
    private List<Integer> communitiesInvolved = new ArrayList<>();
    /**
     * The list of nodes involved in the event
     */
    private List<String> nodesInvolved = new ArrayList<>();
    /**
     * The community life cycle the event is part of
     */
    private CommunityLifeCycle clc;
    public CommunityEvent(CommunityLifeCycle clc, String date, List<Integer> communitiesInvolved, List<String> nodesInvolved) {
        this.clc = clc;
        this.date = date;
        this.communitiesInvolved = communitiesInvolved;
        this.nodesInvolved = nodesInvolved;
    }

    public CommunityEvent(CommunityLifeCycle clc, String date, List<Integer> communitiesInvolved) {
        this.clc = clc;
        this.date = date;
        this.communitiesInvolved = communitiesInvolved;
    }

    public CommunityEvent() {
    }

    public int getEventType() {
        return eventType;
    }

    public String getKey() {
        return key;
    }

    public String getDate() {
        return date;
    }
    public int getDateAsInt() {
        return Integer.parseInt(date);
    }

    public List<Integer> getCommunitiesInvolved() {
        return communitiesInvolved;
    }

    public List<String> getNodesInvolved() {
        return nodesInvolved;
    }

    /**
     * Adds an event type to the event as the id of the type.
     * @param type
     */
    public void addEventType(CommunityEventType type){
        this.eventType = type.getId();
    }

    public void persist(ArangoDatabase db, DocumentCreateOptions opt){
        ArangoCollection collection = db.collection(collectionName);
        BaseDocument bd = new BaseDocument();
        bd.addAttribute(dateColumnName, this.date);
        bd.addAttribute(eventTypeColumnName, this.eventType);
        bd.addAttribute(communitiesColumnName, this.communitiesInvolved);
        bd.addAttribute(nodesColumnName, this.nodesInvolved);
        bd.addAttribute(clcKeyColumnName, this.clc.getKey());
        collection.insertDocument(bd, opt);
        this.key = bd.getKey();
    }

    public static CommunityEvent load(String key, CommunityLifeCycle clc, ArangoDatabase db, DocumentReadOptions opt){
        CommunityEvent ce = new CommunityEvent();

        ArangoCollection collection = db.collection(collectionName);

        BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
        if(bd != null){
            ObjectMapper om = new ObjectMapper();
            String eventTypeString = bd.getAttribute(eventTypeColumnName).toString();
            Object objCommunities = bd.getAttribute(communitiesColumnName);
            Object objNodes = bd.getAttribute(nodesColumnName);

            ce.key = key;
            ce.clc = clc;
            ce.date = bd.getAttribute(dateColumnName).toString();
            ce.eventType = Integer.parseInt(eventTypeString);
            ce.communitiesInvolved = om.convertValue(objCommunities, List.class);
            ce.nodesInvolved = om.convertValue(objNodes, List.class);
        }else{
            System.out.println("empty event document");
        }
        return ce;
    }

    @Override
    public String toString() {
        return "CommunityEvent{" +
                "date='" + date + '\'' +
                ", eventType=" + CommunityEventType.lookupType(eventType) +
                ", communitiesInvolved=" + communitiesInvolved +
                ", nodesInvolved=" + nodesInvolved +
                '}';
    }
}
