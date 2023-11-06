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
    private float initChance = 0.5f;
    private float inertiaFactor = 0.5f;
    private float learningFactor1 = 0.5f;
    private float learningFactor2 = 0.5f;
    private float changeChance = 0.5f;
    private float addComChance = 0.1f;
    private int maxPSOIterations = 50;
    private float filter = 0.5f;
    private int maxLPAIterations = 50;
    private static Random rng = new Random();

    protected static final String PSO_POPULATION_SIZE_NAME = "psoPopulationSize";
    protected static final String PSO_INERTIA_FACTOR_NAME = "psoInertiaFactor";
    protected static final String PSO_LEARNING_FACTOR_POPULATION_NAME = "psoLearningFactorPopulation";
    protected static final String PSO_LEARNING_FACTOR_GLOBAL_NAME = "psoLearningFactorGlobal";
    protected static final String PSO_MAX_ITERATION_NAME = "psoMaxIteration";
    protected static final String PSO_INIT_SPREADING_CHANCE_NAME = "psoInitSpreadingChance";
    protected static final String PSO_SPREADING_CHANCE_NAME = "psoSpreadingChance";

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
        parameters.put(PSO_LEARNING_FACTOR_POPULATION_NAME, Float.toString(learningFactor1));
        parameters.put(PSO_LEARNING_FACTOR_GLOBAL_NAME, Float.toString(learningFactor2));
        parameters.put(PSO_MAX_ITERATION_NAME, Integer.toString(maxPSOIterations));
        parameters.put(PSO_INIT_SPREADING_CHANCE_NAME, Float.toString(initChance));
        parameters.put(PSO_SPREADING_CHANCE_NAME, Float.toString(changeChance));
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
                case PSO_LEARNING_FACTOR_POPULATION_NAME:
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
                case PSO_SPREADING_CHANCE_NAME:
                    changeChance = Float.parseFloat(entry.getValue());
                    if (changeChance < 0 || changeChance > 1)
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
        int[] k = new int[1];
        return runLabelPropagation(graph,runParticleSwarmOptimization(graph, k), k);
    }

    private HashMap<String, Integer> runParticleSwarmOptimization(CustomGraph graph, int[] k)  throws InterruptedException {
        HashMap<String, Integer>[] populationPosition = populationPositionInitialization(graph, k);
        HashMap<String, Integer>[] populationVelocity = new HashMap[populationSize];
        HashMap<String, Integer>[] populationExperiencePosition = new HashMap[populationSize];
        double[] populationExperienceFitness = new double[populationSize];
        HashMap<String, Integer> optimalPosition;
        double optimalFitness = -1;
        int temp = 0;
        for (int i = 0; i < populationSize; i++){
            populationVelocity[i] = new HashMap<>();
            for (Node n : graph.nodes().toList())
                populationVelocity[i].put(n.getId(), 0);
            populationExperiencePosition[i] = (HashMap<String, Integer>)populationPosition[i].clone();
            populationExperienceFitness[i] = calculateFitness(graph,populationPosition[i]);
            if (optimalFitness < populationExperienceFitness[i]) {
                temp = i;
                optimalFitness = populationExperienceFitness[i];
            }
        }
        optimalPosition = (HashMap<String, Integer>)populationPosition[temp].clone();

        int iteration = 0;
        while (maxPSOIterations > iteration) {
            updateVelocity(populationVelocity, populationPosition, populationExperiencePosition, optimalPosition, populationExperienceFitness);
            updatePosition(graph, populationVelocity, populationPosition, populationExperiencePosition, populationExperienceFitness, k);
            temp = -1;
            for (int i = 0; i < populationSize; i++){
                double fitness =  calculateFitness(graph,populationPosition[i]);
                if (fitness > populationExperienceFitness[i]) {
                    populationExperienceFitness[i] = fitness;
                    populationExperiencePosition[i] = (HashMap<String, Integer>)populationPosition[i].clone();
                }
                if (optimalFitness < populationExperienceFitness[i]) {
                    temp = i;
                    optimalFitness = populationExperienceFitness[i];
                }
            }
            if (temp >= 0)
                optimalPosition = (HashMap<String, Integer>)populationPosition[temp].clone();
            iteration++;
            System.out.println(optimalFitness);
            for (Map<String,Integer> c : populationPosition) {
                Set<Integer> communitySet = new HashSet<>();
                for (Map.Entry<String, Integer> id : c.entrySet()) {
                    communitySet.add(id.getValue());
                }
                System.out.print(communitySet.size()+", ");
            }
            System.out.println();
        }

        return optimalPosition;
    }

    private HashMap<String, Integer>[] populationPositionInitialization(CustomGraph graph, int[] k) {
        HashMap<String, Integer>[] populationPosition = new HashMap[populationSize];
        k[0] = 0;
        for (int l = 0; l < populationSize; l++) {
            populationPosition[l] = new HashMap<>();
            final int p = l;
            Iterator<Node> iterator = graph.nodes().iterator();
            while (iterator.hasNext()) {
                Node j = iterator.next();
                if (populationPosition[p].containsKey(j.getId()))
                    continue;
                HashMap<String, Integer> flag = new HashMap<>();
                List<Node> nodes = new LinkedList<Node>(j.neighborNodes().toList());
                populationPosition[p].put(j.getId(), k[0]);
                while (nodes.size() > 0) {
                    Node i = nodes.get(0);
                    nodes.remove(0);
                    if (!flag.containsKey(i.getId()) && !populationPosition[p].containsKey(i.getId())) {
                        flag.put(i.getId(), 1);
                        if (rng.nextFloat() > initChance) {
                            populationPosition[p].put(i.getId(), k[0]);
                            nodes.addAll(i.neighborNodes().toList());
                        }
                    }
                }
                k[0]++;
            }
        }
        return populationPosition;
    }

    private void updateVelocity(HashMap<String, Integer>[] velocity, HashMap<String, Integer>[] position, HashMap<String, Integer>[] expPosition, HashMap<String, Integer> optimalPosition, double[] expFitness){
        for (int i = 0; i < populationSize; i++) {
            int p = i;
            velocity[p].replaceAll((k,v)->
                    (int)(
                            inertiaFactor*v +
                            learningFactor1*rng.nextFloat()*(expPosition[p].get(k) - position[p].get(k)) +
                            learningFactor2*rng.nextFloat()*(optimalPosition.get(k) - position[p].get(k)))
                    );
        }
    }

    private void updatePosition(CustomGraph graph,HashMap<String, Integer>[] velocity, HashMap<String, Integer>[] position, HashMap<String, Integer>[] expPosition, double[] expFitness, int[] kCount){
        for (int i = 0; i < populationSize; i++) {
            int p = i;
            position[p].replaceAll((k,v) ->
                    (int)(
                            ((1-Math.exp(-velocity[p].get(k)))/(1+Math.exp(-velocity[p].get(k))) > changeChance) ?
                             getNewCommunity(graph,position[p],k, kCount) : v
                    ));
        }
    }

    private int getNewCommunity(CustomGraph graph, HashMap<String, Integer> position, String key, int[] kCount){
        //graph.getNode(k).neighborNodes().collect(Collectors.groupingBy(Node::getId,n->position[p].get(n), Collectors.counting()))
        Map<Integer,Integer> occurrence = new HashMap();
        graph.getNode(key).neighborNodes().forEach(n -> {
            int k = position.get(n.getId());
            if (!occurrence.containsKey(k)) occurrence.put(k,0);
            occurrence.put(k,occurrence.get(k)+1);
        });
        int max = 0;
        for (Map.Entry<Integer, Integer> e : occurrence.entrySet())
            max += e.getValue();
        occurrence.put(kCount[0]+1,(int)(max*addComChance));
        max += (int)(max*addComChance);
        int i = rng.nextInt(max);
        for (Map.Entry<Integer, Integer> entry : occurrence.entrySet()) {
            i -= entry.getValue();
            if (i<=0) {
                if (entry.getKey() == kCount[0]+1) kCount[0]++;
                return entry.getKey();
            }
        }
        return 0;
    }
