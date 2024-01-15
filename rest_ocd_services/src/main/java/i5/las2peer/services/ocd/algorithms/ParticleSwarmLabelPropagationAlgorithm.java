package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.*;
import java.util.stream.Collectors;

import i5.las2peer.services.ocd.metrics.OcdMetricException;
import io.reactivex.internal.operators.flowable.FlowableOnErrorReturn;
import jnr.ffi.annotations.In;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.base.Sys;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.web3j.abi.datatypes.Int;

public class ParticleSwarmLabelPropagationAlgorithm  implements OcdAlgorithm {

    private int populationSize = 20;
    private float initChance = 0.6f;
    private float inertiaFactor = 0.4f;
    private float learningFactor1 = 0.4f;
    private float learningFactor2 = 0.4f;
    private int maxPSOIterations = 40;
    private float filter = 0.6f;
    private int maxLPAIterations = 50;
    private static Random rng = new Random();

    protected static final String PSO_POPULATION_SIZE_NAME = "psoPopulationSize";
    protected static final String PSO_INERTIA_FACTOR_NAME = "psoInertiaFactor";
    protected static final String PSO_LEARNING_FACTOR_PERSONAL_NAME = "psoLearningFactorPersonal";
    protected static final String PSO_LEARNING_FACTOR_GLOBAL_NAME = "psoLearningFactorGlobal";
    protected static final String PSO_MAX_ITERATION_NAME = "psoMaxIteration";
    protected static final String PSO_INIT_SPREADING_CHANCE_NAME = "psoInitSpreadingChance";

    protected static final String LPA_THRESHOLD_NAME = "lpaThreshold";
    protected static final String LPA_MAX_ITERATION_NAME = "lpaMaxIteration";


    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.PSO_LPA_ALGORITHM;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(PSO_POPULATION_SIZE_NAME, Integer.toString(populationSize));
        parameters.put(PSO_INERTIA_FACTOR_NAME, Float.toString(inertiaFactor));
        parameters.put(PSO_LEARNING_FACTOR_PERSONAL_NAME, Float.toString(learningFactor1));
        parameters.put(PSO_LEARNING_FACTOR_GLOBAL_NAME, Float.toString(learningFactor2));
        parameters.put(PSO_MAX_ITERATION_NAME, Integer.toString(maxPSOIterations));
        parameters.put(PSO_INIT_SPREADING_CHANCE_NAME, Float.toString(initChance));
        parameters.put(LPA_THRESHOLD_NAME, Float.toString(filter));
        parameters.put(LPA_MAX_ITERATION_NAME, Integer.toString(maxLPAIterations));
        return parameters;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            switch (entry.getKey()){
                case PSO_POPULATION_SIZE_NAME:
                    populationSize = Integer.parseInt(entry.getValue());
                    if (populationSize < 0)
                        throw new IllegalArgumentException();
                    break;
                case PSO_INERTIA_FACTOR_NAME:
                    inertiaFactor = Float.parseFloat(entry.getValue());
                    if (inertiaFactor < 0 || inertiaFactor > 1)
                        throw new IllegalArgumentException();
                    break;
                case PSO_LEARNING_FACTOR_PERSONAL_NAME:
                    learningFactor1 = Float.parseFloat(entry.getValue());
                    if (learningFactor1 < 0)
                        throw new IllegalArgumentException();
                    break;
                case PSO_LEARNING_FACTOR_GLOBAL_NAME:
                    learningFactor2 = Float.parseFloat(entry.getValue());
                    if (learningFactor2 < 0)
                        throw new IllegalArgumentException();
                    break;
                case PSO_MAX_ITERATION_NAME:
                    maxPSOIterations = Integer.parseInt(entry.getValue());
                    if (maxPSOIterations < 0)
                        throw new IllegalArgumentException();
                    break;
                case PSO_INIT_SPREADING_CHANCE_NAME:
                    initChance = Float.parseFloat(entry.getValue());
                    if (initChance < 0 || initChance > 1)
                        throw new IllegalArgumentException();
                    break;
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
        return runLabelPropagation(graph,runParticleSwarmOptimization(graph));
    }

