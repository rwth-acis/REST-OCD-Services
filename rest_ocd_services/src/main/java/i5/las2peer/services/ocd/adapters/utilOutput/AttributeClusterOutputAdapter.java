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
    CustomGraph graph;
    String[] attributeKeyNesting = null;

    String attributeValueString = null;
    Double attributeValueNumber = null;
    Boolean attributeValueBoolean = null;
    Date attributeValueDate = null;

    String operator = null;
	
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
                            attributeValueDate = DateUtils.parseDate(param.get("attributeValue"), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
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

            //System.out.println("attrBool: " + attributeValueBoolean);
            //System.out.println("attrNumber: " + attributeValueNumber);
            //System.out.println("attrDate: " + attributeValueDate);
            //System.out.println("attrString: " + attributeValueString);

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
            //System.out.println("PARAM: " + param.toString()); //TODO: Remove
            throw new IllegalArgumentException();
        }
    }

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
        return "no value";
    }

    //TODO: Cope with multiple possible values
    private HashSet<String> clusterByAttributeValues(HashSet<String> keyNestingValues) {
        if(keyNestingValues.isEmpty()) {
            return new HashSet<String>(List.of("no value"));
        }

        if (operator.equals("==")) {
//            if (keyNestingValues.contains(attributeValueString)) {
//                return attributeValueString;
//            }
//            else {
//                return keyNestingValues.iterator().next();
//            }
            return keyNestingValues;
        }
        else {
            HashSet<String> comparedValues = new HashSet<String>();
            Iterator<String> nestingIt = keyNestingValues.iterator();
            if (attributeValueNumber != null) {
                while (nestingIt.hasNext()) {
                    String keyNestingValue = nestingIt.next();
                    if (keyNestingValues.contains(Double.toString(attributeValueNumber))) {
                        keyNestingValue = Double.toString(attributeValueNumber);
                    }
                    try {
                        Double keyNestingValueNumber = Double.parseDouble(keyNestingValue);
                        comparedValues.add(compareAndReturnTextResult(keyNestingValueNumber, attributeValueNumber));
                    } catch (NumberFormatException e) {
                        comparedValues.add("not comparable");
                    }
                }
            } else if (attributeValueBoolean != null) {
                while (nestingIt.hasNext()) {
                    String keyNestingValue = nestingIt.next();
                    if (keyNestingValues.contains(Boolean.toString(attributeValueBoolean))) {
                        keyNestingValue = Boolean.toString(attributeValueBoolean);
                    }
                    if (keyNestingValue.equalsIgnoreCase("true") || keyNestingValue.equalsIgnoreCase("false")) {
                        Boolean keyNestingValueBoolean = Boolean.parseBoolean(keyNestingValue);
                        comparedValues.add(compareAndReturnTextResult(keyNestingValueBoolean, attributeValueBoolean));
                    }
                    comparedValues.add("not comparable");
                }
            } else if (attributeValueDate != null) {
                while (nestingIt.hasNext()) {
                    String keyNestingValue = nestingIt.next();
                    if (keyNestingValues.contains(attributeValueDate.toInstant().toString())) {
                        keyNestingValue = attributeValueDate.toInstant().toString();
                    }
                    try {
                        Date keyNestingValueDate = DateUtils.parseDate(keyNestingValue, "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
                        comparedValues.add(compareAndReturnTextResult(keyNestingValueDate, attributeValueDate));
                    } catch (ParseException e) {
                        comparedValues.add("not comparable");
                    }
                }
            }
            return comparedValues;
        }
    }

    //TODO: Check if it is really save to use use stringified values as key.
    private HashSet<String> findJSONNestingValues(Node node) {
        Stack<ImmutablePair<Object,Integer>> nestedObjectStack = new Stack<>();
        nestedObjectStack.push(new ImmutablePair<>(graph.getNodeExtraInfo(node),0));
        //int k=0;        //k++ k<attributeKeyNesting.length //TODO: Manage k on popping
        HashSet<String> foundValues = new HashSet<>();
        while (!nestedObjectStack.isEmpty()) {
            ImmutablePair<Object,Integer> currentStackTop = nestedObjectStack.pop();
            Object currentNestedObject = currentStackTop.getLeft();
            Integer currentNestingCount = currentStackTop.getRight();

            if (currentNestingCount < attributeKeyNesting.length) {
                String nesting = attributeKeyNesting[currentNestingCount];
                if (currentNestedObject instanceof Map currentNestedObjectMap) {
                    if (currentNestedObjectMap.containsKey(nesting)) {
                        nestedObjectStack.push(new ImmutablePair<>(currentNestedObjectMap.get(nesting), currentNestingCount + 1));
                    } else if (currentNestedObjectMap.containsKey("value")) { //Branch entirely just for wikidata-like objects
                        Object currentNestedObjectValue = currentNestedObjectMap.get("value");
                        if (currentNestedObjectValue instanceof String valueString && valueString.equals(nesting) && currentNestedObjectMap.containsKey("qualifiers") && currentNestedObjectMap.get("qualifiers") instanceof Map qualifierMap)
                        {
                            nestedObjectStack.push(new ImmutablePair<>(qualifierMap, currentNestingCount + 1));
                        }
                    }
                    //Otherwise do nothing since object didnt yield what we wanted
                } else if (currentNestedObject instanceof List currentNestedObjectList) {//This is essentially also just for wikidata
                    if (!currentNestedObjectList.isEmpty()) {
                        for (Object arrayElem : currentNestedObjectList) {
                            nestedObjectStack.push(new ImmutablePair<>(arrayElem, currentNestingCount));
                        }
                    }
                    //Otherwise do nothing since object didnt yield what we wanted
                }
            }
            else if (currentNestingCount == attributeKeyNesting.length) {
                if (currentNestedObject instanceof Map currentNestedObjectMap) { //Branch just for wikidata objects
                    Object currentNestedObjectValue = currentNestedObjectMap.get("value");
                    if (currentNestedObjectValue != null && !(currentNestedObjectValue instanceof Map) && !(currentNestedObjectValue instanceof List))
                    {
                        foundValues.add(currentNestedObjectValue.toString());
                    }
                }
                else if (currentNestedObject instanceof List currentNestedObjectList) { //Branch just for wikidata objects
                    for (Object currentNestedObjectValue : currentNestedObjectList) {
                        if (currentNestedObjectValue != null) {
                            if (!(currentNestedObjectValue instanceof Map) && !(currentNestedObjectValue instanceof List)) {
                                foundValues.add(currentNestedObjectValue.toString());
                            } else if (currentNestedObjectValue instanceof Map currentNestedObjectValueMap && currentNestedObjectValueMap.get("value") != null) {
                                foundValues.add(currentNestedObjectValueMap.get("value").toString());
                            }
                        }
                    }
                }
                else if (currentNestedObject != null) { //The object must then be Integer,Double,String or Boolean, all safely stringifiable to be map keys
                    foundValues.add(currentNestedObject.toString());
                }
                //Otherwise do nothing since object didnt yield what we wanted
            }
        }
        return foundValues;
    }

    @Override
    public void writeCluster() throws AdapterException {
        HashSet<String> attributeClusters = new HashSet<>();
        HashMap<String,List<String>> nodeClusterMap = new HashMap<>();

        //int i = 0;
        for (Iterator<Node> it = graph.nodes().iterator(); it.hasNext(); ) {
            Node node = it.next();

            HashSet<String> keyNestingValues = findJSONNestingValues(node);
            HashSet<String> clusterNames = clusterByAttributeValues(keyNestingValues);
            attributeClusters.addAll(clusterNames);
            nodeClusterMap.put(graph.getNodeKey(node), new ArrayList<>(clusterNames));
            //i++;
        }
        JSONObject clusterObject = new JSONObject();
        clusterObject.put("clusters",attributeClusters);
        clusterObject.put("cluster_nodes",nodeClusterMap);

        try {
            writer.write(clusterObject.toJSONString());
        }
        catch (IOException e) {
            throw new AdapterException("Could not write Cluster: " + e.getMessage());
        }
    }
}
