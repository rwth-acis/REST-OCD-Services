package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;

import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.Pair;
import org.apache.commons.collections4.map.MultiKeyMap;
import y.base.*;

import java.util.*;
import java.util.stream.Collectors;

public class LabelPropagationAlgorithmWithNeighborNodeInfluence implements
        OcdAlgorithm {

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
        if (parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public HashMap<String, String> getParameters() {
        return new HashMap<String, String>();
    }

    private int alpha = 3;
    HashMap<Node, Double> niToNodes = new HashMap<Node, Double>();
    MultiKeyMap simToNodesPair = new MultiKeyMap();
    MultiKeyMap nniToNodesPair = new MultiKeyMap();

    private Set<Edge> getEdges(CustomGraph graph){
        Set<Edge> edges = new HashSet<Edge>();
        EdgeCursor allEdges = graph.edges();
        Edge currEdge;
        while (allEdges.ok()) {
            currEdge = allEdges.edge();
            edges.add(currEdge);
            graph.edges().next();
        }
        return edges;
    }

    private Set<Node> getNodes(CustomGraph graph){
        Set<Node> nodes = new HashSet<Node>();
        NodeCursor allNodes = graph.nodes();
        Node currNode;
        while (allNodes.ok()) {
            currNode = allNodes.node();
            nodes.add(currNode);
            graph.nodes().next();
        }
        return nodes;
    }

    private Set<Node> getNeighbors(Node node){
        Set<Node> neighbors = new HashSet<Node>();
        NodeCursor nodeNeighbors = node.neighbors();
        Node nodeNeighbor;
        while (nodeNeighbors.ok()) {
            nodeNeighbor = nodeNeighbors.node();
            neighbors.add(nodeNeighbor)
            nodeNeighbors.next();
        }
        return neighbors;
    }

    private HashMap<Node, Double> calculateNI(CustomGraph graph) {

        Set<Node> nodes = getNodes(graph);

        double minNI = 0;
        double maxNI = 2 * graph.N();

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
            double ni = curr.degree() + triangleCount;
            niToNodes.put(curr, ni);
            minNI = Math.min(minNI, ni);
            maxNI = Math.max(maxNI, ni);
        }
        for (Node curr : nodes) {
            double newNI = 0.5 + 0.5 * (niToNodes.get(curr) - minNI) / (maxNI - minNI);
            niToNodes.put(curr, newNI);
        }
        return niToNodes;
    }

    private MultiKeyMap calculateSim(CustomGraph graph) {
        MultiKeyMap sToNodesPair = new MultiKeyMap();
       //HashMap< Pair<Node, Node>, Double> sToNodesPair = new HashMap< Node, HashMap<Node, Double>>();
        Set<Edge> edges = getEdges(graph);
        for (Edge edge : edges) {
            Node u = edge.source();
            Node v = edge.target();
            Set<Node> neighborsFromU = getNeighbors(u);
            double s = 1;
            for (Node i : neighborsFromU) {
                if (graph.containsEdge(i, v)) {
                    s += 0.5;
                }
            }
            for (Node i : neighborsFromU) {
                Set<Node> neighborsFromI = getNeighbors(i);
                for (Node j : neighborsFromI) {
                    if (graph.containsEdge(j, v) && (j != u)) {
                        s += 1 / 3;
                    }
                }
            }
            sToNodesPair.put(v, u, s);
            sToNodesPair.put(u, v, s);

        }

            for (Edge edge : edges){
                Node u = edge.source();
                Node v = edge.target();
                Set<Node> neighborsFromU = getNeighbors(u);
                Set<Node> neighborsFromV = getNeighbors(v);
                Double sumForU = 0.0;
                Double sumForV = 0.0;
                for (Node node : neighborsFromU){
                    sumForU += Double.valueOf(sToNodesPair.get(u,node).toString());
                }
                for (Node node : neighborsFromV){
                    sumForV += Double.valueOf(sToNodesPair.get(v,node).toString());
                }
                Double sim = Double.valueOf(sToNodesPair.get(v,u).toString()) / Math.sqrt(sumForU * sumForV);
                simToNodesPair.put(u, v, sim);
                simToNodesPair.put(v, u, sim);
            }

        return simToNodesPair;
    }

    protected MultiKeyMap calculateNNI(CustomGraph graph) {
        Set<Node> nodes = getNodes(graph);
        for (Node u : nodes) {
            ArrayList<Double> simList = new ArrayList<Double>();
            for ( Node i : nodes){
                simList.add(Double.valueOf(simToNodesPair.get(u, i).toString()));
            }
            double maxSim = Collections.max(simList);
            for (Node v : getNeighbors(u)){
                double nni = Math.sqrt(niToNodes.get(u) * (Double.valueOf(simToNodesPair.get(v, u).toString()) / maxSim));
                nniToNodesPair.put(u,v, nni);
            }
        }
        return nniToNodesPair;
    }
    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
        calculateNI(graph);
        calculateSim(graph);
        calculateNNI(graph);
        Pair<Node, Double> label = new Pair<>();
        Set<Node> nodes = getNodes(graph);
        HashMap<Node, Double> vQueue = niToNodes.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        for (Node node : nodes){

        }
        return null;
    }
}







