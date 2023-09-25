package i5.las2peer.services.ocd.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.sparql.function.library.max;
import org.graphstream.graph.Node;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;
import i5.las2peer.services.ocd.algorithms.utils.Clique;

public class CliquePercolationMethodAlgorithm implements OcdAlgorithm {

    private int k = 3;

    /*
     * PARAMETER NAMES
     */

    protected static final String MIN_CLIQUE_SIZE = "k";

    /**
     * Default constructor that returns algorithm instance with default parameter
     * values
     */
    public CliquePercolationMethodAlgorithm() {

    }

    @Override
    public void setParameters(Map<String, String> parameters) {

        if (parameters.containsKey(MIN_CLIQUE_SIZE)) {
            k = Integer.parseInt(parameters.get(MIN_CLIQUE_SIZE));
            if (k < 1) {
                throw new IllegalArgumentException("Minimum clique size must be at least 1!");
            }
            parameters.remove(MIN_CLIQUE_SIZE);
        }

        if (parameters.size() > 0) {
            throw new IllegalArgumentException("Too many input parameters!");
        }

    }

    @Override
    public Map<String, String> getParameters() {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MIN_CLIQUE_SIZE, Integer.toString(k));
        return parameters;

    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        return compatibilities;
    }

    @Override
    public CoverCreationType getAlgorithmType() {

        return CoverCreationType.CLIQUE_PERCOLATION_METHOD_MULTIPLEX_ALGORITHM;

    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException {
        MaximalCliqueSearch maximalCliqueSearch = new MaximalCliqueSearch();
        long start = System.currentTimeMillis();
        HashMap<Integer, HashSet<Node>> allMaxCliques = maximalCliqueSearch.cliques(graph);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        HashSet<Clique> maxCliques = createCliques(allMaxCliques);
        HashMap<Integer, HashSet<Clique>> nodesToCliques = nodesToCliques(maxCliques);
        int communityCount = computeCommunities(maxCliques, nodesToCliques);
        int nodeCount = graph.getNodeCount();
        Matrix membershipMatrix = createMembershipMatrix(maxCliques, communityCount, nodeCount);
        return new Cover(graph, membershipMatrix);

    }

    private HashSet<Clique> createCliques(HashMap<Integer, HashSet<Node>> allMaxCliques) {

        HashSet<Clique> maxCliques = new HashSet<>();

        for (Map.Entry<Integer, HashSet<Node>> entry : allMaxCliques.entrySet()) {
            if (entry.getValue().size() >= k) {
                HashSet<Integer> nodes = new HashSet<>();
                for (Node node : entry.getValue()) {
                    nodes.add(node.getIndex());
                }
                Clique clique = new Clique(nodes);
                maxCliques.add(clique);
            }
        }
        return maxCliques;
    }

    private HashMap<Integer, HashSet<Clique>> nodesToCliques(HashSet<Clique> maxCliques) {
        HashMap<Integer, HashSet<Clique>> nodesTocliques = new HashMap<>();
        for (Clique clique : maxCliques) {
            for (Integer node : clique.getNodes()) {
                if (!nodesTocliques.containsKey(node)) {
                    nodesTocliques.put(node, new HashSet<>());
                }
                nodesTocliques.get(node).add(clique);
            }
        }
        return nodesTocliques;
    }

    private int computeCommunities(HashSet<Clique> maxCliques, HashMap<Integer, HashSet<Clique>> nodesToClique) {

        int currentCommunity = 0;

        for (Clique clique : maxCliques) {

            if (!clique.isVisited()) {
                HashSet<Clique> frontier = new HashSet<>();
                clique.setVisited(true);
                frontier.add(clique);

                while (!frontier.isEmpty()) {
                    Clique currentClique = frontier.iterator().next();
                    currentClique.setCommunity(currentCommunity);
                    frontier.remove(currentClique);
                    HashSet<Clique> neighbors = getUnvisitedAdjacentCliques(currentClique, nodesToClique);
                    for (Clique neighbor : neighbors) {
                        Set<Integer> neighborNodes = new HashSet<>(neighbor.getNodes());
                        neighborNodes.retainAll(currentClique.getNodes());
                        if (neighborNodes.size() >= k - 1) {
                            frontier.add(neighbor);
                            neighbor.setVisited(true);
                            for (Integer node : neighbor.getNodes()) {
                                nodesToClique.get(node).remove(neighbor);
                            }

                        }
                    }

                }
                currentCommunity++;
            }
        }
        return currentCommunity;
    }

    private HashSet<Clique> getUnvisitedAdjacentCliques(Clique clique,
            HashMap<Integer, HashSet<Clique>> nodesToClique) {

        HashSet<Clique> neighbors = new HashSet<>();
        for (Map.Entry<Integer, HashSet<Clique>> entry : nodesToClique.entrySet()) {
            if (clique.getNodes().contains(entry.getKey())) {
                for (Clique neighborClique : entry.getValue()) {
                    if (!neighborClique.isVisited()) {
                        neighbors.add(neighborClique);
                    }
                }
            }
        }
        return neighbors;
    }

    private Matrix createMembershipMatrix(HashSet<Clique> maxCliques, int communityCount, int nodeCount) {
        Matrix membershipMatrix = new CCSMatrix(nodeCount, communityCount);
        for (Clique clique : maxCliques) {
            for (Integer node : clique.getNodes()) {
                membershipMatrix.set(node, clique.getCommunity(), 1);
            }
        }
        return membershipMatrix;
    }
}
