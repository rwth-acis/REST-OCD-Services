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
 * The DescriptiveVisualization class is used for descriptive visualization of the calculations that the underlying OCD algorithm performs on the input graph up to the final cover.
 * It provides methods for setting and retrieving visualization option and for saving and manipulating graph data.
 */
public class DescriptiveVisualization {
    CustomGraph graphComponent;
    int component = 0;
    int iteration = 0;
    int tempIteration = 0;
    public static boolean visualize = false;
    public static String key = "";
    public static JsonData data = new JsonData("", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    /**
     * Sets the visualization option.
     * @param visualize true to enable visualization, false otherwise.
     */
    public static void setVisualize(boolean visualize){
        DescriptiveVisualization.visualize = visualize;
    }

    /**
     * Returns the current visualization option.
     * @return true if visualization is enabled, false otherwise.
     */
    public static boolean getVisualize(){
        return DescriptiveVisualization.visualize;
    }

    /**
     * Sets the JSON key for the graph data.
     * @param key the JSON key to set.
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
         * @param id the ID of the node.
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
         * @param source the ID of the source node.
         * @param target the ID of the target node.
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
         * @param _key the JSON key.
         * @param nodes the list of nodes.
         * @param edges the list of edges.
         * @param shortDescription the list of short descriptions.
         * @param detailedDescription the list of detailed descriptions.
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

    // Color constants.
    List<int[]> usedRgbs = new ArrayList<>();
    int[] black = new int[]{0, 0, 0};
    int[] gray = new int[]{220, 220, 220};
    int[] white = new int[]{255, 255, 255};

    /**
     * Generates a new color for the communities of the final cover.
     * @return The RGB value of the new color.
     */
    private int[] finalColor() {
        if(iteration > tempIteration){
            usedRgbs.clear();
            tempIteration++;
        }
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        int[] newRgb = {red, green, blue};
        int attempts = 0;

        while ((checkColors(newRgb, usedRgbs) || (newRgb[0] == 220 && newRgb[1] == 220 && newRgb[2] == 220)) && (attempts < 10)) {
            red = random.nextInt(256);
            green = random.nextInt(256);
            blue = random.nextInt(256);
            newRgb = new int[] {red, green, blue};
            attempts++;
        }

        usedRgbs.add(newRgb);
        return newRgb;
    }

    /**
     * Checks if a color is too similar to any of the previously used colors.
     * @param color The RGB color to check.
     * @param usedRgbs A list of used RGB colors to compare against.
     * @return true if the color is too similar, false otherwise.
     */
    private boolean checkColors(int[] color, List<int[]> usedRgbs) {
        for (int[] rgb : usedRgbs){
            if(tooSimilar(color, rgb)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given color is already used in the list of used RGB colors.
     * @param color The RGB color to check.
     * @param usedRgbs A list of used RGB colors to compare against.
     * @return true if the given color is already in the list of used RGB colors, false otherwise.
     */
    private boolean colorIsUsed(int[] color, List<int[]> usedRgbs) {
        for (int[] rgb : usedRgbs){
            if(rgb[0] == color[0] && rgb[1] == color[1] && rgb[2] == color[2]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the squared Euclidean distance between two RGB values.
     * @param rgb1 the first RGB value.
     * @param rgb2 the second RGB value.
     * @return true if the distance between the colors is less than a threshold, false otherwise.
     */
    private boolean tooSimilar(int[] rgb1, int[] rgb2) {
        int threshold = 1500;
        int deltaRed = rgb1[0] - rgb2[0];
        int deltaGreen = rgb1[1] - rgb2[1];
        int deltaBlue = rgb1[2] - rgb2[2];
        int distanceSquare = (deltaRed * deltaRed) + (deltaGreen * deltaGreen) + (deltaBlue * deltaBlue);
        return (distanceSquare < threshold);
    }

    /**
     * Retrieves the distinct RGB colors associated with neighboring nodes in the graph.
     * @param neighbors A List of Integers containing the IDs of neighboring nodes.
     * @return A List of the RGB color values of the neighboring nodes.
     */
    private List<int[]> getNodeNeighborColors(List<Integer> neighbors) {
        List<int[]> neighborColors = new ArrayList<>();
        for (Node node : data.getNodes()) {
            if (neighbors.contains(node.id)) {
                int[] color = node.getRgbValue(iteration);
                if (!neighborColors.contains(color) && !(color[0] == 255 && color[1] == 255 && color[2] == 255) && !(color[0] == 220 && color[1] == 220 && color[2] == 220)) {
                    neighborColors.add(color);
                }
            }
        }
        return neighborColors;
    }

    /**
     * Retrieves the distinct RGB colors associated with neighboring edges in the graph.
     * @param neighbors A List of Edges containing the neighboring edges.
     * @return A List of the RGB color values of the neighboring edges.
     */
    private List<int[]> getEdgeNeighborColors(List<Edge> neighbors) {
        List<int[]> neighborColors = new ArrayList<>();
        for (Edge edge : data.getEdges()) {
            if (neighbors.contains(edge)) {
                int[] color = edge.getRgbValue(iteration);
                if (!neighborColors.contains(color) && !(color[0] == 0 && color[1] == 0 && color[2] == 0) && !(color[0] == 220 && color[1] == 220 && color[2] == 220)) {
                    neighborColors.add(color);
                }
            }
        }
        return neighborColors;
    }

    /**
     * Generates a new RGB color for a node-group based on its neighboring nodes' colors.
     * @param nodes A Set of Nodes representing the neighboring nodes.
     * @return The new RGB color for the underlying node-group.
     */
    private int[] newNodeColor(int iteration, Set<Node> nodes) {
        if(iteration > tempIteration){
            usedRgbs.clear();
            tempIteration++;
        }
        List<Integer> neighbors = new ArrayList<>();
        for (Node node : nodes) {
            List<Integer> nodeNeighbors = new ArrayList<>();
            for (Edge edge : data.getEdges()) {
                if (edge.source == node.id) {
                    nodeNeighbors.add(edge.target);
                }
                if (edge.target == node.id) {
                    nodeNeighbors.add(edge.source);
                }
            }
            neighbors.addAll(nodeNeighbors);
        }
        neighbors.removeAll(nodes);
        List<int[]> neighborColors = getNodeNeighborColors(neighbors);
        int[] newRgb = getNewColor(neighborColors);
        usedRgbs.add(newRgb);
        return newRgb;
    }

    /**
     * Generates a new RGB color for an edge-group based on its neighboring edges' colors.
     * @param edges A Set of Edges representing the neighboring edges.
     * @return The new RGB color for the underlying edge-group.
     */
    private int[] newEdgeColor(int iteration, Set<Edge> edges) {
        if(iteration > tempIteration){
            usedRgbs.clear();
            tempIteration++;
        }
        List<Edge> neighbors = new ArrayList<>();
        for (Edge e : edges) {
            List<Edge> edgeNeighbors = new ArrayList<>();
            for (Edge edge : data.getEdges()) {
                if (edge.source == e.source && edge.target != e.target) {
                    edgeNeighbors.add(edge);
                }
                if (edge.target == e.target && edge.source != e.source) {
                    edgeNeighbors.add(edge);
                }
            }
            neighbors.addAll(edgeNeighbors);
        }
        neighbors.removeAll(edges);
        List<int[]> neighborColors = getEdgeNeighborColors(neighbors);
        int[] newRgb = getNewColor(neighborColors);
        usedRgbs.add(newRgb);
        return newRgb;
    }

    /**
     * Generates a new RGB color with maximum average Euclidean distance to the list of neighboring colors.
     * @param neighborColors A List of RGB color values of neighboring nodes or edges.
     * @return The new RGB color for the underlying node-group or edge-group.
     */
    private int[] getNewColor(List<int[]> neighborColors) {
        int[] newRgb;
        int rgbSum = 0;
        int attempts = 0;
        do {
            newRgb = new int[]{0, 0, 0};
            if (neighborColors.isEmpty()) {
                Random random = new Random();
                newRgb[0] = random.nextInt(256);
                newRgb[1] = random.nextInt(256);
                newRgb[2] = random.nextInt(256);
            }
            else {
                int[] averageColor = new int[]{0, 0, 0};
                for (int[] color : neighborColors) {
                    averageColor[0] += color[0];
                    averageColor[1] += color[1];
                    averageColor[2] += color[2];
                }
                averageColor[0] = averageColor[0] / neighborColors.size();
                averageColor[1] = averageColor[1] / neighborColors.size();
                averageColor[2] = averageColor[2] / neighborColors.size();
                if (averageColor[0] <= 127) {
                    newRgb[0] = 255;
                }
                if (averageColor[1] <= 127) {
                    newRgb[1] = 255;
                }
                if (averageColor[2] <= 127) {
                    newRgb[2] = 255;
                }
                if (attempts > 0) {
                    if (newRgb[0] == 0 && newRgb[1] == 0 && newRgb[2] == 0) {
                        int value = 0;
                        int index = 0;
                        for (int i = 0; i < 3; i++) {
                            if (averageColor[i] > value) {
                                value = averageColor[i];
                                index = i;
                            }
                        }
                        for (int i = 0; i < 3; i++) {
                            if (i != index) {
                                Random random = new Random();
                                newRgb[i] = random.nextInt(5, 75);
                            }
                        }
                    }
                    if (newRgb[0] == 255 && newRgb[1] == 255 && newRgb[2] == 255) {
                        int value = 255;
                        int index = 0;
                        for (int i = 0; i < 3; i++) {
                            if (averageColor[i] < value) {
                                value = averageColor[i];
                                index = i;
                            }
                        }
                        for (int i = 0; i < 3; i++) {
                            if (i != index) {
                                Random random = new Random();
                                newRgb[i] = random.nextInt(180, 250);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < 3; i++) {
                            if (newRgb[i] == 0) {
                                Random random = new Random();
                                newRgb[i] = random.nextInt(5, 75);
                            }
                            if (newRgb[i] == 255) {
                                Random random = new Random();
                                newRgb[i] = random.nextInt(180, 250);
                            }
                        }
                    }
                }
                attempts++;
            }
            rgbSum = newRgb[0] + newRgb[1] + newRgb[2];
        } while ((colorIsUsed(newRgb, usedRgbs) || rgbSum == 0 || rgbSum == 765 || rgbSum < 100 || rgbSum > 700 || (newRgb[0] == 220 && newRgb[1] == 220 && newRgb[2] == 220)) && attempts < 50);
        return newRgb;
    }

    /**
     * Updates the labels of nodes in the graph.
     * @param labels a map containing the updated labels for each node.
     */
    private void updateLabels(HashMap<Node, String> labels){
        for (Node node : labels.keySet()) {
            node.setLabel(labels.get(node));
        }
    }

    /**
     * Updates the numerical values and colors of nodes in the graph.
     * @param newNumValues a map containing the updated numerical values for each node.
     */
    private void updateNodeNumValues(int iteration, HashMap<Node, Double> newNumValues){
        int[] color = newNodeColor(iteration, newNumValues.keySet());
        for (Node node : newNumValues.keySet()) {
            node.numValue.set(iteration, newNumValues.get(node));
            node.rgbValue.set(iteration, color);
        }
    }

    /**
     * Updates the string values and colors of nodes in the graph.
     * @param newStringValues a map containing the updated string values for each node.
     */
    private void updateNodeStringValues(int iteration, HashMap<Node, String> newStringValues){
        int[] color = newNodeColor(iteration, newStringValues.keySet());
        for (Node node : newStringValues.keySet()) {
            node.stringValue.set(iteration, newStringValues.get(node));
            node.rgbValue.set(iteration, color);
        }
    }

    /**
     * Updates the numerical values and colors of edges in the graph.
     * @param newNumValues a map containing the updated numerical values for each edge.
     */
    private void updateEdgeNumValues(int iteration, HashMap<Edge, Double> newNumValues){
        int[] color = newEdgeColor(iteration, newNumValues.keySet());
        for (Edge edge : newNumValues.keySet()) {
            edge.numValue.set(iteration, newNumValues.get(edge));
            edge.rgbValue.set(iteration, color);
        }
    }

    /**
     * Updates the string values and colors of edges in the graph.
     * @param newStringValues a map containing the updated string values for each edge.
     */
    private void updateEdgeStringValues(int iteration, HashMap<Edge, String> newStringValues){
        int[] color = newEdgeColor(iteration, newStringValues.keySet());
        for (Edge edge : newStringValues.keySet()) {
            edge.stringValue.set(iteration, newStringValues.get(edge));
            edge.rgbValue.set(iteration, color);
        }
    }

    /**
     * Converts the graph data to JSON format.
     * @param data the graph data to be converted.
     * @return the JSON representation of the graph data.
     * @throws JsonProcessingException if an error occurs during JSON processing.
     */
    private static String convert(JsonData data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);
        return json;
    }

    /**
     * Resets the graph data and visualization option.
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
                if (node.getRgbValue(iteration)[0] + node.getRgbValue(iteration)[1] + node.getRgbValue(iteration)[2] != 765 && node.getRgbValue(iteration)[0] + node.getRgbValue(iteration)[1] + node.getRgbValue(iteration)[2] != 660) {
                    node.setNumValue(node.numValue.get(iteration));
                    node.setStringValue(node.stringValue.get(iteration));
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
                if (edge.getRgbValue(iteration)[0] + edge.getRgbValue(iteration)[1] + edge.getRgbValue(iteration)[2] != 0 && edge.getRgbValue(iteration)[0] + edge.getRgbValue(iteration)[1] + edge.getRgbValue(iteration)[2] != 660) {
                    edge.setNumValue(edge.numValue.get(iteration));
                    edge.setStringValue(edge.stringValue.get(iteration));
                    edge.setRgbValue(gray);
                } else {
                    edge.setNumValue(Double.MIN_VALUE);
                    edge.setStringValue("");
                    edge.setRgbValue(black);
                }
            }
        }
        iteration++;
    }

/* Methods that can be used in the OCD algorithms */

    /**
     * Adds the graph component from the OCD algorithm input graph to the visualization data.
     * This method takes a CustomGraph object representing a graph component and adds its nodes and edges to the visualization data.
     * @param graph The CustomGraph object representing the graph component to be added.
     */
    public void addComponent(CustomGraph graph) {
        try {
            graphComponent = graph;
            List<Node> nodes = new ArrayList<>();
            for (int i = 0; i < graph.getNodeCount(); i++) {
                Node node = new Node(getRealNode(i));
                node.setDegree(graph.getNode(i).getDegree() / 2);
                nodes.add(node);
            }

            List<Edge> edges = new ArrayList<>();
            ArrayList<ArrayList<Integer>> edges_temp = new ArrayList<>();
            for (int i = 0; i < graph.getEdgeCount(); i++) {
                int source;
                int target;
                if (graph.getEdge(i).getSourceNode().getId().charAt(0) == '0') {
                    source = Integer.valueOf(graph.getEdge(i).getSourceNode().getId());
                    target = Integer.valueOf(graph.getEdge(i).getTargetNode().getId());
                } else {
                    source = Integer.valueOf(graph.getEdge(i).getSourceNode().getId().substring(1 + component / 10));
                    target = Integer.valueOf(graph.getEdge(i).getTargetNode().getId().substring(1 + component / 10));
                }
                ArrayList<Integer> edge1 = new ArrayList<>();
                ArrayList<Integer> edge2 = new ArrayList<>();
                edge1.addAll(Arrays.asList(source, target));
                edge2.addAll(Arrays.asList(target, source));
                if (!edges_temp.contains(edge1) && !edges_temp.contains(edge2)) {
                    edges_temp.add(edge1);
                    edges.add(new Edge(source, target));
                }
            }

            data.addNodes(nodes);
            data.addEdges(edges);

            // Sort the nodes by their IDs.
            Collections.sort(data.getNodes(), new Comparator<Node>() {
                @Override
                public int compare(Node node1, Node node2) {
                    return Integer.compare(node1.id, node2.id);
                }
            });
        } catch (NumberFormatException e) {
            System.out.println("Descriptive Visualization not possible for provided network!");
            DescriptiveVisualization.setVisualize(false);
        }
    }

    /**
     * Retrieves the real node ID based on the provided index.
     * @param index The index of the node.
     * @return The real node ID.
     */
    public int getRealNode(int index){
        int id = index;
        try {
            if (graphComponent.getNode(index).getId().charAt(0) == '0') {
                id = Integer.valueOf(graphComponent.getNode(index).getId());
            } else {
                id = Integer.valueOf(graphComponent.getNode(index).getId().substring(1 + component / 10));
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
     * @return The real edge representation as an ArrayList of Integers.
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
        descriptionPath = "rest_ocd_services/src/main/java/i5/las2peer/services/ocd/graphs/descriptions/" + descriptionPath;
        if(component == 0) {
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
            component++;
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
            updateNodeNumValues(iteration, nodeNumValues);
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
            component++;
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
            updateNodeStringValues(iteration, nodeStringValues);
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
            component++;
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
            updateEdgeNumValues(iteration, edgeNumValues);
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
            component++;
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
            updateEdgeStringValues(iteration, edgeStringValues);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets the final cover for the last iteration.
     * @param iteration The final iteration for which the cover is set.
     * @param cover The Cover object containing membership information.
     */
    public void setCover(int iteration, Cover cover) {
        Matrix membershipMatrix = cover.getMemberships();
        try {
            List<int[]> communityColors = new ArrayList<>();
            for (int c = 0; c < membershipMatrix.columns(); c++){
                communityColors.add(finalColor());
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

        // Save Json file to ArangoDB.
        String collectionName = "descriptiveVisualization";
        try {
            // Create collection if it doesn't exist
            if (!arangoDB.db("ocdDB").collection(collectionName).exists()) {
                arangoDB.db("ocdDB").createCollection(collectionName);
            }
            // Insert Json document.
            DocumentCreateEntity entity = arangoDB.db("ocdDB").collection(collectionName).insertDocument(json);
        } catch (ArangoDBException e) {
            e.printStackTrace();
        } finally {
            arangoDB.shutdown();
        }
    }

}