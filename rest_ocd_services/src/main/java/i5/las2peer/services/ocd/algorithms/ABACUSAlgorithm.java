package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.spmf.AlgoAprioriClose;
import i5.las2peer.services.ocd.spmf.Itemset;
import i5.las2peer.services.ocd.spmf.Itemsets;
import i5.las2peer.services.ocd.utils.Database;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.*;


/**
 * Implements the algorithm to the ABACUS (Overlapping Community Detection based on Information Dynamics) method, by Z. Sun, B. Wang, J. Sheng,Z. Yu, J. Shao:
 * https://doi.org/10.1109/ACCESS.2018.2879648
 * Handles undirected and unweighted graphs.
 */
public class ABACUSAlgorithm implements OcdAlgorithm {

    /**
     * The entity handler used for access stored entities.
     */
    private static Database database;

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
        database = new Database(false);
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph representiveGraph) throws InterruptedException {
        //run FCIM on results
        Map<Integer, List<Integer>> transactions = new HashMap<>();

        for (Cover cover : database.getLayerCovers(representiveGraph.getKey())) {
            for (Community community : cover.getCommunities()) {
                String communityKey = community.getKey();
                List<Integer> memberIndices = community.getMemberIndices();
                for(Integer memberIndex : memberIndices) {
                    if (!transactions.containsKey(memberIndex)){
                        transactions.put(memberIndex, new ArrayList<Integer>());
                    }
                    transactions.get(memberIndex).add(Integer.valueOf(communityKey));
                }
            }
        }

        AlgoAprioriClose algo = new AlgoAprioriClose();
        Itemsets itemsets = algo.runAlgorithm(this.threshold, transactions);
        Matrix membershipMatrix = new Basic2DMatrix(representiveGraph.getNodeCount(), itemsets.getLevels().size());
        int communityIndex = 0;
        for (List<Itemset> level : itemsets.getLevels()) {
            for (Itemset itemset : level) {
                for(Integer item : itemset.getItems()) {
                    membershipMatrix.set(item, communityIndex, 1);
                }
                for (int i = 0; i < itemset.getItems().length; i++) {
                    membershipMatrix.set(itemset.getItems()[i], level.indexOf(itemset), 1);
                }
                communityIndex++;
            }
        }
        return new Cover(representiveGraph, membershipMatrix);
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