/*
    private double calculateFitness(CustomGraph graph, HashMap<String, Integer> position) throws InterruptedException {
        int edgeCount = graph.getEdgeCount()/2;
        double modularity = 0;
        Matrix adjacency = graph.getNeighbourhoodMatrix();
        Node[] nodes = graph.nodes().toArray(Node[]::new);

        for(int i = 0; i < graph.getNodeCount(); i++) {
            Node n1 = nodes[i];
            double deg1 = graph.getNeighbours(n1).size();
            List<Integer> com1 = Collections.singletonList(position.get(n1.getId()));
            for(int j = i+1; j < graph.getNodeCount(); j++) {
                Node n2 = nodes[j];
                List<Integer> com2 = Collections.singletonList(position.get(n2.getId()));
                com2.retainAll(com1);
                if(com2.size() != 0) {
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
*/
    public static double calculateFitness(CustomGraph graph, Map<String, Integer> cover) {
        Map<Integer, List<String>> communityMap = new HashMap<>();
        Set<Integer> communitySet = new HashSet<>();

        for (Map.Entry<String, Integer> entry : cover.entrySet()) {
            String nodeId = entry.getKey();
            int communityId = entry.getValue();
            communitySet.add(communityId);
            communityMap.computeIfAbsent(communityId, k -> new ArrayList<>()).add(nodeId);
        }

        double modularity = 0.0;
        double m = graph.getEdgeCount() / 2;

        for (int communityId : communitySet) {
            List<String> communityNodes = communityMap.get(communityId);
            double degreeSum = 0;
            Map<Edge, Integer> interEdges = new HashMap<>();
            for (String n : communityNodes) {
                degreeSum += graph.getNode(n).getDegree();
                for (Edge e : graph.getNode(n).edges().toList()) {
                    interEdges.computeIfAbsent(e, k -> 1);
                    interEdges.put(e, interEdges.get(e) + 1);
                }
            }
            double L_c = interEdges.entrySet().parallelStream().filter(e -> e.getValue() > 1).count();
            double communityContribution = L_c / m - (degreeSum * degreeSum) / (4*m*m);
            modularity += communityContribution;
        }

        return modularity / communitySet.size();
    }


    private Cover runLabelPropagation(CustomGraph graph, HashMap<String, Integer> initialization, int[] kCount) throws InterruptedException {
        Map<String, Map<Integer,Float>> cover = new HashMap();
        for (Map.Entry<String, Integer> entry : initialization.entrySet()) {
            cover.put(entry.getKey(),new HashMap());
            cover.get(entry.getKey()).put(entry.getValue(),1f);
        }
        int i = 0;
        while (i < maxLPAIterations)
        {
            Map<String, Map<Integer,Float>> coverNew = new HashMap();
            for (Map.Entry<String, Map<Integer,Float>> entry : cover.entrySet()) {
                Map<Integer, Float> comm = new HashMap();
                addHashmaps(comm,entry.getValue());
                Iterator<Node> neighbors = graph.getSuccessorNeighbours(graph.getNode(entry.getKey())).iterator();
                while (neighbors.hasNext()) {
                    addHashmaps(comm, cover.get(neighbors.next().getId()));
                }
                float max = comm.entrySet().stream().max(Comparator.comparing(Map.Entry<Integer,Float>::getValue)).get().getValue();
                Map<Integer, Float> temp = new HashMap();
                for (Map.Entry<Integer,Float> c : comm.entrySet()) {
                    if (max*filter < c.getValue())
                        temp.put(c.getKey(),c.getValue());
                }
                comm = temp;
                float sum = 0;
                for (Map.Entry<Integer,Float> c : comm.entrySet()) {
                    sum += c.getValue();
                }
                for (Map.Entry<Integer,Float> c : comm.entrySet()) {
                    comm.put(c.getKey(),c.getValue()/sum);
                }
                coverNew.put(entry.getKey(),comm);
            }
            cover = coverNew;
            i++;
        }

        Set<Integer> communitySet = new HashSet<>();
        for (Map.Entry<String, Map<Integer,Float>> entry : cover.entrySet()) {
            for (Integer id : entry.getValue().keySet()) {
                communitySet.add(id);
            }
        }
        Map<Integer,Integer> remap = new HashMap<>();
        int index = 0;
        for (Integer id : communitySet){
            remap.put(id,index);
            index++;
        }
        for (Map.Entry<Integer,Integer> e : remap.entrySet())
            System.out.println(e.getKey() +"->"+e.getValue());
        Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), communitySet.size());
        Iterator<Node> nodes = graph.iterator();
        while (nodes.hasNext()){
            Node n = nodes.next();
            System.out.print(n.getId() + ": ");
            for (Map.Entry<Integer,Float> e : cover.get(n.getId()).entrySet()) {
                membershipMatrix.set(n.getIndex(), remap.get(e.getKey()), (double) e.getValue());
                System.out.print("("+remap.get(e.getKey()) +";"+ e.getValue()+"),");
            }
            System.out.println();
        }
        return new Cover(graph, membershipMatrix);
    }

    private void addHashmaps(Map<Integer,Float> a, Map<Integer,Float> b){
        for (Map.Entry<Integer,Float> entry : b.entrySet()) {
            if (a.containsKey(entry.getKey()))
                a.put(entry.getKey(),a.get(entry.getKey())+entry.getValue());
            else
                a.put(entry.getKey(),entry.getValue());
        }
    }
}
