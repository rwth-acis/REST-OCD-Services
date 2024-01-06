package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.apache.jena.base.Sys;
import org.graphstream.graph.Node;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.*;
import java.util.stream.Collectors;

public class BalancedMultiLabelPropagationAlgorithm  implements OcdAlgorithm {

    private float filter = 0.5f;
    private int maxLPAIterations = 50;

    protected static final String LPA_THRESHOLD_NAME = "lpaThreshold";
    protected static final String LPA_MAX_ITERATION_NAME = "lpaMaxIteration";


    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.PSO_LPA_ALGORITHM;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(LPA_THRESHOLD_NAME, Float.toString(filter));
        parameters.put(LPA_MAX_ITERATION_NAME, Integer.toString(maxLPAIterations));
        return parameters;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            switch (entry.getKey()){
                case LPA_THRESHOLD_NAME:
                    filter = Float.parseFloat(entry.getValue());
                    if (filter < 0 || filter > 1)
                        throw new IllegalArgumentException();
                    break;
                case LPA_MAX_ITERATION_NAME:
                    maxLPAIterations = Integer.parseInt(entry.getValue());
                    if (maxLPAIterations < 0)
                        throw new IllegalArgumentException();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        compatibilities.add(GraphType.WEIGHTED);
        return compatibilities;
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException {
        return runLabelPropagation(graph,roughCoresInit(graph));
    }

    /**
     * Runs the Rough Cores to initialize BMLPA
     * @param graph the graph BMLPA runs on
     * @return a List of cores for initialization
     */
    private List<List<Node>> roughCoresInit(CustomGraph graph){
        List<List<Node>> cores = new ArrayList<>();
        List<Node> vSet = graph.nodes().collect(Collectors.toCollection(ArrayList::new));
        HashMap<Node, Boolean> free = new HashMap<>();
        for (Node node : vSet) {
            free.put(node, true);
        }
        Collections.sort(vSet, Comparator.comparingInt(Node::getDegree).reversed());
        for (Node i : vSet){
            if (i.getDegree() >= 3 && free.get(i)){
                List<Node> commNei = i.neighborNodes().collect(Collectors.toCollection(ArrayList::new));
                Optional<Node> j = commNei.stream().filter(v -> free.get(v)).max(Comparator.comparingInt(Node::getDegree).reversed());
                if (j.isPresent()){
                    List<Node> core = new ArrayList<>();
                    core.add(i);
                    free.put(i,false);
                    core.add(j.get());
                    free.put(j.get(),false);
                    Set<Node> temp = new HashSet<>(commNei);
                    temp.addAll(j.get().neighborNodes().toList());
                    temp.remove(i);
                    temp.remove(j.get());
                    commNei = temp.stream().filter(v -> free.get(v)).collect(Collectors.toCollection(ArrayList::new));
                    Collections.sort(commNei, Comparator.comparingInt(Node::getDegree));
                    while (commNei.size() > 0) {
                         Node h = commNei.get(0);
                         core.add(h);
                         free.put(h,false);
                         commNei.retainAll(h.neighborNodes().toList());
                         commNei.remove(h);
                    }
                    if (core.size() >= 3){
                        cores.add(core);
                    }
                }
            }
        }
        System.out.println(cores);
        return cores;
    }

    /**
     * Runs the BMLPA part of the algorithm
     * @param graph the graph BMLPA runs on
     * @param initialization rough cores
     * @return the final cover of the BMLPA
     */
    private Cover runLabelPropagation(CustomGraph graph, List<List<Node>> initialization) throws InterruptedException {
        Map<String, Map<Integer,Float>> oldCover = new HashMap();
        Map<String, Map<Integer,Float>> newCover;
        for (Node n : graph.nodes().toList()){
            oldCover.put(n.getId(),new HashMap<>());
        }
        int community = 0;
        for (List<Node> core : initialization) {
            for (Node n : core) {
                oldCover.get(n.getId()).put(community, 1f);
            }
            community++;
        }
        int i = 0;
        Map<Integer, Integer> oldMin = new HashMap<>();
        boolean running = true;
        while (i < maxLPAIterations && running)
        {
            newCover = new HashMap();
            for (Map.Entry<String, Map<Integer,Float>> x : oldCover.entrySet())
                propagate_bbc(graph, x.getKey(), oldCover, newCover);
            Map<Integer, Integer> min;
            if (getId(oldCover) == getId(newCover))
                min = mc(count(oldCover), count(newCover));
            else
                min = count(newCover);
            if (!min.equals(oldMin)){
                oldCover = newCover;
                oldMin = min;
            }
            else {
                running = false;
            }
            i++;
        }
        System.out.println(i);
        Map<Integer, Set<String>> coms = new HashMap<>();
        Map<Integer, Set<Integer>> sub = new HashMap<>();
        for (Map.Entry<String, Map<Integer,Float>> x : oldCover.entrySet()) {
            Set<Integer> ids = getSubId(x.getValue());
            for (Integer c : ids){
                coms.computeIfAbsent(c,k -> new HashSet<>());
                coms.get(c).add(x.getKey());
                sub.computeIfAbsent(c, k -> new HashSet<>(ids));
                sub.get(c).retainAll(ids);
            }
        }

        for (Map.Entry<Integer, Set<Integer>> ci : sub.entrySet())
            if (ci.getValue().size() > 1)
                coms.remove(ci.getKey());

        Map<Integer, Set<String>> splitCommunities = new HashMap<>();
        Map<Integer, Integer> commSplits = new HashMap<>();
        int k = coms.size()+1;
        for (Map.Entry<Integer, Set<String>> cEntry : coms.entrySet()) {
            int id = cEntry.getKey();
            while (cEntry.getValue().size() > 0){
                Set<String> commNodes = new HashSet<>();
                List<String> neighborNodes = new LinkedList<>();
                neighborNodes.add(cEntry.getValue().stream().toList().get(0));
                commNodes.add(neighborNodes.get(0));
                String node;
                while (neighborNodes.size() > 0)
                {
                    node = neighborNodes.get(0);
                    for (Node n : graph.getNode(node).neighborNodes().toList()){
                        if (!commNodes.contains(n.getId()) && cEntry.getValue().contains(n.getId())) {
                            commNodes.add(n.getId());
                            neighborNodes.add(n.getId());
                        }
                    }
                    neighborNodes.remove(node);
                }
                cEntry.getValue().removeAll(commNodes);
                splitCommunities.put(id,commNodes);
                if (cEntry.getValue().size() > 0){
                    id = k;
                    k++;
                    commSplits.put(id,cEntry.getKey());
                }
            }
        }

        Map<Integer,Integer> remap = new HashMap<>();
        int index = 0;
        for (Integer id : splitCommunities.keySet()){
            remap.put(id,index);
            index++;
        }
        Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), splitCommunities.size());

