package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.DocumentCreateEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.la4j.matrix.Matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * The DescriptiveVisualization class is used for descriptive visualization of the calculations that the underlying OCD algorithm performs on the input graph until the final cover.
 * It provides methods for setting and retrieving visualization options and for saving and manipulating graph data.
 */
public class DescriptiveVisualization {
    CustomGraph graphComponent;
    int component = 0;
    int iteration = 0;
    private int[] previousRgb = {0, 0, 0};
    List<int[]> usedRgbs = new ArrayList<>();
    public static boolean visualize = false;
    public static boolean deleteEnabled = false;
    public static String key = "";
    public static JsonData data = new JsonData("", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    /**
     * Sets the visualization option.
     * @param visualize true to enable visualization, false otherwise
     */
    public static void setVisualize(boolean visualize){
        DescriptiveVisualization.visualize = visualize;
        DescriptiveVisualization.deleteEnabled = visualize;
    }

    /**
     * Returns the current visualization option.
     * @return true if visualization is enabled, false otherwise
     */
    public static boolean getVisualize(){
        return DescriptiveVisualization.visualize;
    }

    /**
     * Sets the JSON key for the graph data.
     * @param key the JSON key to set
     */
    public static void setJsonKey(String key){
        DescriptiveVisualization.key = key;
    }

    /**
     * Constructs a new DescriptiveVisualization object.
     */
    public DescriptiveVisualization(){
        reset();
    }

    /**
     * Custom serializer for Node objects.
     */
    @JsonSerialize(using = NodeSerializer.class)
    static class Node {
        int id;
        String label;
        int degree;
        List<Double> numValue = new ArrayList<>();
        List<String> stringValue = new ArrayList<>();
        List<int[]> rgbValue = new ArrayList<>();

        /**
         * Constructs a new Node object with the given ID.
         * @param id the ID of the node
         */
        Node(int id) {
            this.id = id;
            this.label = "node" + id;
            this.degree = 0;
            this.numValue.add(Double.MIN_VALUE);
            this.stringValue.add("");
            this.rgbValue.add(new int[]{255, 255, 255});
        }

        private void setNumValue(double numValue) {
            this.numValue.add(numValue);
        }
        private void setStringValue(String stringValue) {
            this.stringValue.add(stringValue);
        }
        private void setRgbValue(int[] rgbValue) {
            this.rgbValue.add(rgbValue);
        }
        private int[] getRgbValue(int i){
            return this.rgbValue.get(i);
        }
        private void setLabel(String label){
            this.label = label;
        }
        private void setDegree(int degree){
            this.degree = degree;
        }
    }

    /**
     * Custom serializer for Edge objects.
     */
    @JsonSerialize(using = EdgeSerializer.class)
    static class Edge {
        int source;
        int target;
        List<Double> numValue = new ArrayList<>();
        List<String> stringValue = new ArrayList<>();
        List<int[]> rgbValue = new ArrayList<>();

        /**
         * Constructs a new Edge object with the given source and target IDs.
         * @param source the ID of the source node
         * @param target the ID of the target node
         */
        Edge(int source, int target) {
            this.source = source;
            this.target = target;
            this.numValue.add(Double.MIN_VALUE);
            this.stringValue.add("");
            this.rgbValue.add(new int[]{0, 0, 0});
        }

        private void setNumValue(double numValue) {
            this.numValue.add(numValue);
        }
        private void setStringValue(String stringValue) {
            this.stringValue.add(stringValue);
        }
        private void setRgbValue(int[] rgbValue) {
            this.rgbValue.add(rgbValue);
        }
        private int[] getRgbValue(int i){
            return this.rgbValue.get(i);
        }
    }

    /**
     * Custom serializer for JsonData objects.
     */
    @JsonSerialize(using = JsonDataSerializer.class)
    static class JsonData {
        String _key = "";
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        List<String> shortDescription = new ArrayList<>();
        List<String> detailedDescription = new ArrayList<>();

        /**
         * Constructs a new JsonData object with the given properties.
         * @param _key the JSON key
         * @param nodes the list of nodes
         * @param edges the list of edges
         * @param shortDescription the list of short descriptions
         * @param detailedDescription the list of detailed descriptions
         */
        private JsonData(String _key, List<Node> nodes, List<Edge> edges, List<String> shortDescription, List<String> detailedDescription) {
            this._key = _key;
            this.nodes = nodes;
            this.edges = edges;
            this.shortDescription = shortDescription;
            this.detailedDescription = detailedDescription;
        }

        private void addNodes(List<Node> nodes){
            this.nodes.addAll(nodes);
        }
        private void addEdges(List<Edge> edges){
            this.edges.addAll(edges);
        }
        private List<Node> getNodes() {
            return this.nodes;
        }
        private List<Edge> getEdges() {
            return this.edges;
        }
        private void setKey(String _key){
            this._key = _key;
        }
        private void setShortDescription(List<String> shortDescription) {
            this.shortDescription = shortDescription;
        }
        private void setDetailedDescription(List<String> detailedDescription) {
            this.detailedDescription = detailedDescription;
        }
    }

    // Color constants
    int[] white = new int[]{255, 255, 255};
    int[] black = new int[]{0, 0, 0};
    int[] gray = new int[]{220, 220, 220};
    int tempIteration = 0;

    /**
     * Generates a new color for visualization.
     * @return an array representing the RGB values of the new color
     */
    private int[] newColor() {
        this.usedRgbs.add(this.previousRgb);
        if(this.iteration > this.tempIteration){
            this.usedRgbs.clear();
            this.tempIteration += 1;
        }

        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        int[] newRgb = {red, green, blue};
        int rgbSum = newRgb[0] + newRgb[1] + newRgb[2];
        int attempts = 0;

        // Generate new color until it meets the criteria
        while ((checkColors(newRgb, this.usedRgbs) || newRgb == this.gray ||  rgbSum < 100 || rgbSum > 650) && (attempts < 5)) {
            red = random.nextInt(256);
            green = random.nextInt(256);
            blue = random.nextInt(256);
            newRgb = new int[] {red, green, blue};
            rgbSum = newRgb[0] + newRgb[1] + newRgb[2];
            attempts++;
        }

        this.previousRgb = newRgb;
        return newRgb;
    }

    /**
     * Checks if a color is too similar to any of the previously used colors.
     * @param color the color to check
     * @param usedRgbs the list of used RGB values
     * @return true if the color is too similar, false otherwise
     */
    private boolean checkColors(int[] color, List<int[]> usedRgbs){
        boolean result = false;
        for (int[] rgb : usedRgbs){
            if(tooSimilar(color, rgb)){
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Calculates the squared Euclidean distance between two RGB values.
     * @param rgb1 the first RGB value
     * @param rgb2 the second RGB value
     * @return true if the distance between the colors is less than a threshold, false otherwise
     */
    private boolean tooSimilar(int[] rgb1, int[] rgb2) {
        int deltaRed = rgb1[0] - rgb2[0];
        int deltaGreen = rgb1[1] - rgb2[1];
        int deltaBlue = rgb1[2] - rgb2[2];
        int distanceSquare = (deltaRed * deltaRed) + (deltaGreen * deltaGreen) + (deltaBlue * deltaBlue);
        return (distanceSquare < 500); // Return true if the distance is less than a threshold
    }

    /**
     * Updates the labels of nodes in the graph.
     * @param labels a map containing the updated labels for each node
     */
    private void updateLabels(HashMap<Node, String> labels){
        for (Node node : labels.keySet()) {
            node.setLabel(labels.get(node));
        }
    }

    /**
     * Updates the numerical values and colors of nodes in the graph.
     * @param newNumValues a map containing the updated numerical values for each node
     */
    private void updateNodeNumValues(HashMap<Node, Double> newNumValues){
        int[] color = newColor();
        for (Node node : newNumValues.keySet()) {
            node.numValue.set(this.iteration, newNumValues.get(node));
            node.rgbValue.set(this.iteration, color);
        }
    }

    /**
     * Updates the string values and colors of nodes in the graph.
     * @param newStringValues a map containing the updated string values for each node
     */
    private void updateNodeStringValues(HashMap<Node, String> newStringValues){
        int[] color = newColor();
        for (Node node : newStringValues.keySet()) {
            node.stringValue.set(this.iteration, newStringValues.get(node));
            node.rgbValue.set(this.iteration, color);
        }
    }

    /**
     * Updates the numerical values and colors of edges in the graph.
     * @param newNumValues a map containing the updated numerical values for each edge
     */
    private void updateEdgeNumValues(HashMap<Edge, Double> newNumValues){
        int[] color = newColor();
        for (Edge edge : newNumValues.keySet()) {
            edge.numValue.set(this.iteration, newNumValues.get(edge));
            edge.rgbValue.set(this.iteration, color);
        }
    }

    /**
     * Updates the string values and colors of edges in the graph.
     * @param newStringValues a map containing the updated string values for each edge
     */
    private void updateEdgeStringValues(HashMap<Edge, String> newStringValues){
        int[] color = newColor();
        for (Edge edge : newStringValues.keySet()) {
            edge.stringValue.set(this.iteration, newStringValues.get(edge));
            edge.rgbValue.set(this.iteration, color);
        }
    }

    /**
     * Converts the graph data to JSON format.
     * @param data the graph data to be converted
     * @return the JSON representation of the graph data
     * @throws JsonProcessingException if an error occurs during JSON processing
     */
    private static String convert(JsonData data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);
        return json;
    }

    /**
     * Resets the graph data and visualization options.
     */
    private static void reset(){
        DescriptiveVisualization.data = new JsonData("", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        DescriptiveVisualization.visualize = false;
    }

    /**
     * Sets the default values for nodes and edges in the graph data.
     * This method is used to ensure that all nodes and edges have values for each iteration in the graph data.
     * If a node or edge does not have a value for the current iteration, default values are assigned based on certain conditions.
     * After setting the default values, the iteration counter is incremented.
     */
    private void setDefaultValues() {
        for(Node node : data.getNodes()){
            if(node.numValue.size() < data.shortDescription.size()) {
                if (node.getRgbValue(this.iteration)[0] + node.getRgbValue(this.iteration)[1] + node.getRgbValue(this.iteration)[2] != 765 && node.getRgbValue(this.iteration)[0] + node.getRgbValue(this.iteration)[1] + node.getRgbValue(this.iteration)[2] != 660) {
                    node.setNumValue(node.numValue.get(this.iteration));
                    node.setStringValue(node.stringValue.get(this.iteration));
                    node.setRgbValue(gray);
                } else {
                    node.setNumValue(Double.MIN_VALUE);
                    node.setStringValue("");
                    node.setRgbValue(white);
                }
            }
        }
        for(Edge edge : data.getEdges()){
            if(edge.numValue.size() < data.shortDescription.size()) {
                if (edge.getRgbValue(this.iteration)[0] + edge.getRgbValue(this.iteration)[1] + edge.getRgbValue(this.iteration)[2] != 0 && edge.getRgbValue(this.iteration)[0] + edge.getRgbValue(this.iteration)[1] + edge.getRgbValue(this.iteration)[2] != 660) {
                    edge.setNumValue(edge.numValue.get(this.iteration));
                    edge.setStringValue(edge.stringValue.get(this.iteration));
                    edge.setRgbValue(gray);
                } else {
                    edge.setNumValue(Double.MIN_VALUE);
                    edge.setStringValue("");
                    edge.setRgbValue(black);
                }
            }
        }
        this.iteration += 1;
    }

//------------Methods that can be used in the OCD alogrithms------------//

    /**
     * Adds the graph component from the OCD (Overlapping Community Detection) algorithm input graph to the visualization data.
     * This method takes a CustomGraph object representing a graph component and adds its nodes and edges to the visualization data.
     * @param graph The CustomGraph object representing the graph component to be added.
     */
    public void addComponent(CustomGraph graph) {
        this.graphComponent = graph;

        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < graph.getNodeCount(); i++){
            Node node = new Node(getRealNode(i));
            node.setDegree(graph.getNode(i).getDegree() / 2);
            nodes.add(node);
        }

        List<Edge> edges = new ArrayList<>();
        ArrayList<ArrayList<Integer>> edges_temp = new ArrayList<>();
        for(int i = 0; i < graph.getEdgeCount(); i++){
            int source;
            int target;
            if (graph.getEdge(i).getSourceNode().getId().charAt(0) == '0') {
                source = Integer.valueOf(graph.getEdge(i).getSourceNode().getId());
                target = Integer.valueOf(graph.getEdge(i).getTargetNode().getId());
            }
            else {
                source = Integer.valueOf(graph.getEdge(i).getSourceNode().getId().substring(1 + component / 10));
                target = Integer.valueOf(graph.getEdge(i).getTargetNode().getId().substring(1 + component / 10));
            }
            ArrayList<Integer> edge1 = new ArrayList<>();
            ArrayList<Integer> edge2 = new ArrayList<>();
            edge1.addAll(Arrays.asList(source, target));
            edge2.addAll(Arrays.asList(target, source));
            if(!edges_temp.contains(edge1) && !edges_temp.contains(edge2)){
                edges_temp.add(edge1);
                edges.add(new Edge(source, target));
            }
        }

        data.addNodes(nodes);
        data.addEdges(edges);

        // Sort the nodes by their IDs
        Collections.sort(data.getNodes(), new Comparator<Node>() {
            @Override
            public int compare(Node node1, Node node2) {
                return Integer.compare(node1.id, node2.id);
            }
        });
    }

    /**
     * Retrieves the real node ID based on the provided index.
     * @param i The index of the node.
     * @return The real node ID.
     */
    public int getRealNode(int i){
        int id = i;
        try {
            if (this.graphComponent.getNode(i).getId().charAt(0) == '0') {
                id = Integer.valueOf(this.graphComponent.getNode(i).getId());
            } else {
                id = Integer.valueOf(this.graphComponent.getNode(i).getId().substring(1 + component / 10));
            }
        }catch (Exception e){
            System.out.println(e);
        }
        return id;
    }

    /**
     * Retrieves the real edge based on the provided edge representation.
     * @param s The source node ID of the underlying edge.
     * @param t The target node ID of the underlying edge.
     * @return The real edge representation as an ArrayList of integers.
     */
    public ArrayList<Integer> getRealEdge(int s, int t){
        ArrayList<Integer> realEdge = new ArrayList<>();
        int source = getRealNode(s);
        int target = getRealNode(t);
        if(source < target){
            realEdge.add(source);
            realEdge.add(target);
        }
        else {
            realEdge.add(target);
            realEdge.add(source);
        }
        return realEdge;
    }

    /**
     * Sets the descriptions for the graph data.
     * @param descriptionPath The path to the file containing the descriptions.
     * @param delimiter The delimiter used to separate the columns in the file.
     */
    public void setDescriptions(String descriptionPath, String delimiter) {
        List<String> shortDescription = new ArrayList<>();
        shortDescription.add("Initial Network");
        List<String> detailedDescription = new ArrayList<>();
        detailedDescription.add("This is the unprocessed initial network.");

        try (BufferedReader br = new BufferedReader(new FileReader(descriptionPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(delimiter);
                if (columns.length > 0) {
                    String firstElement = columns[0].replaceAll("^\"|\"$", ""); // remove double quotes from first column
                    shortDescription.add(firstElement);
                    String secondElement = columns[1].replaceAll("^\"|\"$", ""); // remove double quotes from second column
                    detailedDescription.add(secondElement);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        data.setShortDescription(shortDescription);
        data.setDetailedDescription(detailedDescription);
    }

    /**
     * Sets the labels for the nodes in the graph data.
     * @param labels A HashMap containing node IDs and their corresponding labels.
     */
    public void setNodeLabels(HashMap<Integer, String> labels) {
        HashMap<Node, String> nodeLabels = new HashMap<>();
        try {
            for (int id : labels.keySet()) {
                for(Node node : data.getNodes()){
                    if(id == node.id){
                        nodeLabels.put(node, labels.get(id));
                    }
                }
            }
            updateLabels(nodeLabels);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets the numerical values for the nodes in the graph data for a specific iteration.
     * @param iteration The iteration for which to set the values.
     * @param values A HashMap containing node IDs and their corresponding numerical values.
     */
    public void setNodeNumericalValues(int iteration, HashMap<Integer, Double> values) {
        if(iteration < this.iteration){
            this.component += 1;
            this.iteration = 0;
        }
        if(iteration > this.iteration) {
            setDefaultValues();
        }
        HashMap<Node, Double> nodeNumValues = new HashMap<>();
        try {
            for(int id : values.keySet()) {
                int realNode = getRealNode(id);
                for(Node node : data.getNodes()){
                    if(realNode == node.id){
                        nodeNumValues.put(node, values.get(id));
                    }
                }
            }
            updateNodeNumValues(nodeNumValues);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets the string values for the nodes in the graph data for a specific iteration.
     * @param iteration The iteration for which to set the values.
     * @param values A HashMap containing node IDs and their corresponding string values.
     */
    public void setNodeStringValues(int iteration, HashMap<Integer, String> values) {
        if(iteration < this.iteration){
            this.component += 1;
            this.iteration = 0;
        }
        if (iteration > this.iteration) {
            setDefaultValues();
        }
        HashMap<Node, String> nodeStringValues = new HashMap<>();
        try {
            for(int id : values.keySet()) {
                int realNode = getRealNode(id);
                for(Node node : data.getNodes()){
                    if(realNode == node.id){
                        nodeStringValues.put(node, values.get(id));
                    }
                }            }
            updateNodeStringValues(nodeStringValues);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets numerical values for edges in the graph based on the given iteration and a HashMap of edge-value pairs.
     * @param iteration The iteration for which to set the values.
     * @param values A HashMap containing edge information as keys and their corresponding numerical values.
     *               The key is an ArrayList of two integers representing the source and target nodes of the edge,
     *               and the value is a Double representing the numerical value associated with the edge.
     */
    public void setEdgeNumericalValues(int iteration, HashMap<ArrayList<Integer>, Double> values) {
        if(iteration < this.iteration){
            this.component += 1;
            this.iteration = 0;
        }
        if(iteration > this.iteration) {
            setDefaultValues();
        }
        HashMap<Edge, Double> edgeNumValues = new HashMap<>();
        try {
            for(ArrayList<Integer> edge : values.keySet()) {
                ArrayList<Integer> realEdge = getRealEdge(edge.get(0), edge.get(1));
                for(Edge e : data.getEdges()) {
                    if((e.source == realEdge.get(0) || e.source == realEdge.get(1)) && (e.target == realEdge.get(0) || e.target == realEdge.get(1))) {
                        edgeNumValues.put(e, values.get(edge));
                        break;
                    }
                }
            }
            updateEdgeNumValues(edgeNumValues);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets string values for edges in the graph based on the given iteration and a HashMap of edge-value pairs.
     * @param iteration The iteration for which to set the values.
     * @param values A HashMap containing edge information as keys and their corresponding string values.
     *               The key is an ArrayList of two integers representing the source and target nodes of the edge,
     *               and the value is a String representing the value associated with the edge.
     */
    public void setEdgeStringValues(int iteration, HashMap<ArrayList<Integer>, String> values) {
        if(iteration < this.iteration){
            this.component += 1;
            this.iteration = 0;
        }
        if(iteration > this.iteration) {
            setDefaultValues();
        }
        HashMap<Edge, String> edgeStringValues = new HashMap<>();
        try {
            for(ArrayList<Integer> edge : values.keySet()) {
                ArrayList<Integer> realEdge = getRealEdge(edge.get(0), edge.get(1));
                for(Edge e : data.getEdges()) {
                    if((e.source == realEdge.get(0) || e.source == realEdge.get(1)) && (e.target == realEdge.get(0) || e.target == realEdge.get(1))) {
                        edgeStringValues.put(e, values.get(edge));
                        break;
                    }
                }
            }
            updateEdgeStringValues(edgeStringValues);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets the cover for a specific iteration.
     * @param iteration The iteration for which the cover is set.
     * @param cover The Cover object containing membership information.
     */
    public void setCover(int iteration, Cover cover) {
        Matrix membershipMatrix = cover.getMemberships();
        try {
            List<int[]> communityColors = new ArrayList<>();
            for (int c = 0; c < membershipMatrix.columns(); c++){
                communityColors.add(newColor());
            }
            List<int[]> nodeColors = new ArrayList<>();
            HashMap<Integer, String> initNodes = new HashMap<>();
            for (int i = 0; i < membershipMatrix.rows(); i++){
                initNodes.put(i, "");
                int[] color = new int[]{0, 0, 0};
                double red = 0.0;
                double green = 0.0;
                double blue = 0.0;
                for (int j = 0; j < membershipMatrix.columns(); j++){
                    int[] communityColor = communityColors.get(j);
                    red += membershipMatrix.get(i, j) * communityColor[0];
                    green += membershipMatrix.get(i, j) * communityColor[1];
                    blue += membershipMatrix.get(i, j) * communityColor[2];
                }
                color[0] = (int)red;
                color[1] = (int)green;
                color[2] = (int)blue;
                nodeColors.add(color);
            }
            setNodeStringValues(iteration, initNodes);
            for (int i = 0; i < membershipMatrix.rows(); i++){
                Node node = data.getNodes().get(0);
                for (Node n : data.getNodes()){
                    if (n.id == getRealNode(i)){
                        node = n;
                        break;
                    }
                }
                node.rgbValue.set(iteration, nodeColors.get(i));
            }
            for (Node node : data.getNodes()){
                node.numValue.set(iteration, Double.MIN_VALUE);
                node.stringValue.set(iteration, "");
            }
            for (Edge edge : data.getEdges()){
                edge.numValue.set(iteration, Double.MIN_VALUE);
                edge.stringValue.set(iteration, "");
                edge.stringValue.set(iteration, "");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Saves the JSON data to ArangoDB.
     * @throws IOException If an I/O error occurs while converting the data to JSON.
     */
    public static void saveJson() throws IOException {
        data.setKey(key);
        String json = convert(data);
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("127.0.0.1", Integer.parseInt("8529"))
                .user("root")
                .password("")
                .build();

        // Save Json file to ArangoDB
        String collectionName = "descriptiveVisualization";
        try {
            // Create collection if it doesn't exist
            if (!arangoDB.db("ocdDB").collection(collectionName).exists()) {
                arangoDB.db("ocdDB").createCollection(collectionName);
            }
            // Insert Json document
            DocumentCreateEntity entity = arangoDB.db("ocdDB").collection(collectionName).insertDocument(json);
        } catch (ArangoDBException e) {
            e.printStackTrace();
        } finally {
            arangoDB.shutdown();
        }
    }

}