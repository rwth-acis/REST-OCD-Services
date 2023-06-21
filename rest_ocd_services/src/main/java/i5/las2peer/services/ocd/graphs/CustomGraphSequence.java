package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.graphstream.graph.Node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a graph sequence (or dynamic graph), i.e. a (time-)ordered list of graphs
 *
 * @author Max Kißgen
 *
 */
//TODO: Check how/if graphs can be part of multiple sequences
//TODO: Check if date attributes are even needed here anymore
public class CustomGraphSequence {

    public static final String collectionName = "customgraphsequence";
    public static final String customGraphKeysColumnName = "CUSTOM_GRAPH_KEYS";
    public static final String nameColumnName = "NAME";
    public static final String userColumnName = "USER_NAME";
    public static final String sequenceCommunityColorMapColumnName = "SEQ_COMM_COLOR_MAP";
    public static final String communitySequenceCommunityMapColumnName = "COMM_SEQ_COMM_MAP";
    public static final String timeOrderedColumnName = "TIME_ORDERED";
    public static final String extraInfoColumnName = "EXTRA_INFO";

    public static final String startDateColumnName = "START_DATE";
    public static final String endDateColumnName = "END_DATE";


    /**
     * System generated persistence key.
     */
    private String key;

    /**
     * System generated persistence key.
     */
    private String name = "generated sequence";

    /**
     * The name of the user owning the graph.
     */
    private String userName = "";

    private List<String> customGraphKeys = new ArrayList<>();

    //TODO: Update SequenceCommunities when Cover/Graph are deleted.
    private HashMap<String,Integer> sequenceCommunityColorMap = new HashMap<String, Integer>();
    private HashMap<String,String> communitySequenceCommunityMap = new HashMap<String, String>();

    private boolean timeOrdered = false; //Variable to signify whether this sequence is time ordered  or not(i.e. when it was generated by a User that didnt take dates into account
    private JSONObject extraInfo = new JSONObject();
    private Date startDate = new Date(Long.MIN_VALUE);
    private Date endDate = new Date(Long.MAX_VALUE);

    private CustomGraphSequence() {};

    public CustomGraphSequence(CustomGraph firstGraph, boolean checkTimeOrdered) throws ParseException {
        customGraphKeys.add(firstGraph.getKey());
        this.userName = firstGraph.getUserName();
        if(checkTimeOrdered && firstGraph instanceof CustomGraphTimed timedGraph) {
            timeOrdered = true;
            this.startDate = new Date(timedGraph.getStartDate().getTime());
            this.endDate = new Date(timedGraph.getEndDate().getTime());
        }
    }

    //TODO: Remove Sequence Community if empty?
    public void deleteGraphFromSequence(Database db, String graphKey, List<Cover> coverList) throws OcdPersistenceLoadException {
        if(this.timeOrdered) {
            if (customGraphKeys.indexOf(graphKey) == 0 && 1 < customGraphKeys.size()) {
                CustomGraphTimed nextGraph = (CustomGraphTimed) db.getGraph(this.userName, customGraphKeys.get(1));
                this.startDate = new Date(nextGraph.getStartDate().getTime());
            } else if (customGraphKeys.indexOf(graphKey) == customGraphKeys.size() - 1 && 1 < customGraphKeys.size()) {
                CustomGraphTimed prevGraph = (CustomGraphTimed) db.getGraph(this.userName, customGraphKeys.get(customGraphKeys.size() - 2));
                this.endDate = new Date(prevGraph.getEndDate().getTime());
            }
        }
        customGraphKeys.remove(graphKey);
        if (!communitySequenceCommunityMap.isEmpty()) {
            for (Cover cover : coverList) {
                deleteCoverFromSequence(cover);
            }
        }
    }

    //TODO: Remove Sequence Community if graph added?
    public void addGraphToSequence(int index, String graphKey) {
        customGraphKeys.add(index, graphKey);
        //Delete visualization since the new graph isnt mapped yet.
        communitySequenceCommunityMap = new HashMap<>();
        sequenceCommunityColorMap = new HashMap<>();
    }