        for (Map.Entry<Integer, Set<String>> cEntry : splitCommunities.entrySet()) {
            int id = cEntry.getKey();
            if (commSplits.containsKey(id))
                id = commSplits.get(id);
            for (String node : cEntry.getValue()){
                membershipMatrix.set(graph.getNode(node).getIndex(), remap.get(cEntry.getKey()), (double) oldCover.get(node).get(id));
            }
        }
        return new Cover(graph, membershipMatrix);
    }

    /**
     * Propagates a labels of neighbor nodes to a node
     * @param graph the graph BMLPA runs on
     * @param x id of current node
     * @param source old community distribution
     * @param dest new community distribution
     */
    private void propagate_bbc(CustomGraph graph, String x, Map<String, Map<Integer,Float>> source, Map<String, Map<Integer,Float>> dest){
        dest.put(x, new HashMap<>());
        Iterator<Node> nodes = graph.getNode(x).neighborNodes().iterator();
        while (nodes.hasNext()) {
            Node y = nodes.next();
            for (Map.Entry<Integer, Float> cb : source.get(y.getId()).entrySet()){
                dest.get(x).computeIfAbsent(cb.getKey(), k -> 0f);
                dest.get(x).put(cb.getKey(),dest.get(x).get(cb.getKey())+cb.getValue());
            }
        }
        float bMax = Collections.max(dest.get(x).values());
        Map<Integer, Float> temp = new HashMap<>();
        for (Map.Entry<Integer, Float> cb : dest.get(x).entrySet()){
            if (cb.getValue()/bMax >= filter)
                temp.put(cb.getKey(), cb.getValue());
        }
        normalize(temp);
        dest.put(x,temp);
    }

    /**
     * Normalized the community belonging coefficients
     * @param l community belonging coefficients of a node
     */
    private void normalize(Map<Integer,Float> l){
        float sum = 0;
        for (Map.Entry<Integer,Float> x : l.entrySet())
            sum += x.getValue();
        for (Map.Entry<Integer,Float> x : l.entrySet())
            l.put(x.getKey(),x.getValue()/sum);
    }

    /**
     * Gets a Set of all community ids in the community distribution
     * @param l current community distribution
     * @return a Set of all community ids
     */
    private Set<Integer> getId(Map<String, Map<Integer,Float>> l){
        Set<Integer> ids = new HashSet<>();
        for (Map.Entry<String, Map<Integer,Float>> x : l.entrySet())
            ids.addAll(getSubId(x.getValue()));
        return ids;
    }

    /**
     * Gets a Set of all community ids of a single node
     * @param l community belonging coefficients of a node
     * @return a Set of all community ids of the node
     */
    private Set<Integer> getSubId(Map<Integer,Float> x){
        Set<Integer> ids = new HashSet<>();
        for (Map.Entry<Integer,Float> cb : x.entrySet())
            ids.add(cb.getKey());
        return ids;
    }

    /**
     * Counts the numbers of nodes in each community
     * @param l current community distribution
     * @return Number of nodes in each community
     */
    private Map<Integer,Integer> count(Map<String, Map<Integer,Float>> l){
        Map<Integer,Integer> counts = new HashMap<>();
        for (Map.Entry<String, Map<Integer,Float>> x : l.entrySet()){
            for (Map.Entry<Integer,Float> cb : x.getValue().entrySet()) {
                counts.computeIfAbsent(cb.getKey(),k -> 0);
                counts.put(cb.getKey(), counts.get(cb.getKey()) + 1);
            }
        }
        return counts;
    }

    /**
     * Combines two Maps, each being the Number of nodes in each community
     * @param cs1 Number of nodes in each community in the old cover
     * @param cs2 Number of nodes in each community in the new cover
     * @return Combined Map of Min Numbers of nodes in each community in each cover
     */
    private Map<Integer,Integer> mc(Map<Integer,Integer> cs1, Map<Integer,Integer> cs2){
        Map<Integer, Integer> cs = new HashMap<>();
        for (Map.Entry<Integer, Integer> c1 : cs1.entrySet()){
            if (cs2.containsKey(c1.getKey())) {
                cs.put(c1.getKey(), Math.min(c1.getValue(), cs2.get(c1.getKey())));
                cs2.remove(c1.getKey());
            }
            else {
                cs.put(c1.getKey(), c1.getValue());
            }
        }
        for (Map.Entry<Integer, Integer> c2 : cs2.entrySet()){
            cs.put(c2.getKey(),c2.getValue());
        }
        return cs;
    }

}