    /**
     * Runs the PSO part of the algorithm
     * @param graph the graph PSO_LPA runs on
     * @return a non overlapping community distribution
     */
    private HashMap<String, Integer> runParticleSwarmOptimization(CustomGraph graph)  throws InterruptedException {
        HashMap<String, Float>[] populationPosition = populationPositionInitialization(graph);
        HashMap<String, Float>[] populationVelocity = new HashMap[populationSize];
        HashMap<String, Float>[] populationExperiencePosition = new HashMap[populationSize];
        double[] populationExperienceFitness = new double[populationSize];
        HashMap<String, Float> optimalPosition;
        double optimalFitness = -1;
        int temp = 0;
        for (int i = 0; i < populationSize; i++){
            populationVelocity[i] = new HashMap<>();
            for (Node n : graph.nodes().toList())
                populationVelocity[i].put(n.getId(), 0f);
            populationExperiencePosition[i] = (HashMap<String, Float>)populationPosition[i].clone();
            populationExperienceFitness[i] = calculateFitness(graph,populationPosition[i]);
            if (optimalFitness < populationExperienceFitness[i]) {
                temp = i;
                optimalFitness = populationExperienceFitness[i];
            }
        }

        optimalPosition = (HashMap<String, Float>)populationPosition[temp].clone();

        int iteration = 0;
        while (maxPSOIterations > iteration) {
            updateVelocity(populationVelocity, populationPosition, populationExperiencePosition, optimalPosition);
            updatePosition(populationVelocity, populationPosition);
            temp = -1;
            for (int i = 0; i < populationSize; i++){
                double fitness =  calculateFitness(graph, populationPosition[i]);
                if (fitness > populationExperienceFitness[i]) {
                    populationExperienceFitness[i] = fitness;
                    populationExperiencePosition[i] = (HashMap<String, Float>)populationPosition[i].clone();
                }
                if (optimalFitness < populationExperienceFitness[i]) {
                    temp = i;
                    optimalFitness = populationExperienceFitness[i];
                }
            }
            if (temp >= 0)
                optimalPosition = (HashMap<String, Float>)populationPosition[temp].clone();
            iteration++;
        }
        HashMap<String, Integer> finalPosition = new HashMap<>();
        for (Map.Entry<String,Float> e:  optimalPosition.entrySet())
        {
            finalPosition.put(e.getKey(),Math.round(e.getValue()));
        }
        return finalPosition;
    }

    /**
     * Initializes the population
     * @param graph the graph PSO_LPA runs on
     * @return an array with all initialized particles
     */
    private HashMap<String, Float>[] populationPositionInitialization(CustomGraph graph) {
        HashMap<String, Float>[] populationPosition = new HashMap[populationSize];
        for (int l = 0; l < populationSize; l++) {
            int k = 0;
            populationPosition[l] = new HashMap<>();
            final int p = l;
            Iterator<Node> iterator = graph.nodes().iterator();
            while (iterator.hasNext()) {
                Node j = iterator.next();
                if (populationPosition[p].containsKey(j.getId()))
                    continue;
                HashMap<String, Integer> flag = new HashMap<>();
                List<Node> nodes = new LinkedList<Node>(j.neighborNodes().toList());
                populationPosition[p].put(j.getId(), (float)k);
                while (nodes.size() > 0) {
                    Node i = nodes.get(0);
                    nodes.remove(0);
                    if (!flag.containsKey(i.getId()) && !populationPosition[p].containsKey(i.getId())) {
                        flag.put(i.getId(), 1);
                        if (rng.nextFloat() > initChance) {
                            populationPosition[p].put(i.getId(), (float)k);
                            nodes.addAll(i.neighborNodes().toList());
                        }
                    }
                }
                k++;
            }
        }
        return populationPosition;
    }

