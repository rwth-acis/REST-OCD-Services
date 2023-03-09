package i5.las2peer.services.ocd.adapters.utilOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.graphstream.graph.Node;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class AttributeClusterOutputAdapter extends AbstractClusterOutputAdapter {
    CustomGraph graph = null;
    String[] attributeKeyNesting = null;

    String attributeValueString = null;
    Double attributeValueNumber = null;
    Boolean attributeValueBoolean = null;
    Date attributeValueDate = null;

    String operator = null;

    //TODO: Check if comparable first?
    private <T> String compareAndReturnTextResult(Comparable<T> firstVal, T secondVal) {
        if (secondVal != null) {
            if (firstVal.compareTo(secondVal) == 0) {
                return "equal";
            } else if (firstVal.compareTo(secondVal) > 0) {
                return "greater";
            } else if (firstVal.compareTo(secondVal) < 0) {
                return "smaller";
            }
        }
        return "not comparable";
    }

    private String clusterByAttributeValue(String keyNestingValue) {
        if(keyNestingValue == null) {
            return "not comparable";
        }
        if (operator.equals("==")) {
            return keyNestingValue;
        }
        else {
            if (attributeValueNumber != null) {
                try {
                    Double keyNestingValueNumber = Double.parseDouble(keyNestingValue);
                    return compareAndReturnTextResult(attributeValueNumber, keyNestingValueNumber);
                } catch (NumberFormatException e) {
                    return "not comparable";
                }
            } else if (attributeValueBoolean != null) {
                if (keyNestingValue.equalsIgnoreCase("true") || keyNestingValue.equalsIgnoreCase("false")) {
                    Boolean keyNestingValueBoolean = Boolean.parseBoolean(keyNestingValue);
                    return compareAndReturnTextResult(attributeValueBoolean, keyNestingValueBoolean);
                }
                return "not comparable";
            } else if (attributeValueDate != null) {
                try {
                    Date keyNestingValueDate = DateUtils.parseDate(keyNestingValue, "YYYY-MM-DD'T'HH:MM:SS.SSS'Z'", "YYYY-MM-DD'T'HH:MM:SS'Z'", "YYYY-MM-DD'Z'", "YYYY-MM-DD'T'HH:MM:SS.SSS", "YYYY-MM-DD'T'HH:MM:SS", "YYYY-MM-DD");
                    return compareAndReturnTextResult(attributeValueDate, keyNestingValueDate);
                } catch (ParseException e) {
                    return "not comparable";
                }
            }
        }
        return null;
    }

    //TODO: Check if it is really save to use use stringified values as key.
    private String findJSONNestingValue(Node node) {
        Stack<ImmutablePair<Object,Integer>> nestedObjectStack = new Stack<>();
        nestedObjectStack.push(new ImmutablePair<>(graph.getNodeExtraInfo(node),0));
        //int k=0;        //k++ k<attributeKeyNesting.length //TODO: Manage k on popping
        while (!nestedObjectStack.isEmpty()) {
            ImmutablePair<Object,Integer> currentStackTop = nestedObjectStack.pop();
            Object currentNestedObject = currentStackTop.getLeft();
            Integer currentNestingCount = currentStackTop.getRight();

            if (currentNestingCount != attributeKeyNesting.length) {
                String nesting = attributeKeyNesting[currentNestingCount];
                if (currentNestedObject instanceof JSONObject currentNestedObjectJSON) {
                    if (currentNestedObjectJSON.containsKey(nesting)) {
                        nestedObjectStack.push(new ImmutablePair<>(currentNestedObjectJSON.get(nesting), currentNestingCount + 1));
                    } else if (currentNestedObjectJSON.containsKey("value")) { //Branch entirely just for wikidata-like objects
                        Object currentNestedObjectValue = currentNestedObjectJSON.get("value");
                        if (currentNestingCount == attributeKeyNesting.length - 1 && currentNestedObjectValue != null && !(currentNestedObjectValue instanceof JSONObject) && !(currentNestedObjectValue instanceof JSONArray))
                        {
                            //nestedObjectStack.push(new ImmutablePair<>(currentNestedObjectValue, currentNestingCount));
                            return currentNestedObjectValue.toString();
                        } else if (currentNestedObjectValue instanceof String valueString && valueString.equals(nesting) && currentNestedObjectJSON.containsKey("qualifiers") && currentNestedObjectJSON.get("qualifiers") instanceof JSONArray)
                        {
                            nestedObjectStack.push(new ImmutablePair<>(currentNestedObjectJSON.get(nesting), currentNestingCount + 1));
                        }
                    }
                    //Otherwise do nothing since object didnt yield what we wanted
                } else if (currentNestedObject instanceof JSONArray currentNestedObjectArray) {//This is essentially also just for wikidata
                    if (!currentNestedObjectArray.isEmpty()) {
                        for (Object arrayElem : currentNestedObjectArray) {
                            nestedObjectStack.push(new ImmutablePair<>(arrayElem, currentNestingCount));
                        }
                    }
                    //Otherwise do nothing since object didnt yield what we wanted
                } else if (currentNestingCount == attributeKeyNesting.length - 1) {
                    if (currentNestedObject != null) { //The object must then be Integer,Double,String or Boolean, all safely stringifiable to be map keys
                        return currentNestedObject.toString();
                    }
                    //Otherwise do nothing since object didnt yield what we wanted
                }
                //Otherwise do nothing since object didnt yield what we wanted
            }
        }
        return null;
    }

    @Override
    public void writeCluster() throws AdapterException {
        HashMap<String,Integer> attributeClusterNumbers = new HashMap<>();
        HashMap<String,Integer> nodeIDClusterMap = new HashMap<>();

        int i = 0;
        for (Iterator<Node> it = graph.nodes().iterator(); it.hasNext(); ) {
            Node node = it.next();

            String keyNestingValue = findJSONNestingValue(node);
            String clusterName = clusterByAttributeValue(keyNestingValue);
            if (!attributeClusterNumbers.containsKey(clusterName)) {
                attributeClusterNumbers.put(clusterName,i);
                nodeIDClusterMap.put(node.getId(), i);
                i++;
            }
            nodeIDClusterMap.put(node.getId(),attributeClusterNumbers.get(clusterName));
        }
        JSONObject clusterObject = new JSONObject();
        clusterObject.put("number_clusters",i+1);
        clusterObject.put("cluster_nodes",nodeIDClusterMap);

        try {
            writer.write(clusterObject.toJSONString());
        }
        catch (IOException e) {
            throw new AdapterException("Could not write Cluster: " + e.getMessage());
        }
    }

    @Override
    public void setParameter(CustomGraph graphParam, Map<String, String> param) throws IllegalArgumentException, ParseException {
        if (graphParam != null) {
            graph = graphParam;
        }
        else {
            throw new IllegalArgumentException("Did not get a valid graph");
        }

        if (param.containsKey("operator")) {
            List<String> possibleOperators = List.of("==","><");
            if(!possibleOperators.contains(param.get("operator"))) {
                throw new IllegalArgumentException("Can't use this operator for cluster generation");
            }
            operator = param.get("operator");

            param.remove("operator");
        }
        else {
            throw new IllegalArgumentException("Did not get an attribute key to sort by");
        }

        if (param.containsKey("attributeValue")) {
            if(operator.equals("><")) {
                try {
                    attributeValueNumber = Double.parseDouble(param.get("attributeValue"));
                }
                catch (NumberFormatException doubleExc) {
                    //Using own check here since parseBoolean always produces a boolean no matter whether the string said anything sensible
                    if(param.get("attributeValue").equalsIgnoreCase("true") || param.get("attributeValue").equalsIgnoreCase("false")) {
                        attributeValueBoolean = Boolean.parseBoolean(param.get("attributeValue"));
                    }
                    else {
                        try {
                            attributeValueDate = DateUtils.parseDate(param.get("attributeValue"), "YYYY-MM-DD'T'HH:MM:SS.SSS'Z'", "YYYY-MM-DD'T'HH:MM:SS'Z'", "YYYY-MM-DD'Z'", "YYYY-MM-DD'T'HH:MM:SS.SSS", "YYYY-MM-DD'T'HH:MM:SS", "YYYY-MM-DD");
                        }
                        catch (ParseException dateExc) {
                            throw new IllegalArgumentException("Attribute Value is not comparable via operator");
                        }
                    }
                }
            }
            else {
                attributeValueString = param.get("attributeValue");
            }

            param.remove("attributeValue");
        }
        else {
            throw new IllegalArgumentException("Did not get an attribute key to sort by");
        }

        if (param.containsKey("attributeKeyNesting")) {
            attributeKeyNesting = param.get("attributeKeyNesting").split(":::");
//
//            boolean foundAttribute = false;
//            for (Iterator<Node> it = graph.nodes().iterator(); it.hasNext(); ) {
//                Node node = it.next();
//                JSONObject keyNestedObject = graph.getNodeExtraInfo(node);
//                for (int k = 0; k < attributeKeyNesting.length; k++) {
//                    String nesting = attributeKeyNesting[k];
//                    if (keyNestedObject.containsKey(nesting)) {
//                        if (keyNestedObject.get(nesting) instanceof JSONObject) {
//                            keyNestedObject = (JSONObject) keyNestedObject.get(nesting);
//                        } else if (k == attributeKeyNesting.length - 1) {
//                            if (keyNestedObject.get(nesting) instanceof JSONArray || keyNestedObject.get(nesting) instanceof JSONObject) {
//                                throw new IllegalArgumentException("Can't cluster by Array/Map attributes");
//                            }
//                            foundAttribute = true;
//                        } else {
//                            throw new IllegalArgumentException("There's no nesting ");
//                        }
//                    } else {
//                        break;
//                    }
//                }
//                if(foundAttribute){
//                    break;
//                }
//            }

            param.remove("attributeKeyNesting");
        }
        else {
            throw new IllegalArgumentException("Did not get an attribute key to sort by");
        }

        if(!param.isEmpty()) {
            System.out.println("PARAM: " + param.toString()); //TODO: Remove
            throw new IllegalArgumentException();
        }
    }
}
