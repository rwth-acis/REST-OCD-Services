package i5.las2peer.services.ocd.adapters.graphInput;

import com.arangodb.*;
import com.arangodb.mapping.ArangoJack;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphTimed;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ArangoDBGraphInputAdapter extends AbstractGraphInputAdapter {

    /////////////////
    //// Variables///
    /////////////////
    /**
     * Boolean for showing usernames or URIs as node names
     */
    private String host;
    private int port;
    private String databaseUser;
    private String databasePassword;
    private DbName databaseName;

    private ArrayList<String> involvedUserKeys = null;
    /**
     * A common(!) attribute name over all edge collections that signifies a posts date
     */
    private String dateAttributeName = null;
    /**
     * Starting date of posts to be considered
     */
    private Date startDate = null;

    /**
     * Ending date of posts to be considered
     */
    private Date endDate = null;

    private boolean weighEdges = false;

    private String nameAttributeName = "";

    private String nodeCollectionName;
    private ArrayList<String> edgeCollectionNames;
    private ArrayList<ImmutableTriple<String,String,String>> nodeFilters = new ArrayList<>();
    private HashMap<String,ArrayList<ImmutableTriple<String,String,String>>> edgeFilters = new HashMap<>();

    private ArangoDatabase database;

    //TODO: Have an option to directly select all edge collections
    @Override
    public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {
        if (param.containsKey("databaseAddress")) {
			//TODO: REMOVE HTTP PREFIXES
            String[] databaseAddress = param.get("databaseAddress").replaceFirst("http://", "").replaceFirst("https://", "").split(":");
            host = databaseAddress[0];
            if (databaseAddress.length < 2) {
                throw new IllegalArgumentException("Did not get a database port");
            }
            else {
                port = Integer.parseInt(databaseAddress[1]);
            }
            param.remove("databaseAddress");
        }
        else {
            throw new IllegalArgumentException("Did not get a database address");
        }
        try {
            new Socket().connect(new InetSocketAddress(host, port), 60);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("There is nothing running on this address: '" + host + ":" + port +"'");
        }

        if (param.containsKey("databaseCredentials")) {
            String[] databaseCredentials = param.get("databaseCredentials").split(",");
            databaseUser = databaseCredentials[0];
            databasePassword = "";
            if (databaseCredentials.length == 2) {
                //throw new IllegalArgumentException("Did not get a database password");
                databasePassword = databaseCredentials[1];
            }
            param.remove("databaseCredentials");
        }
        else {
            throw new IllegalArgumentException("Did not get database credentials");
        }
        if (param.containsKey("databaseName")) {
            try {
                databaseName = DbName.of(param.get("databaseName"));
                param.remove("databaseName");
            }
            catch(Exception e) {
                throw new IllegalArgumentException("Database name does not seem to correspond with ArangoDBs naming conventions");
            }
        }
        else {
            throw new IllegalArgumentException("Did not get a database name");
        }

        try {
            ArangoDB arangoDBInstance = new ArangoDB.Builder().host(host, port).user(databaseUser).password(databasePassword).serializer(new ArangoJack()).build();
            database = arangoDBInstance.db(databaseName);
        }
        catch(ArangoDBException e) {
            throw new IllegalArgumentException("Could not connect to database '" + databaseName + "' with user '" + databaseUser + "' and the given password");
        }

        if (param.containsKey("nodeCollectionName")) {
            nodeCollectionName = param.get("nodeCollectionName");
            try {
                database.collection(nodeCollectionName).getInfo();
            }
            catch(Exception e) {
                throw new IllegalArgumentException("Can not access node collection on database");
            }
            param.remove("nodeCollectionName");
        }
        else {
            throw new IllegalArgumentException("Did not get a node collection name");
        }
        if (param.containsKey("involvedUsers")) {
            if(!param.get("involvedUsers").equals("")) {
                involvedUserKeys = new ArrayList<String>(Arrays.asList(param.get("involvedUsers").split(",")));
            }
            param.remove("involvedUsers");
        }

        if (param.containsKey("edgeCollectionNames")) {
            edgeCollectionNames = new ArrayList<String>(Arrays.asList(param.get("edgeCollectionNames").split(",")));

            for (String edgeCollectionName : edgeCollectionNames) {
                try {
                    database.collection(edgeCollectionName).getInfo();
                }
                catch(Exception e) {
                    throw new IllegalArgumentException("Can not access edge collection '" + edgeCollectionName + "' on database");
                }
                edgeFilters.put(edgeCollectionName, new ArrayList<>());
            }
            param.remove("edgeCollectionNames");
        }
        else {
            throw new IllegalArgumentException("Did not get any edge collection names");
        }

        if (param.containsKey("dateAttributeName")) {
            if (param.get("dateAttributeName").equals("")) {
                dateAttributeName = null;
            }
            else {
                dateAttributeName = param.get("dateAttributeName");
            }
                param.remove("dateAttributeName");
        }

        if (param.containsKey("startDate")) {
            if (dateAttributeName == null && !param.get("startDate").equals("")) {
                throw new IllegalArgumentException("Start date given but no date attribute name to apply it to");
            }
            else if(!param.get("startDate").equals("")) {
                startDate = DateUtils.parseDate(param.get("startDate"), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
                try {
                    for (String edgeCollectionName : edgeCollectionNames) {
                        edgeFilters.get(edgeCollectionName).add(new ImmutableTriple<>(dateAttributeName, ">=", '"' + param.get("startDate") + '"'));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Can not access edge collection on database while adding start date rule");
                }
            }
            param.remove("startDate");
        }

        if (param.containsKey("endDate")) {
            if (dateAttributeName == null && !param.get("endDate").equals("")) {
                throw new IllegalArgumentException("End date given but no date attribute name to apply it to");
            }
            else if(!param.get("endDate").equals("")) {
                endDate = DateUtils.parseDate(param.get("endDate"), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
                try {
                    for (String edgeCollectionName : edgeCollectionNames) {
                        edgeFilters.get(edgeCollectionName).add(new ImmutableTriple<>(dateAttributeName, "<=", '"' + param.get("endDate") + '"'));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Can not access edge collection on database while adding end date rule");
                }
            }
            param.remove("endDate");
        }

        if (param.containsKey("weighEdges")) {
            if (param.get("weighEdges").equalsIgnoreCase("true")) {
                weighEdges = true;
            }
            else if (param.get("weighEdges").equalsIgnoreCase("false")) {
                weighEdges = false;
            }
            else {
                throw new IllegalArgumentException("weighEdges parameter was not of boolean value");
            }
            param.remove("weighEdges");
        }

		if (param.containsKey("showUserNames")) {
            if (Boolean.parseBoolean(param.get("showUserNames"))) {
                if (param.containsKey("nameAttributeName")) {
                    nameAttributeName = param.get("nameAttributeName");
                    param.remove("nameAttributeName");
                }
                //else {
                //	throw new IllegalArgumentException("Can not use names as node labels since no name attribute was given.");
                //}
            }
            else if (param.containsKey("nameAttributeName")) {
                //throw new IllegalArgumentException("Name attribute was given but showUserNames was not enabled");
                param.remove("nameAttributeName");
            }
			param.remove("showUserNames");
		}

        if(!param.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public void setListParameter(Map<String, List<String>> listParam) throws IllegalArgumentException, ParseException {
        if (listParam.containsKey("nodeFilters")) {
            for (String nodeFilterStr : listParam.get("nodeFilters")) {
                if (!nodeFilterStr.equals("")) {
                    nodeFilters.add(parseDatabaseRule(nodeFilterStr));
                }
            }
            listParam.remove("nodeFilters");
        }

        if (listParam.containsKey("edgeFilters")) {
            for (String edgeFilterStr : listParam.get("edgeFilters")) {
                if (!edgeFilterStr.equals("")) {
                    String[] edgeFilterStrArray = edgeFilterStr.split(":::");
                    if (edgeFilterStrArray.length != 4) {
                        throw new IllegalArgumentException("Edge filter is malformed");
                    } else if (!edgeCollectionNames.contains(edgeFilterStrArray[0])) {
                        throw new IllegalArgumentException("Can't find edge collection for edge filter");
                    }
                    edgeFilters.get(edgeFilterStrArray[0])
                            .add(parseDatabaseRule(edgeFilterStrArray[1] + ":::" + edgeFilterStrArray[2] + ":::" + edgeFilterStrArray[3])); //TODO: Still a bit hacky, change

                }
            }
            listParam.remove("edgeFilters");
        }
    }

    /**
     * Tries to parse an ArangoDB rule for filtering a query. The Result is essentially be a triple of value, an operator and another value
     * @param ruleString The string conveying the database rule, the elements of the rule should be separated by ':::', if the last part of the string is a list, it is comma separated
     * @return An ImmutableTriple of the attribute (String) affected by the rule, the rule parameter (a logic operator such as ==) and a List (String[]) of filters for the rule
     */
    public ImmutableTriple<String,String,String> parseDatabaseRule(String ruleString) {
        String[] ruleStringArray = ruleString.split(":::");
        if (ruleStringArray.length != 3) {
            throw new IllegalArgumentException();
        }

        ArrayList<String> AllowedOperators = new ArrayList<>(List.of("==", "!=", "<", ">", ">=", "<=", "LIKE", "IN", "NOT IN", "REGEX"));
        if (!AllowedOperators.contains(ruleStringArray[1])) {
            throw new IllegalArgumentException();
        }
        if (List.of("IN", "NOT IN").contains(ruleStringArray[1])) { //TODO: Account for text as value that may contain commata
            ruleStringArray[2] = "[\"" + ruleStringArray[2].replace(",","\",\"") + "\"]";
        }
        else {
            ruleStringArray[2] = "\"" + ruleStringArray[2] + "\"";
        }

        //TODO: Maybe check query syntax, too?
        return new ImmutableTriple<>(ruleStringArray[0],ruleStringArray[1],ruleStringArray[2]);
    }

    /**
     * Queries the nodes from the ArangoDB database and creates them in WebOCD, i.e. documents from non-edge collections
     * @param graph The graph the nodes are added to
     * @return A map of of node keys from the database to their newly created graph nodes
     * @throws AdapterException if no nodes could be found or queries/node json documents are malformed
     */
    private HashMap<String,Node> queryNodes(CustomGraph graph) throws AdapterException {
        //Build node query
        StringBuilder queryStringBuilder = new StringBuilder("FOR node IN " + nodeCollectionName + "\n");
        if (involvedUserKeys != null) {
            queryStringBuilder.append("FILTER (node._key IN ")
                    .append(involvedUserKeys.toString()
                            .replace(", ", "\",\"")
                            .replace("[", "[\"").replace("]", "\"]"))
                    .append(") ");
        }
        if (nodeFilters.size() != 0) {
            queryStringBuilder.append("FILTER ");
        }
        for (ImmutableTriple<String,String,String> filter : nodeFilters) {
            queryStringBuilder.append("(node.").append(filter.getLeft()).append(" ").append(filter.getMiddle()).append(" ").append(filter.getRight()).append(") && ");
        }
        if (nodeFilters.size() != 0) {
            queryStringBuilder = new StringBuilder(queryStringBuilder.substring(0, queryStringBuilder.length()-1 - 3)); // Remove last "&& "
            queryStringBuilder.append("\n");
        }
        queryStringBuilder.append("return node");

        //Get nodes
        final ArangoCursor<String> cursor;
        try {
             cursor = database.query(queryStringBuilder.toString(), String.class);
        } catch(ArangoDBException e) {
            throw new AdapterException("Arango Node Query malformed");
        }
        if (cursor == null || !cursor.hasNext()) {
            throw new AdapterException("Arango Query produces empty graph!");
        }

        HashMap<String,Node> keyNodeMap = new HashMap<String,Node>();
        while (cursor.hasNext()) {
            JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            String nodeString = cursor.next();
            JSONObject nodeJson;
            try {
                nodeJson = (JSONObject) jsonParser.parse(nodeString);
            }
            catch(net.minidev.json.parser.ParseException exception) {
                throw new AdapterException("Could not parse node JSON");
            }

            Node node = graph.addNode((String) nodeJson.get("_key"));
            if (!nameAttributeName.equals("") && nodeJson.containsKey(nameAttributeName) && nodeJson.get(nameAttributeName) != null) {
                if (nodeJson.get(nameAttributeName) instanceof JSONArray multipleNames) {
                    if (multipleNames.get(0) instanceof JSONObject firstNameObj) {
                        graph.setNodeName(node, (String) firstNameObj.get("value"));
                    }
                }
                else if (nodeJson.get(nameAttributeName) instanceof JSONObject firstNameObj) {
                    graph.setNodeName(node, (String) firstNameObj.get("value"));
                }
                else {
                    graph.setNodeName(node,(String) nodeJson.get(nameAttributeName));
                }
            }
            else {
                graph.setNodeName(node,(String) nodeJson.get("_key"));
            }
            keyNodeMap.put((String) nodeJson.get("_key"), node);
            for (String key : nodeJson.keySet()) {
                if (!key.equals("_key") && !key.equals("_rev") && !key.equals("_id")) { //If we actually have some of the objects data and not just the key
                    graph.setNodeExtraInfo(node, graph.getNodeExtraInfo(node).appendField(key,nodeJson.get(key)));
                }
            }
        }

        return keyNodeMap;
    }

    //TODO: Check whats most efficient regarding querying edges. At the moment it just queries all edges for which the filters work and then picks the ones between the nodes

    /**
     * Queries the edges from the ArangoDB database and creates them in WebOCD, i.e. documents from edge collections
     * @param graph The graph the edges are added to
     * @param keyNodeMap A map of of node keys from the database to their newly created graph nodes
     * @throws AdapterException if no nodes could be found or queries/node json documents are malformed
     */
    private void queryEdges(CustomGraph graph, HashMap<String,Node> keyNodeMap) throws AdapterException {
        //Build edge query
        for (String edgeCollectionName : edgeCollectionNames) {
            StringBuilder queryStringBuilder = new StringBuilder("FOR edge IN " + edgeCollectionName + "\n");
            if (edgeFilters.get(edgeCollectionName).size() != 0) {
                queryStringBuilder.append("FILTER ");
            }
            for (ImmutableTriple<String,String,String> filter : edgeFilters.get(edgeCollectionName)) {
                queryStringBuilder.append("(edge.").append(filter.getLeft()).append(" ").append(filter.getMiddle()).append(" ").append(filter.getRight()).append(") && ");
            }
            if (edgeFilters.get(edgeCollectionName).size() != 0) {
                queryStringBuilder = new StringBuilder(queryStringBuilder.substring(0, queryStringBuilder.length()-1 - 3)); // Remove last "&& "
                queryStringBuilder.append("\n");
            }
            String arangoEdgeNodes = keyNodeMap.keySet().toString()
                                        .replace(", ","\",\"" + nodeCollectionName + "/")
                                        .replace("[","[\"" + nodeCollectionName + "/").replace("]","\"]");
            queryStringBuilder.append("FILTER (edge._from IN ").append(arangoEdgeNodes).append(" && ");
            queryStringBuilder.append("edge._to IN ").append(arangoEdgeNodes).append(") ");
            queryStringBuilder.append("return edge");

            //Get edges
            final ArangoCursor<String> cursor;
            try {
                cursor = database.query(queryStringBuilder.toString(), String.class);
            } catch(ArangoDBException e) {
                throw new AdapterException("Arango Edge Query malformed");
            }

            while (cursor.hasNext()) {
                JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                String edgeString = cursor.next();
                JSONObject edgeJson;
                try {
                    edgeJson = (JSONObject) jsonParser.parse(edgeString);
                }
                catch(net.minidev.json.parser.ParseException exception) {
                    throw new AdapterException("Could not parse node JSON");
                }

                if (graph.getNode(((String)edgeJson.get("_from")).replace(nodeCollectionName + "/","")) != null && graph.getNode(((String)edgeJson.get("_to")).replace(nodeCollectionName + "/","")) != null) { //If the graph contain both related nodes of the edge
                    Edge edge = graph.addEdge((String) edgeJson.get("_key"),
                            ((String)edgeJson.get("_from")).replace(nodeCollectionName + "/",""),
                            ((String)edgeJson.get("_to")).replace(nodeCollectionName + "/",""));
                    boolean edgeWeighted = false;
                    for (String key : edgeJson.keySet()) {
                        if (weighEdges && key.equals("weight")) {
                            graph.setEdgeWeight(edge, Double.parseDouble(edgeJson.get("weight").toString()));
                            edgeWeighted = true;
                        }
                        else if(!key.equals("_key") && !key.equals("_rev") && !key.equals("_id") && !key.equals("_from") && !key.equals("_to")) { //If we actually have some of the objects data and not just a key
                            graph.setEdgeExtraInfo(edge, graph.getEdgeExtraInfo(edge).appendField(key,edgeJson.get(key)));
                        }
                    }
                    if(weighEdges && !edgeWeighted) { //I.e. if we didnt find an edge attribute
                        graph.setEdgeWeight(edge, 0); // Then give edge a zero weight
                    }
                }
            }
        }
    }

    //TODO: Account for list vs list comparisons
    @Override
    public CustomGraph readGraph() throws AdapterException {
        CustomGraph graph;
        if(dateAttributeName != null) {
            graph = new CustomGraphTimed();
        }
        else {
            graph = new CustomGraph();
        }

        JSONObject graphExtraInfo = graph.getExtraInfo();
        JSONObject graphExtraInfoIdentifiers = new JSONObject();
        graphExtraInfoIdentifiers.put("arangoDatabase",databaseName.toString());
        graphExtraInfoIdentifiers.put("arangoNodeCollection",nodeCollectionName);
        JSONArray edgeCollections = new JSONArray();
        edgeCollections.addAll(edgeCollectionNames);
        graphExtraInfoIdentifiers.put("arangoEdgeCollections", edgeCollections);
        graphExtraInfo.put("identifiers",graphExtraInfoIdentifiers);

        if(graph instanceof CustomGraphTimed timedGraph) {
            if (startDate != null) {
                // Advance start time a little since due to floating point shenanigans,
                // start and end time can sometimes not be considered to be on the same date when they are on exactly the same time
                startDate.setTime(startDate.getTime() + TimeUnit.MILLISECONDS.toMillis(1));
            }
            else {
                startDate = new Date(Long.MIN_VALUE);
            }
            timedGraph.setStartDate(startDate);
            timedGraph.setEndDate((endDate != null ? endDate : new Date(Long.MAX_VALUE)));
        }

        HashMap<String,Node> keyNodeMap = queryNodes(graph);
        queryEdges(graph, keyNodeMap);

        return graph;
    }


}