    /**
     * Updates the velocity of all particles
     * @param velocity the current velocities
     * @param position the current positions
     * @param expPosition the best position of each particle
     * @param optimalPosition the best position of all particles
     */
    private void updateVelocity(HashMap<String, Float>[] velocity, HashMap<String, Float>[] position, HashMap<String, Float>[] expPosition, HashMap<String, Float> optimalPosition){
        for (int i = 0; i < populationSize; i++) {
            int p = i;
            velocity[p].replaceAll((k,v)->
                    Math.max(-4, Math.min(4,
                            inertiaFactor*v +
                            learningFactor1*rng.nextFloat()*(expPosition[p].get(k) - position[p].get(k)) +
                            learningFactor2*rng.nextFloat()*(optimalPosition.get(k) - position[p].get(k))
                    )));
        }
    }

    /**
     * Updates the position of all particles
     * @param velocity the current velocities
     * @param position the current positions
     */
    private void updatePosition(HashMap<String, Float>[] velocity, HashMap<String, Float>[] position){
        for (int i = 0; i < populationSize; i++) {
            int p = i;
            position[p].replaceAll((k,v) ->
                        v + velocity[p].get(k)
                    );
        }
    }

    /**
     * Calculates the fitness of a particles using the Modularity score
     * Changed version of the existing ModularityMetric
     * @param graph the graph PSO_LPA runs on
     * @param cover the position of a single particle
     * @return the modularity score of the cover
     */
    public static double calculateFitness(CustomGraph graph, Map<String, Float> cover) throws InterruptedException {
		int edgeCount = graph.getEdgeCount()/2;
		double modularity = 0;
		Matrix adjacency = graph.getNeighbourhoodMatrix();
		Node[] nodes = graph.nodes().toArray(Node[]::new);

		for(int i = 0; i < graph.getNodeCount(); i++) {
			Node n1 = nodes[i];
			double deg1 = graph.getNeighbours(n1).size();
			int com1 = Math.round(cover.get(n1.getId()));
			for(int j = i+1; j < graph.getNodeCount(); j++) {
				Node n2 = nodes[j];
			    int com2 = Math.round(cover.get(n2.getId()));
				if(com1 == com2) {
					double deg2 = graph.getNeighbours(n2).size();
					modularity -= deg1*deg2/(2*edgeCount);
					if(adjacency.get(i, j) > 0){
						modularity += 1;
					}
				}
			}
		}
		return modularity/edgeCount;
	}

    /**
     * Runs the BMLPA part of the algorithm
     * @param graph the graph PSO_LPA runs on
     * @param initialization the best position found by PSO
     * @return the final cover of the PSO_LPA
     */
    private Cover runLabelPropagation(CustomGraph graph, HashMap<String, Integer> initialization) throws InterruptedException {
        Map<String, Map<Integer,Float>> oldCover = new HashMap();
        Map<String, Map<Integer,Float>> newCover;
        for (Map.Entry<String, Integer> entry : initialization.entrySet()) {
            oldCover.put(entry.getKey(),new HashMap());
            oldCover.get(entry.getKey()).put(entry.getValue(),1f);
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
            if (getId(oldCover).equals(getId(newCover)))
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
        Set<Integer> removed = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> ci : sub.entrySet()) {
            Set<Integer> value = ci.getValue();
            value.removeAll(removed);
            if (value.size() > 1) {
                removed.add(ci.getKey());
                coms.remove(ci.getKey());
            }
        }

        Map<Integer, Set<String>> splitCommunities = new HashMap<>();
        Map<Integer, Integer> commSplits = new HashMap<>();
        int k = Collections.max(coms.keySet())+1;
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
     * @param graph the graph PSO_LPA runs on
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
        if (dest.get(x).size() > 0) {
            float bMax = Collections.max(dest.get(x).values());
            Map<Integer, Float> temp = new HashMap<>();
            for (Map.Entry<Integer, Float> cb : dest.get(x).entrySet()) {
                if (cb.getValue() / bMax >= filter)
                    temp.put(cb.getKey(), cb.getValue());
            }
            normalize(temp);
            dest.put(x, temp);
        }
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
     * @param x community belonging coefficients of a node
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
