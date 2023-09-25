package i5.las2peer.services.ocd.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.graph.Edge;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.algorithms.utils.MLCPMCommunity;
import i5.las2peer.services.ocd.algorithms.utils.MultiplexClique;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Pair;

public class CliquePercolationMethod_MultiplexAlgorithm implements OcdAlgorithm {

    private int k = 3;
    private int m = 1;

    private int indexCount = 0;

    /*
     * PARAMETER NAMES
     */

    protected static final String MIN_CLIQUE_SIZE = "k";
    protected static final String MIN_LAYERS = "m";

    /**
     * Default constructor that returns algorithm instance with default parameter
     * values
     */
    public CliquePercolationMethod_MultiplexAlgorithm() {

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

        if (parameters.containsKey(MIN_LAYERS)) {
            m = Integer.parseInt(parameters.get(MIN_LAYERS));
            if (k < 1) {
                throw new IllegalArgumentException("Minimum layers must be at least 1!");
            }
            parameters.remove(MIN_LAYERS);
        }

        if (parameters.size() > 0) {
            throw new IllegalArgumentException("Too many input parameters!");
        }

    }

    @Override
    public Map<String, String> getParameters() {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MIN_CLIQUE_SIZE, Integer.toString(k));
        parameters.put(MIN_LAYERS, Integer.toString(m));
        return parameters;

    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        compatibilities.add(GraphType.MULTIPLE_EDGES);
        return compatibilities;
    }

    @Override
    public CoverCreationType getAlgorithmType() {

        return CoverCreationType.CLIQUE_PERCOLATION_METHOD_MULTIPLEX_ALGORITHM;

    }

    public Cover detectOverlappingCommunities(CustomGraph customGraph) {
        Map<String, Matrix> networks = createAdjacencyMatrix(customGraph);
        Set<String> allLayers = networks.keySet();
        Set<MultiplexClique> cliques = findMaximalCliques(k, m, networks, customGraph);
        Map<MultiplexClique, Set<MultiplexClique>> adjacencyGraph = buildMaxAdjacencyGraph(cliques, k, m);
        Set<MLCPMCommunity> communities = findMaxCommunities(adjacencyGraph, m);
        Map<String, Matrix> memberships = convertIntoMembershipMatrix(communities,
                customGraph, allLayers);
        return new Cover(customGraph, memberships);

    }
    public Set<MultiplexClique> findMaximalCliques(int k, int m, Map<String, Matrix> networks,
            CustomGraph customGraph) {
        Set<MultiplexClique> cliques = new HashSet<>();
        MultiplexClique a = new MultiplexClique();
        a.setLayers(new HashSet<>(networks.keySet()));

        Set<Pair<Integer, Set<String>>> b = new HashSet<>();
        for (int i = 0; i < customGraph.getNodeCount(); i++) {
            b.add(new Pair<>(i, networks.keySet()));
        }
        Set<Pair<Integer, Set<String>>> c = new HashSet<>();
        findCliques(a, b, c, k, m, cliques, customGraph, networks);

        return cliques;
    }

    public void findCliques(MultiplexClique a, Set<Pair<Integer, Set<String>>> b, Set<Pair<Integer, Set<String>>> c,
            int k, int m, Set<MultiplexClique> cliques,
            CustomGraph customGraph, Map<String, Matrix> networks) {

        if (a.getNodes().size() >= k) {

            if (a.getLayers().size() >= m) {

                boolean can_extend_on_B = false;
                boolean can_extend_on_C = false;

                /*
                 * if (b.isEmpty()) {
                 * can_extend_on_B = false;
                 * }
                 * if (c.isEmpty()) {
                 * can_extend_on_C = false;
                 * }
                 */

                int maxLayers = a.getLayers().size();

                for (Pair<Integer, Set<String>> cElement : c) {

                    Set<String> commonLayers = new HashSet<>(a.getLayers());
                    commonLayers.retainAll(cElement.getSecond());
                    int numberOfCommonLayers = commonLayers.size();

                    if (numberOfCommonLayers == maxLayers) {
                        can_extend_on_C = true;
                    }
                }

                for (Pair<Integer, Set<String>> bElement : b) {

                    Set<String> commonLayers = new HashSet<>(a.getLayers());
                    commonLayers.retainAll(bElement.getSecond());
                    int numberOfCommonLayers = commonLayers.size();

                    if (numberOfCommonLayers == maxLayers) {
                        can_extend_on_B = true;
                    }
                }
                if (!can_extend_on_B && !can_extend_on_C) {
                    a.setIndex(indexCount);
                    indexCount++;
                    cliques.add(a);
                }

            }

        }
        for (Pair<Integer, Set<String>> bElement : new ArrayList<>(b)) {

            Set<String> newLayers = new HashSet<>(a.getLayers());
            newLayers.retainAll(bElement.getSecond());

            Set<Integer> newNodes = new HashSet<>(a.getNodes());
            newNodes.add(bElement.getFirst());

            b.remove(bElement);

            MultiplexClique a_ext = new MultiplexClique(newNodes, newLayers, 0);
            Set<Pair<Integer, Set<String>>> b_ext = new HashSet<>();
            for (Pair<Integer, Set<String>> currentElement : b) {

                Set<String> commonLayers = new HashSet<>(bElement.getSecond());
                commonLayers.retainAll(neighboringLayers(networks, bElement.getFirst(), currentElement.getFirst()));
                commonLayers.retainAll(currentElement.getSecond());

                if (commonLayers.size() >= m) {

                    b_ext.add(new Pair<>(currentElement.getFirst(), new HashSet<>(commonLayers)));

                }
            }
            Set<Pair<Integer, Set<String>>> c_ext = new HashSet<>();
            for (Pair<Integer, Set<String>> currentElement : c) {

                Set<String> commonLayers = new HashSet<>(bElement.getSecond());
                commonLayers.retainAll(neighboringLayers(networks, bElement.getFirst(), currentElement.getFirst()));
                commonLayers.retainAll(currentElement.getSecond());

                if (commonLayers.size() >= m) {

                    c_ext.add(new Pair<>(currentElement.getFirst(), new HashSet<>(commonLayers)));

                }
            }
            findCliques(a_ext, b_ext, c_ext, k, m, cliques, customGraph, networks);
            c.add(bElement);
        }

    }

    public Map<MultiplexClique, Set<MultiplexClique>> buildMaxAdjacencyGraph(Set<MultiplexClique> cliques, int k,
            int m) {

        Map<MultiplexClique, Set<MultiplexClique>> adjacencyGraph = new HashMap<>();

        for (MultiplexClique c1 : cliques) {
            adjacencyGraph.put(c1, new HashSet<>());
        }

        for (MultiplexClique c1 : cliques) {

            for (MultiplexClique c2 : cliques) {
                if (!(c1 == c2) && c1.getIndex() > c2.getIndex()) {

                    Set<String> commonLayers = new HashSet<>(c1.getLayers());
                    commonLayers.retainAll(c2.getLayers());
                    int numberOfCommonLayers = commonLayers.size();

                    Set<Integer> commonNodes = new HashSet<>(c1.getNodes());
                    commonNodes.retainAll(c2.getNodes());
                    int numberOfCommonNodes = commonNodes.size();

                    if (numberOfCommonLayers >= m && numberOfCommonNodes >= k - 1) {
                        adjacencyGraph.get(c1).add(c2);
                        adjacencyGraph.get(c2).add(c1);
                    }

                }
            }
        }
        return adjacencyGraph;
    }

    public Set<MLCPMCommunity> findMaxCommunities(
            Map<MultiplexClique, Set<MultiplexClique>> adjacency, int m) {
        Set<MLCPMCommunity> result = new HashSet<>();

        Set<MultiplexClique> alreadySeen = new HashSet<>();

        for (Map.Entry<MultiplexClique, Set<MultiplexClique>> cliquePair : adjacency.entrySet()) {
            MLCPMCommunity A = new MLCPMCommunity();
            A.addClique(cliquePair.getKey());

            for (String layer : cliquePair.getKey().getLayers()) {
                A.addLayer(layer);
            }

            Set<MultiplexClique> candidates = new HashSet<>(cliquePair.getValue());
            Set<Set<String>> empty = new HashSet<>();
            findMaxCommunities2(adjacency, A, candidates, alreadySeen, empty, m, result);
            alreadySeen.add(cliquePair.getKey());
        }

        return result;
    }

    private void findMaxCommunities2(
            Map<MultiplexClique, Set<MultiplexClique>> adjacency,
            MLCPMCommunity A,
            Set<MultiplexClique> candidates,
            Set<MultiplexClique> processedCliques,
            Set<Set<String>> processedLayerCombinations,
            int m,
            Set<MLCPMCommunity> result) {

        // EXPAND
        while (!candidates.isEmpty()) {
            MultiplexClique c = candidates.iterator().next();
            candidates.remove(c);

            Set<String> i = A.getLayers().stream()
                    .filter(c.getLayers()::contains)
                    .collect(Collectors.toSet());

            if (i.size() == A.getLayers().size()) {
                if (processedCliques.contains(c)) {
                    return;
                }

                A.addClique(c);

                for (MultiplexClique j : adjacency.get(c)) {
                    if (A.getCliques().contains(j)) {
                        continue;
                    } else {
                        candidates.add(j);
                    }
                }
            } else if (i.size() >= m) {
                MLCPMCommunity comm = new MLCPMCommunity();
                comm.setCliques(new HashSet<>(A.getCliques()));
                comm.setLayers(new HashSet<>(i));
                if (setContains(processedLayerCombinations, comm.getLayers())) {
                    continue;
                }

                Set<MultiplexClique> newCandidates = new HashSet<>(candidates);
                newCandidates.add(c);
                findMaxCommunities2(adjacency, comm, newCandidates, processedCliques, processedLayerCombinations, m,
                        result);
                processedLayerCombinations.add(comm.getLayers());
            }
        }

        result.add(A);

    }

    /**
     * This method creates a Mapping from layerID to adjacency matrix.
     * @param graph Graph based on which the adjacency matrix should be built
     * @return Adjacency matrix based on the input graph
     */
    public Map<String, Matrix> createAdjacencyMatrix(CustomGraph graph) {

        Iterator<Edge> edges = graph.edges().iterator();
        Map<String, Matrix> matrices = new HashMap<>();

        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (!matrices.containsKey(graph.getEdgeLayerId(edge))) {
                Matrix A = new Basic2DMatrix(graph.getNodeCount(), graph.getNodeCount());
                A = A.blank();
                matrices.put(graph.getEdgeLayerId(edge), A);
            }
            matrices.get(graph.getEdgeLayerId(edge)).set(edge.getSourceNode().getIndex(),
                    edge.getTargetNode().getIndex(), 1);
        }

        return matrices;

    }

    public Set<String> neighboringLayers(Map<String, Matrix> networks, int u, int v) {

        Set<String> neighboringLayers = new HashSet<>();

        for (Map.Entry<String, Matrix> network : networks.entrySet()) {

            if (network.getValue().get(u, v) == 1) {

                neighboringLayers.add(network.getKey());

            }

        }
        return neighboringLayers;
    }

    public boolean setContains(Set<Set<String>> layersSet, Set<String> layers) {

        for (Set<String> layersIt : layersSet) {
            if (layers.equals(layersIt)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Matrix> convertIntoMembershipMatrix(Set<MLCPMCommunity> communities, CustomGraph graph,
            Set<String> allLayers) {

        int numberOfCommunities = communities.size();
        int numberOfNodes = graph.getNodeCount();
        int communityCount = 0;

        Map<String, Matrix> memberships = new HashMap<>();

        // Initialize memberships map
        for (String layer : allLayers) {
            Matrix a = new Basic2DMatrix(numberOfNodes, numberOfCommunities);
            a.blank();
            memberships.put(layer, a);
        }

        for (MLCPMCommunity community : communities) {

            for (String layer : community.getLayers()) {

                for (MultiplexClique multiplexClique : community.getCliques()) {

                    for (Integer integer : multiplexClique.getNodes()) {

                        memberships.get(layer).set(integer, communityCount, 1);
                    }
                }

            }
            communityCount++;

        }
        return memberships;

    }

}