    public void deleteCoverFromSequence(Cover cover) {
        if (!communitySequenceCommunityMap.isEmpty()) {
            for (Community comm : cover.getCommunities()) {
                String sequenceCommKey = communitySequenceCommunityMap.get(comm.getKey());
                communitySequenceCommunityMap.remove(comm.getKey());
                if(!communitySequenceCommunityMap.containsValue(sequenceCommKey)) { //TODO: Check if this can be done more nicely without adding too much more to the database
                    sequenceCommunityColorMap.remove(sequenceCommKey);
                }
            }
        }
    }

    //TODO: Make this a binary Search. Should for the moment not impact performance too much though, fetched docs are small and likely not many
    public boolean tryAddTimedGraphToSequence(ArangoDatabase db, CustomGraphTimed graph) throws OcdPersistenceLoadException {//, String newGraphKey, Date newGraphStartDate, Date newGraphEndDate) throws OcdPersistenceLoadException {
        if (!this.timeOrdered) { //Can't add a graph to a sequence that (potentially) doesn't operate by our timeframe rules
            return false;
        }

        ArangoCollection graphCollection = db.collection(CustomGraph.collectionName);
        ObjectMapper om = new ObjectMapper();
        // We are not checking whether the needed extraInfo attributes exist here since if they didn't the sequence would not be time ordered
        for (int i=0; i<customGraphKeys.size(); i++) {
            String seqGraphKey = customGraphKeys.get(i);
            BaseDocument seqGraphDoc = graphCollection.getDocument(seqGraphKey, BaseDocument.class);
            Date seqGraphStartDate = new Date(Long.MIN_VALUE);
            Date seqGraphEndDate = new Date(Long.MAX_VALUE);
            try {
                //JSONObject seqGraphextraInfo = new JSONObject(om.convertValue(seqGraphDoc.getAttribute(extraInfoColumnName), Map.class));
                seqGraphStartDate = DateUtils.parseDate(seqGraphDoc.getAttribute(CustomGraphTimed.startDateColumnName).toString(),
                        "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
                seqGraphEndDate = DateUtils.parseDate(seqGraphDoc.getAttribute(CustomGraphTimed.endDateColumnName).toString(),
                        "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
            }
            catch (ParseException dateEx) {
                throw new OcdPersistenceLoadException("Could not retrieve/parse dates for graph in time-ordered sequence");
            }

            if (graph.getEndDate().getTime() <= seqGraphStartDate.getTime()) {
                if (i == 0) {
                    addGraphToSequence(0,graph.getKey());
                    this.startDate = new Date(graph.getStartDate().getTime());
                    return true;
                }
                else {
                    ObjectNode nextGraphInListDoc = graphCollection.getDocument(customGraphKeys.get(i-1), ObjectNode.class);
                    Date seqPrevGraphEndDate;
                    try {
                        seqPrevGraphEndDate = DateUtils.parseDate(om.convertValue(nextGraphInListDoc.get(CustomGraphTimed.endDateColumnName), String.class), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
                    }
                    catch (ParseException e) {
                        throw new OcdPersistenceLoadException("Could not retrieve/parse end date for non-first graph in sequence");
                    }
                    if (graph.getStartDate().getTime() >= seqPrevGraphEndDate.getTime()) { // Check if we actually fit in between
                        addGraphToSequence(i,graph.getKey());
                        return true;
                    }
                    break; // graph doesn't fit in sequence so we can abort
                }
            }
            else if (i == customGraphKeys.size()-1 && seqGraphEndDate != null && graph.getStartDate().getTime() >= seqGraphEndDate.getTime()) {
                addGraphToSequence(customGraphKeys.size(), graph.getKey());
                this.endDate = new Date(graph.getEndDate().getTime());
                return true;
            }
        }

        return false;
    }

    //TODO: Check if going over node indices will always work db wise
    private HashMap<String,ImmutablePair<Integer,Integer>> makeCombinedNodesMap(Cover aCover, Cover bCover) {
        CustomGraph aGraph = aCover.getGraph();
        CustomGraph bGraph = bCover.getGraph();

        HashMap<String,ImmutablePair<Integer,Integer>> nodesABcombined = new HashMap<>();

        for(Node nodeA : aGraph.nodes().toList()) {
            CustomNode cNodeA = aGraph.getCustomNode(nodeA);
            CustomNode cNodeB = bGraph.getCustomNode(cNodeA.getName());

            nodesABcombined.put(cNodeA.getName(), new ImmutablePair<>(nodeA.getIndex(), cNodeB != null ? bGraph.getNode(cNodeB).getIndex() : -1));
        }

        //Add remaining nodes from graph b
        for(Node nodeB : bGraph.nodes().toList()) {
            CustomNode cNodeB = bGraph.getCustomNode(nodeB);
            CustomNode cNodeA = aGraph.getCustomNode(cNodeB.getName());
            if(!nodesABcombined.containsKey(cNodeB.getName())) {
                nodesABcombined.put(cNodeB.getName(), new ImmutablePair<>(cNodeA != null ? bGraph.getNode(cNodeA).getIndex() : -1, nodeB.getIndex()));
            }
        }

        return nodesABcombined;
    }

    //TODO: Check performance of this
    //Do a pearson correlation of node belongings for both communities with a pretend "complete" graph of all of both graphs nodes (no duplicates for shared nodes)
    private double getCommunitySimilarity(Community commA, Community commB, HashMap<String,ImmutablePair<Integer,Integer>> nodesABcombined) {
        Map<Integer,Double> commAMemberships = commA.getMembershipsByIndices();
        Map<Integer,Double> commBMemberships = commB.getMembershipsByIndices();

        double commAAvg = commAMemberships.values().stream().reduce(0.0, Double::sum);
        double commBAvg = commBMemberships.values().stream().reduce(0.0, Double::sum);

//        for(String nodeName : nodesABcombined.keySet()) {
//            commAAvg += nodesABcombined.get(nodeName).getLeft() >= 0 ? commAMemberships.getOrDefault(nodesABcombined.get(nodeName).getLeft(),0.0) : 0.0;
//            commBAvg += nodesABcombined.get(nodeName).getRight() >= 0 ? commBMemberships.getOrDefault(nodesABcombined.get(nodeName).getRight(),0.0) : 0.0;
//        }
        commAAvg /= nodesABcombined.size();
        commBAvg /= nodesABcombined.size();

        double SP_a_b = 0.0, SQ_a_b = 0.0, SQ_b_a = 0.0;

        //TODO: Check whether we really need to iterate through all of this or can just sum up over comA/comB
        for(int index : commAMemberships.keySet()) {
            double nodeAValue = commAMemberships.get(index);
            String nodeName = commA.getCover().getGraph().getCustomNode(index).getName();
            double nodeBValue = 0.0;
            if (nodesABcombined.get(nodeName).getRight() >= 0) {
                if (commBMemberships.containsKey(nodesABcombined.get(nodeName).getRight())) {
                    nodeBValue = commBMemberships.get(nodesABcombined.get(nodeName).getRight());
                    commBMemberships.remove(nodesABcombined.get(nodeName).getRight()); // remove key as entry as been processed
                }
            }

            SP_a_b += (nodeAValue - commAAvg) * (nodeBValue - commBAvg);
            SQ_a_b += Math.pow(nodeAValue - commAAvg,2);
            SQ_b_a += Math.pow(nodeBValue - commBAvg,2);
        }
        for(int index : commBMemberships.keySet()) { // Cycle through remaining nodes of b community
            double nodeBValue = commBMemberships.get(index);
            String nodeName = commB.getCover().getGraph().getCustomNode(index).getName();
            double nodeAValue = nodesABcombined.get(nodeName).getLeft() >= 0 ? commAMemberships.getOrDefault(nodesABcombined.get(nodeName).getLeft(),0.0) : 0.0;

            SP_a_b += (nodeAValue - commAAvg) * (nodeBValue - commBAvg);
            SQ_a_b += Math.pow(nodeAValue - commAAvg,2);
            SQ_b_a += Math.pow(nodeBValue - commBAvg,2);
        }
        //Rest of the nodes both have zero values and therefore the same SP/SQ results, add them in bulk
        int remainingSize = nodesABcombined.size() - commAMemberships.size() + commBMemberships.size();
        SP_a_b += remainingSize*(0.0 - commAAvg) * (0.0 - commBAvg);
        SQ_a_b += remainingSize*Math.pow(0.0 - commAAvg,2);
        SQ_b_a += remainingSize*Math.pow(0.0 - commBAvg,2);

        return SP_a_b / Math.sqrt(SQ_a_b * SQ_b_a);
    }

    private <T> int getInsertionIndex(double similarityValue, ArrayList<ImmutableTriple<Double,T,T>> communitySimilarities) {
        int index = 0;
        if(communitySimilarities.size() == 0) {//TODO: Delete
            return index;
        }
        int lowerBound = 0, upperBound = communitySimilarities.size()-1;
        while (lowerBound <= upperBound) {
            int middle = lowerBound  + ((upperBound - lowerBound) / 2);
            if(similarityValue > communitySimilarities.get(middle).getLeft()) {
                upperBound = middle-1;
            }
            else if (similarityValue < communitySimilarities.get(middle).getLeft()) {
                lowerBound = middle+1;
            }

            if (similarityValue >= communitySimilarities.get(middle).getLeft()
                    && (middle-1 < 0 || similarityValue <= communitySimilarities.get(middle-1).getLeft())) {
                index = middle;
                return index;
            }
            else if (similarityValue < communitySimilarities.get(middle).getLeft()
                    && (middle+1 == communitySimilarities.size() || similarityValue >= communitySimilarities.get(middle+1).getLeft())) {
                index = middle+1;
                return index;
            }
        }
        return -1; //Should not happen
    }

    //What to do when a graph has no covers. Abort sequence comm generation? -> Still do but without and cope with that in SequenceVisualOutput
    //TODO: Optionally have a boolean to only generate new from point a new graph was added onward. But then the graphs index in the sequence needs to be known and maintained. Also after assignment unused sequenceCommunities need to be deleted from the map
    //TODO: Somehow do this without using a db and username attribute. Currently needed to just query on demand and not get passed a giant list of covers which in turn give all related graphs
    /**
     * Fills the sequence community maps
     * @param username the username for the graphs/communities
     * @param db the database used to fetch the graphs/communities
     * @param similarityThreshold a threshold of similarity between communities. If the similarity is below that, a new sequence community will be created
     * @throws OcdPersistenceLoadException if (one of) the graphs/communities can't be loaded
     */
    public void generateSequenceCommunities(Database db, String username, double similarityThreshold) throws OcdPersistenceLoadException {
        List<CoverMeta> coverMetas = new ArrayList<>();
        List<Integer> executionStatusIds = List.of(ExecutionStatus.COMPLETED.getId());
        List<Integer> metricExecutionStatusIds = List.of(ExecutionStatus.COMPLETED.getId(),ExecutionStatus.ERROR.getId(),ExecutionStatus.RUNNING.getId(),ExecutionStatus.WAITING.getId());

        for(String graphKey : customGraphKeys) {
            coverMetas.addAll(db.getCoverMetaDataEfficiently(username,graphKey,executionStatusIds,metricExecutionStatusIds,0,Integer.MAX_VALUE));
        }
        if (coverMetas.size() == 0) {
            throw new RuntimeException("Cant visualize sequence cause no covers exist");
        }

        sequenceCommunityColorMap = new HashMap<String,Integer>();
        communitySequenceCommunityMap = new HashMap<String,String>();
        int sequenceCoverCounter = 0;
        for (int i=0; i<coverMetas.size(); i++) {
            if(i == 0) { //Simply add the first communities of the first cover as sequence communities
                Cover firstCover = db.getCover(username, coverMetas.get(i).getGraphKey(), coverMetas.get(i).getKey());
                for(Community comm : firstCover.getCommunities()) {
                    sequenceCommunityColorMap.put(Integer.toString(sequenceCoverCounter),null); //TODO: Decide about color attribute, do I even need this?
                    communitySequenceCommunityMap.put(comm.getKey(),Integer.toString(sequenceCoverCounter));
                    sequenceCoverCounter++;
                }
            }
            else {
                ArrayList<ImmutableTriple<Double,String,String>> communitySimilarities = new ArrayList<>(); //Maps current communities to lists of the similarity values of the previous ones (ordered by similarity value)
                Cover prevCover = db.getCover(username, coverMetas.get(i-1).getGraphKey(), coverMetas.get(i-1).getKey());
                Cover currCover = db.getCover(username, coverMetas.get(i).getGraphKey(), coverMetas.get(i).getKey());
                HashMap<String,ImmutablePair<Integer,Integer>> nodesABcombined = makeCombinedNodesMap(prevCover, currCover);
                HashSet<Community> matchedCommunities = new HashSet<>();

                int u = 0;
                int numComms = currCover.getCommunities().size();
                //Compute Similarities for Communities of the two covers and insert them into an ordered list
                for(Community currComm : currCover.getCommunities()) {
                    for(Community prevComm : prevCover.getCommunities()) {
                        if(matchedCommunities.contains(prevComm)) {
                            continue;
                        }
                        //Filter out loose node communities for faster processing
                        if(currComm.getSize() == 1) {
                            if (prevComm.getSize() == 1) {
                                Node nodeA = currComm.getMemberships().keySet().iterator().next();
                                Node nodeB = prevComm.getMemberships().keySet().iterator().next();
                                if (currCover.getGraph().getCustomNode(nodeA).getName().equals(prevCover.getGraph().getCustomNode(nodeB).getName())
                                        && Math.abs(currComm.getBelongingFactor(nodeA.getIndex()) - currComm.getBelongingFactor(nodeB.getIndex())) <= 0.1) {
                                    communitySimilarities.add(0, new ImmutableTriple<>(100.0, currComm.getKey(), prevComm.getKey()));
                                    matchedCommunities.add(currComm);
                                    matchedCommunities.add(prevComm);
                                    break;
                                }
                            }
                            else if (prevComm.getBelongingFactor(currComm.getMembershipsByIndices().keySet().iterator().next()) <= 0.1) {
                                continue;
                            }
                        }
                        else if (prevComm.getSize() == 1 && currComm.getBelongingFactor(prevComm.getMembershipsByIndices().keySet().iterator().next()) <= 0.1) {
                            continue;
                        }

                        double commSimilarity = getCommunitySimilarity(prevComm,currComm,nodesABcombined);
                        int insertionIndex = getInsertionIndex(commSimilarity, communitySimilarities);
                        communitySimilarities.add(insertionIndex, new ImmutableTriple<>(commSimilarity, currComm.getKey(), prevComm.getKey()));
//                        if(commSimilarity >= 0.7) { //Skip the rest of the comparisons when we found a near perfect match
//                            matchedCommunities.add(currComm);
//                            matchedCommunities.add(prevComm);
//                            break;
//                        }
                    }
                }

                HashSet<String> usedCommunities = new HashSet<>();
                //Add most similar pairs to the sequenceCommunitMaps or create a new sequence community if we cant find a good available matching above the similarity threshold.
                for (ImmutableTriple<Double,String,String> similarityTriple : communitySimilarities) {
                    if(!usedCommunities.contains(similarityTriple.getRight())) { //I.e. if we didnt already map a community of the current cover to one of the previous
                        if(!usedCommunities.contains(similarityTriple.getMiddle())) { //I.e. if the community of the current cover wasn't already mapped
                            if(similarityTriple.getLeft() <= similarityThreshold) {
                                sequenceCommunityColorMap.put(Integer.toString(sequenceCoverCounter),null);
                                communitySequenceCommunityMap.put(similarityTriple.getMiddle(), Integer.toString(sequenceCoverCounter));
                                sequenceCoverCounter++;
                                usedCommunities.add(similarityTriple.getMiddle());
                            }
                            else {
                                communitySequenceCommunityMap.put(similarityTriple.getMiddle(), communitySequenceCommunityMap.get(similarityTriple.getRight()));
                                usedCommunities.add(similarityTriple.getMiddle());
                                usedCommunities.add(similarityTriple.getRight());
                            }
                        }
                        else {
                            similarityTriple = null; //TODO: Think of a more elegant way to do this
                        }
                    }
                    else {
                        similarityTriple = null; //TODO: Think of a more elegant way to do this
                    }
                }
                //Now create new sequenceCommunities for those that didn't get a mapping
                for(Community currComm : currCover.getCommunities()) {
                    if(!usedCommunities.contains(currComm.getKey())) {
                        sequenceCommunityColorMap.put(Integer.toString(sequenceCoverCounter),null);
                        communitySequenceCommunityMap.put(currComm.getKey(), Integer.toString(sequenceCoverCounter));
                        sequenceCoverCounter++;
                        //usedCommunities.add(currComm.getKey());
                    }
                }
            }
        }
    }

    public void persist(ArangoDatabase db, String transId) {
        ArangoCollection collection = db.collection(collectionName);
        BaseDocument bd = new BaseDocument();
        //options for the transaction
        DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
        DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
        updateOptions.mergeObjects(false);

        bd.addAttribute(nameColumnName, this.name);
        bd.addAttribute(userColumnName, this.userName);
        bd.addAttribute(timeOrderedColumnName, this.timeOrdered);
        bd.addAttribute(customGraphKeysColumnName, this.customGraphKeys);

        bd.addAttribute(sequenceCommunityColorMapColumnName, this.sequenceCommunityColorMap);
        bd.addAttribute(communitySequenceCommunityMapColumnName, this.communitySequenceCommunityMap);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        bd.addAttribute(startDateColumnName, this.startDate != null ? dateFormat.format(this.startDate) : null);
        bd.addAttribute(endDateColumnName, this.endDate != null ? dateFormat.format(this.endDate) : null);
        bd.addAttribute(extraInfoColumnName,this.extraInfo);

        if(this.key == null) {
            this.key = collection.insertDocument(bd, createOptions).getKey();
        }
        else {
            bd.setKey(this.key);
            collection.updateDocument(this.key, bd, updateOptions);
        }
    }

    public static CustomGraphSequence load(String key, ArangoDatabase db, String transId) throws OcdPersistenceLoadException {
        CustomGraphSequence sq = new CustomGraphSequence();
        ArangoCollection collection = db.collection(collectionName);

        DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
        BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);
        if (bd != null) {
            ObjectMapper om = new ObjectMapper();

            sq.key = key;
            sq.name = bd.getAttribute(nameColumnName).toString();
            sq.userName = bd.getAttribute(userColumnName).toString();
            sq.timeOrdered = om.convertValue(bd.getAttribute(timeOrderedColumnName), Boolean.class);
            sq.customGraphKeys = om.convertValue(bd.getAttribute(customGraphKeysColumnName), ArrayList.class);
            try {
                sq.sequenceCommunityColorMap = om.convertValue(bd.getAttribute(sequenceCommunityColorMapColumnName), HashMap.class);
                sq.communitySequenceCommunityMap = om.convertValue(bd.getAttribute(communitySequenceCommunityMapColumnName), HashMap.class);
            }
            catch (Exception e) {
                throw new OcdPersistenceLoadException("Could not load sequence community maps: " + e.getMessage());
            }
            try {
                sq.startDate = DateUtils.parseDate(bd.getAttribute(startDateColumnName).toString(), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
            }
            catch (ParseException e) {
                sq.startDate = new Date(Long.MIN_VALUE);
            }
            try {
                sq.endDate = DateUtils.parseDate(bd.getAttribute(endDateColumnName).toString(), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
            }
            catch (ParseException e) {
                sq.endDate = new Date(Long.MAX_VALUE);
            }
            if(bd.getAttribute(extraInfoColumnName) != null){
                try {
                    sq.extraInfo = new JSONObject(om.convertValue(bd.getAttribute(extraInfoColumnName), Map.class));
                }
                catch (Exception e) {
                    throw new OcdPersistenceLoadException("Could not parse extraInfo of GraphSequence. " + e.getMessage());
                }
            }
            else {
                sq.extraInfo = new JSONObject();
            }
        }
        else {
            throw new OcdPersistenceLoadException("Could not find graph sequence");
        }
        return sq;
    }

    public void setStartDate(Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }

    public void setEndDate(Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getCustomGraphKeys() {
        return customGraphKeys;
    }

    public void setCustomGraphKeys(List<String> customGraphKeys) {
        this.customGraphKeys = customGraphKeys;
    }

    public boolean getTimeOrdered() {
        return timeOrdered;
    }

    public void setTimeOrdered(boolean timeOrdered) {
        this.timeOrdered = timeOrdered;
    }

    public JSONObject getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(JSONObject extraInfo) {
        this.extraInfo = extraInfo;
    }

    public HashMap<String, Integer> getSequenceCommunityColorMap() {
        return sequenceCommunityColorMap;
    }

    public void setSequenceCommunityColorMap(HashMap<String, Integer> sequenceCommunityColorMap) {
        this.sequenceCommunityColorMap = sequenceCommunityColorMap;
    }

    public HashMap<String, String> getCommunitySequenceCommunityMap() {
        return communitySequenceCommunityMap;
    }

    public void setCommunitySequenceCommunityMap(HashMap<String, String> communitySequenceCommunityMap) {
        this.communitySequenceCommunityMap = communitySequenceCommunityMap;
    }
}