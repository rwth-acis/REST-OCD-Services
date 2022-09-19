package i5.las2peer.services.ocd.algorithms;

import com.sun.jdi.IntegerValue;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;

import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import y.base.*;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


public class LabelPropagationAlgorithmWithNeighborNodeInfluence implements
        OcdAlgorithm {

    /**
     * Maximum iteration number T.
     */
    private static int t = 100;

    protected static final String MAX_ITERATION_NUMBER = "maximum iteration number";

    Map<Node, Double> niToNodes = new HashMap<>();
    MultiKeyMap<Integer, Double> simToNodesPair = new MultiKeyMap<>();
    MultiKeyMap<Integer, Double> nniToNodesPair = new MultiKeyMap<>();

    /**
     * Creates a instance of the algorithm.
     */
    public LabelPropagationAlgorithmWithNeighborNodeInfluence() {
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.LABEL_PROPAGATION_ALGORITHM_WITH_NEIGHBOR_NODE_INFLUENCE;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        return compatibilities;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters.containsKey(MAX_ITERATION_NUMBER)) {
            t = Integer.parseInt(parameters.get(MAX_ITERATION_NUMBER));
            if (t < 1) {
                throw new IllegalArgumentException();
            }
            parameters.remove(MAX_ITERATION_NUMBER);
        }
        if (parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MAX_ITERATION_NUMBER, Integer.toString(t));
        return parameters;
    }

    private Set<Edge> getEdges(CustomGraph graph) {
        return new HashSet<>(graph.getEdgeList());
    }

    private Set<Node> getNodes(CustomGraph graph) {
        return new HashSet<>(new NodeList(graph.nodes()));
    }

    private Set<Node> getNeighbors(Node node) {
        return new HashSet<>(new NodeList(node.neighbors()));
    }

    protected Map<Node, Double> calculateNI(CustomGraph graph) {

        Set<Node> nodes = getNodes(graph);

        double minNI = 2 * graph.N();
        double maxNI = 0;

        for (Node curr : nodes) {
            Set<Node> neighbors = getNeighbors(curr);
            int triangleCount = 0;
            for (Node i : neighbors) {
                for (Node j : neighbors) {
                    if ((i.index() < j.index()) && graph.containsEdge(i, j)) {
                        triangleCount += 1;
                    }
                }
            }

            double ni = (curr.degree()/2) + triangleCount;
            niToNodes.put(curr, ni);
            minNI = Math.min(minNI, ni);
            maxNI = Math.max(maxNI, ni);
            /**System.out.println("Node" + curr.index());
            System.out.println("Degree" + curr.degree());**/

        }

        for (Node curr : nodes) {
            double newNI = 0.5 + 0.5 * ((niToNodes.get(curr) - minNI) / (maxNI - minNI));
            niToNodes.put(curr, newNI);
        }

        return niToNodes;
    }

    protected MultiKeyMap<Integer, Double> calculateSim(CustomGraph graph) {
        MultiKeyMap<Integer, Double> sToNodesPair = new MultiKeyMap<>();
        Map<Integer, ArrayList<Double>> sToNode = new HashMap<>();
        Set<Edge> edges = getEdges(graph);
        for (Edge edge : edges) {
            Node u = edge.source();
            Node v = edge.target();
            if (!sToNode.containsKey(u.index())) {
                sToNode.put(u.index(), new ArrayList<>());
            }
            Set<Node> neighborsFromU = getNeighbors(u);
            double s = 1.0;
            for (Node i : neighborsFromU) {
                if (graph.containsEdge(i, v)) {
                    s += 0.5;
                }
            }
            for (Node i : neighborsFromU) {
                if (i != v) {
                    Set<Node> neighborsFromI = getNeighbors(i);
                    for (Node j : neighborsFromI) {
                        if (graph.containsEdge(j, v) && (j != u)) {
                            s += 1d / 3;
                        }
                    }
                }
            }
            sToNode.get(u.index()).add(s);
            sToNodesPair.put(u.index(), v.index(), s);

        }
        /**Set<Node> nodes = getNodes(graph);
         for (Node u : nodes){
         Set<Node> neighborsFromU = getNeighbors(u);
         for (Node v : neighborsFromU) {
         System.out.println(u.index());
         System.out.println(v.index());
         System.out.println(sToNode.get(u.index()));
         System.out.println(sToNodesPair.get(u.index(), v.index()));
         }
         }**/


        for (Edge edge : edges) {
            Node u = edge.source();
            Node v = edge.target();
            Double sumForU = 0.0;
            Double sumForV = 0.0;
            for (Double uValue : sToNode.get(u.index())) {
                sumForU += uValue;
            }
            for (Double vValue : sToNode.get(v.index())) {
                sumForV += vValue;
            }
            Double sim = sToNodesPair.get(v.index(), u.index()) / Math.sqrt(sumForU * sumForV);
            simToNodesPair.put(u.index(), v.index(), sim);

            /**System.out.println(u.index() + 1);
            System.out.println(v.index() + 1);
            System.out.println(simToNodesPair.get(u.index(), v.index()));**/
        }

        return simToNodesPair;
    }

    protected MultiKeyMap<Integer, Double> calculateNNI(CustomGraph graph) {
        Set<Node> nodes = getNodes(graph);
        for (Node u : nodes) {
            ArrayList<Double> simList = new ArrayList<>();
            Set<Node> neighborsFromU = getNeighbors(u);
            for (Node i : neighborsFromU) {
                simList.add(simToNodesPair.get(u.index(), i.index()));
            }

            double maxSim = Collections.max(simList);
            for (Node v : getNeighbors(u)) {
                double nni = Math.sqrt(niToNodes.get(v) * (simToNodesPair.get(v.index(), u.index()) / maxSim));
                nniToNodesPair.put(v.index(), u.index(), nni);
                /**System.out.println(v.index() + 1);
                System.out.println(u.index() + 1);
                System.out.println(niToNodes.get(v));
                System.out.println(simToNodesPair.get(v.index(), u.index()));
                System.out.println(nniToNodesPair.get(v.index(), u.index()));**/
            }
        }

         /**for (Node u : nodes){
         for (Node v : nodes){
         System.out.println(v.index() + 1);
         System.out.println(u.index() + 1);
         System.out.println(nniToNodesPair.get(v.index(), u.index()));
         }
         }**/
        return nniToNodesPair;
    }

    protected Map<Integer, Map<String, Double>> initNodes(CustomGraph
                                                                graph, Map<Integer, Map<String, Double>> initMap) {
        Set<Integer> nodes = graph.getNodeIds();
        for (Integer curr : nodes) {
            if (!initMap.containsKey(curr)) {
                //initMap.put(curr, Map.of("dominantC", Double.valueOf(curr), "dominantB", 1.0, "labelSetSize", 1.0));
                Map<String, Double> nodeMap = new HashMap<>();
                nodeMap.put("dominantC", Double.valueOf(curr));
                nodeMap.put("dominantB", 1.0);
                nodeMap.put("labelSetSize", 1.0);
                initMap.put(curr, nodeMap);
            }
        }
        return initMap;
    }

    static SortedSet<Map.Entry<Node, Double>> entriesSortedByValues(Map<Node, Double> map) {
        SortedSet<Map.Entry<Node, Double>> sortedEntries = new TreeSet<>(
                Comparator.comparingDouble((ToDoubleFunction<Map.Entry<Node, Double>>) Map.Entry::getValue).thenComparingInt(e -> e.getKey().index())
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    private Matrix formCommunities(Matrix communities) {
        Matrix finalCommunities = new Basic2DMatrix(communities.rows(), communities.columns());
        int curColumn=0;
        for(int i = 0; i < communities.columns(); i++){
            if(communities.getColumn(i).sum() > 0){
                finalCommunities.setColumn(curColumn++, communities.getColumn(i));
            }
        }
        finalCommunities = finalCommunities.slice(0,0,communities.rows(),curColumn);
        return finalCommunities;
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws
            OcdAlgorithmException, InterruptedException, OcdMetricException {
        Map<Integer, Map<String, Double>> dominantMap = new HashMap<>();
        Map<Integer, Map<Integer, Double>> communitiesMap = new HashMap<>();
        calculateNI(graph);
        calculateSim(graph);
        calculateNNI(graph);
        initNodes(graph, dominantMap);
        SortedSet<Map.Entry<Node, Double>> sortedNodes = entriesSortedByValues(niToNodes);
        System.out.println(sortedNodes);
        int temp = 0;
        while (temp < t) {
            boolean change = false;
            for (Map.Entry<Node, Double> entry : sortedNodes) {
                Node u = entry.getKey();
                Map<Integer, Double> bMap = new HashMap<>(); //<c, sum(b*NNI)>
                Set<Node> neighbors = getNeighbors(u);
                for (Node neighbor : neighbors) {
                    Integer c = dominantMap.get(neighbor.index()).get("dominantC").intValue();
                    Double b = dominantMap.get(neighbor.index()).get("dominantB");
                    Double bMapValue = 0.0;
                    if (bMap.containsKey(c)) {
                        bMapValue = bMap.get(c) + b * nniToNodesPair.get(neighbor.index(), u.index());

                    } else {
                        bMapValue = b * nniToNodesPair.get(neighbor.index(), u.index());
                    }
                    bMap.put(c, bMapValue);
                }
                double totalBNNI = bMap.values().stream().mapToDouble(value -> value).sum();
                double threshold = 1.0 / bMap.size();
                double totalB = 0.0;
                double maxB = 0.0;
                List<Integer> candidateDominant = new ArrayList<>();
                Integer labelSetSize = 0;
                Map<Integer, Double> bMapList = new HashMap<>();
                for (Integer key : bMap.keySet()) {
                    double newB = bMap.get(key) / totalBNNI;
                    if (newB >= threshold) {
                        labelSetSize += 1;
                        totalB += newB;
                        bMapList.put(key, newB);
                        if (newB > maxB) {
                            candidateDominant.clear();
                            maxB = newB;
                            candidateDominant.add(key);
                        } else if (newB == maxB) {
                            candidateDominant.add(key);
                        }
                    }
                }
                communitiesMap.put(u.index(), bMapList);
                if (!candidateDominant.contains(dominantMap.get(u.index()).get("dominantC").intValue())) {
                    Map<String, Double> tempMap = dominantMap.computeIfAbsent(u.index(), key -> new HashMap<>());
                    tempMap.put("dominantC", Double.valueOf(candidateDominant.get(0)));
                    dominantMap.put(u.index(), tempMap);
                    change = true;
                }

                if (!labelSetSize.equals(dominantMap.get(u.index()).get("labelSetSize").intValue())) {
                    Map<String, Double> tempMap = dominantMap.computeIfAbsent(u.index(), key -> new HashMap<>());
                    tempMap.put("labelSetSize", Double.valueOf(labelSetSize));
                    dominantMap.put(u.index(), tempMap);
                    change = true;
                }
                /**Map<Integer, Double> bMapList = new HashMap<>();

                 for (Integer key : bMap.keySet()) {
                 double newB = bMap.get(key) / totalBNNI;
                 if (newB > threshold) {
                 bMapList.put(key, newB);
                 }
                 }
                 communitiesMap.put(u.index(), bMapList);**/
            }

            if (!change) {
                break;
            }
            temp += 1;
        }

        Matrix communities = new Basic2DMatrix(graph.nodeCount(), graph.nodeCount());

        for (Map.Entry<Integer, Map<Integer, Double>> entryCurrNode : communitiesMap.entrySet()) {
            for (Map.Entry<Integer, Double> label : entryCurrNode.getValue().entrySet()) {
                communities.set(entryCurrNode.getKey(), label.getKey(), label.getValue());
            }
        }

        Matrix result = formCommunities(communities);
        return new Cover(graph, result);
    }
}







