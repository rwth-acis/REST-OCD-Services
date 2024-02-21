package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import ca.pfv.spmf.algorithms.frequentpatterns.apriori_close.AlgoAprioriClose;


/**
 * Implements the algorithm to the ABACUS (Overlapping Community Detection based on Information Dynamics) method, by Z. Sun, B. Wang, J. Sheng,Z. Yu, J. Shao:
 * https://doi.org/10.1109/ACCESS.2018.2879648
 * Handles undirected and unweighted graphs.
 */
public class ABACUSAlgorithm implements OcdMultiplexAlgorithm {

    /**
     * The threshold value used as input for the frequent closed item set mining algorithm
     */
    private double threshold = 2;

    /*
     * PARAMETER NAMES
     */

    protected static final String THRESHOLD = "threshold";

    /**
     * Creates a standard instance of the algorithm. All attributes are assigned
     * there default values.
     */
    public ABACUSAlgorithm() {
    }

    @Override
    public Cover detectOverlappingCommunities(MultiplexGraph graph) throws InterruptedException {
        //run FCIM on results
        Matrix membershipMatrix = new Basic2DMatrix(0,0);
        CustomGraph coverGraph = new CustomGraph();
        return new Cover(coverGraph, membershipMatrix);
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.ABACUS_ALGORITHM;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        return compatibilities;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {

    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(THRESHOLD, Double.toString(threshold));
        return parameters;
    }
}