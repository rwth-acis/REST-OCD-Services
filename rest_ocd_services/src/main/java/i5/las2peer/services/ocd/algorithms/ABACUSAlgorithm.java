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
 * Implements the ABACUS (frequent pAttern mining-BAsed Community discovery in mUltidimensional networkS) algorithm
 * by Berlingerio, Michele; Pinelli, Fabio; Calabrese, Francesco (2013): https://doi.org/10.1007/s10618-013-0331-0
 * Handles multiplex graphs.
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

    protected static final String THRESHOLD_NAME = "threshold";

    /**
     * Creates a standard instance of the algorithm. All attributes are assigned
     * there default values.
     */
    public ABACUSAlgorithm() {
        database = new Database(false);
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph representativeGraph) throws InterruptedException {
        // Create transactions from the communities that where discovered in the different layers
        Map<Integer, List<Integer>> transactions = new HashMap<>();
        for (Cover cover : database.getLayerCovers(representativeGraph.getKey())) {
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

        // Run FCIM algorithm on the transactions
        AlgoAprioriClose algo = new AlgoAprioriClose();
        Itemsets itemsets = algo.runAlgorithm(this.threshold, transactions);
        // Create the membership matrix that represents the cover for the multiplex graph
        Matrix membershipMatrix = new Basic2DMatrix(representativeGraph.getNodeCount(), itemsets.getLevels().size());
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
        return new Cover(representativeGraph, membershipMatrix);
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.ABACUS_ALGORITHM;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        compatibilities.add(GraphType.MULTIPLEX);
        return compatibilities;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if(parameters.containsKey(THRESHOLD_NAME)) {
            threshold = Double.parseDouble(parameters.get(THRESHOLD_NAME));
            parameters.remove(THRESHOLD_NAME);
        }
        if(parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(THRESHOLD_NAME, Double.toString(threshold));
        return parameters;
    }

    /**
     * Sets the database. Used for testing.
     * @param database The database.
     */
    public void setDatabase(Database database) {
        this.database = database;
    }
}