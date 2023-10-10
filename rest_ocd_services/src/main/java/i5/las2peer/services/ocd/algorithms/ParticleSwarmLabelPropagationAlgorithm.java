package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.*;
import java.util.stream.Collectors;

import io.reactivex.internal.operators.flowable.FlowableOnErrorReturn;
import org.apache.jena.atlas.iterator.Iter;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

public class ParticleSwarmLabelPropagationAlgorithm  implements OcdAlgorithm {

    private int populationSize = 10;
    private float initChance = 0.5f;
    private float inertiaFactor = 0.5f;
    private  float learningFactor1 = 1;
    private  float learningFactor2 = 1;
    private  int maxIterations = 10;
    private static Random rng;

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.PSO_LPA_ALGORITHM;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {

    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        return compatibilities;
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) {
        return runLabelPropagation(graph,runParticleSwarmOptimization(graph));
    }

    private HashMap<String, Integer> runParticleSwarmOptimization(CustomGraph graph) {
        int[] k = new int[1];
        HashMap<String, Integer>[] populationPosition = populationPositionInitialization(graph, k);
        HashMap<String, Integer>[] populationVelocity = new HashMap[populationSize];
        HashMap<String, Integer>[] populationExperiencePosition = new HashMap[populationSize];
        float[] populationExperienceFitness = new float[populationSize];
        HashMap<String, Integer> optimalPosition;
        float optimalFitness = -1;
        int temp = 0;
        for (int i = 0; i < populationSize; i++){
            populationVelocity[i] = new HashMap<>();
            populationExperiencePosition[i] = (HashMap<String, Integer>)populationPosition[i].clone();
            populationExperienceFitness[i] =  calculateFitness(populationPosition[i]);
            if (optimalFitness < populationExperienceFitness[i]) {
                temp = i;
                optimalFitness = populationExperienceFitness[i];
            }
        }
        optimalPosition = (HashMap<String, Integer>)populationPosition[temp].clone();

        int iteration = 0;
        while (maxIterations > iteration) {
            updateVelocity(populationVelocity, populationPosition, populationExperiencePosition, optimalPosition, populationExperienceFitness);
            updatePosition(populationVelocity, populationPosition, populationExperiencePosition, populationExperienceFitness, k);
            temp = -1;
            for (int i = 0; i < populationSize; i++){
                float fitness =  calculateFitness(populationPosition[i]);
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
        }

        return optimalPosition;
    }

    private HashMap<String, Integer>[] populationPositionInitialization(CustomGraph graph, int[] k) {
        HashMap<String, Integer>[] populationPosition = new HashMap[populationSize];
        k[0] = 1;
        for (int l = 0; l < populationSize; l++) {
            populationPosition[l] = new HashMap<>();
            final int p = l;
            Iterator<Node> iterator = graph.nodes().iterator();
            while (iterator.hasNext()) {
                Node j = iterator.next();
                if (populationPosition[p].get(j.getId()) != 0)
                    continue;
                HashMap<String, Integer> flag = new HashMap<>();
                List<Node> nodes = j.neighborNodes().toList();
                while (nodes.size() > 0) {
                    Node i = nodes.get(0);
                    nodes.remove(0);
                    if (flag.get(i.getId()) == 0 && populationPosition[p].get(i.getId()) == 0) {
                        flag.put(i.getId(), 1);
                        if (rng.nextFloat() > initChance) {
                            populationPosition[p].put(i.getId(), populationPosition[p].get(j.getId()));
                            nodes.addAll(i.neighborNodes().toList());
                        }
                    }
                }
                k[0]++;
            }
        }
        return populationPosition;
    }

    private void updateVelocity(HashMap<String, Integer>[] velocity, HashMap<String, Integer>[] position, HashMap<String, Integer>[] expPosition, HashMap<String, Integer> optimalPosition, float[] expFitness){
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

    private void updatePosition(HashMap<String, Integer>[] velocity, HashMap<String, Integer>[] position, HashMap<String, Integer>[] expPosition, float[] expFitness, int[] kCount){


    }

    private float calculateFitness(HashMap<String, Integer> position){
        return 0.5f;
    }

    private Cover runLabelPropagation(CustomGraph graph, HashMap<String, Integer> initialization) {
        return  null;
    }
}
