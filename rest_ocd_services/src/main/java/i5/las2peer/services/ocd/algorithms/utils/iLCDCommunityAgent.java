package i5.las2peer.services.ocd.algorithms.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The class corresponding to a community agent used in the iLCD algorithm
 */
public class iLCDCommunityAgent {
    /**
     * The community id
     */
    private int id;
    /**
     * Counter to index the communities
     */
    private static int communityIndexer = 0;
    /**
     * The birth timestamp of a community
     */
    private String birth;
    /**
     * The death timestamp of a community
     */
    private String death;
    /**
     * The set of node agents included in the community
     */
    private HashSet<iLCDNodeAgent> nodes = new HashSet<iLCDNodeAgent>();

    public iLCDCommunityAgent() {
        this.id = communityIndexer;
        communityIndexer++;
    }

    public iLCDCommunityAgent(iLCDCommunityAgent community) {
        this.nodes = community.getNodes();

    }

    public iLCDCommunityAgent(int id) {
        this.id = id;
    }

    //////////////////////////////////////////////////Getters & Setters/////////////////////////////////////////////////

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getDeath() {
        return death;
    }

    public void setDeath(String death) {
        this.death = death;
    }

    public HashSet<iLCDNodeAgent> getNodes() {
        return nodes;
    }

    public void setNodes(HashSet<iLCDNodeAgent> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNodeNamesAsList(){
        List<String> result = new ArrayList<>();
        for(iLCDNodeAgent node: nodes){
            result.add(node.getNodeName());
        }
        return result;
    }

    ///////////////////////////////////////////////////Methods//////////////////////////////////////////////////////////

    /**
     * Seclusion measures the quality of the community. In detail, it measures how well the community is separated from
     * the remaining part of the system. The smaller the value, the more agents of the community have bonds outside of it.
     * @return the seclusion of the community
     */
    public double getSeclusion() {
        double seclusion = 0;

        for (iLCDNodeAgent node : nodes) {
            seclusion += node.getRepresentativeness(this);
        }
        return seclusion;
    }

    /**
     * Returns the potential belonging of a node to the community. Represents how strongly the node is related to the
     * community.
     * @param node node that asks for integration/rmoval
     * @return the value of potential belonging
     */
    public double getPotentialBelonging(iLCDNodeAgent node) {
        HashSet<iLCDNodeAgent> neighborsInC = new HashSet<iLCDNodeAgent>(this.getNodes());
        neighborsInC.retainAll(node.getNeighbors());
        double potentialBelonging = 0;

        for (iLCDNodeAgent nodeAgent : neighborsInC) {
            potentialBelonging += nodeAgent.getRepresentativeness(this);
        }
        return potentialBelonging;
    }

    /**
     * Adds a node to the community. And adds this community to the node's list of communities.
     * @param node the node to be added
     */
    public void addNodeToCommunity(iLCDNodeAgent node) {
        if (!this.nodes.contains(node)) {
            this.nodes.add(node);
            node.addCommunity(this);
        } else {
            System.out.println("addNodeToCommunity: Node" + node.getNode().getName() + " already in community " + this.id);
        }
    }

    /**
     * Removes a node from the community. And removes this community from the node's list of communities.
     * @param node the node to be removed
     */
    public void removeNodeFromCommunity(iLCDNodeAgent node) {
        if (this.nodes.contains(node)) {
            this.nodes.remove(node);
            node.removeCommunity(this);
        } else {
            System.out.println("removeNodeFromCommunity: Node " + node.getNode().getName() + " not in community " + this.id);
        }
    }

    /**
     * Decides whether to integrate a node to the community or not. Compares the potential belonging to the seclusion with
     * a given threshold.
     * @param node the node to be added
     * @param threshold the integration threshold
     * @return true if the node is to be integrated
     */
    public boolean decideIntegration(iLCDNodeAgent node, double threshold) {
        double pb = getPotentialBelonging(node);
        double sec = getSeclusion();
        //System.out.println("Integration " + pb + " vs " + threshold*sec);
        if (getPotentialBelonging(node) >= threshold * getSeclusion()) {
            return true;
        }
        return false;
    }

    /**
     * Compares the age of the community.
     * @param community community to compare
     * @return true if the community is younger than the given community.
     */
    public boolean isYounger(iLCDCommunityAgent community) {
        return Integer.parseInt(this.birth) > Integer.parseInt(community.birth);
    }

    /**
     * Decides whether a community is to be integrated into this community. Computes the community likeness and compares
     * it to the seclusion.
     * @param youngerCommunity the candidate to dusion with
     * @param threshold the fusion threshold
     * @return true if the community is to be absorbed
     */
    public boolean decideFusion(iLCDCommunityAgent youngerCommunity, double threshold) {
        //Representativity of common nodes from Cazabet's shared code
        double representativityOfCommonNodes = 0;
        for(iLCDNodeAgent node: getCommonNodes(youngerCommunity)){
            representativityOfCommonNodes += node.getRepresentativeness(youngerCommunity);
        }

        // Community Likeness from the paper
        double communityLikeness = 0;
        for (iLCDNodeAgent node: youngerCommunity.getNodes()) {
            communityLikeness += this.getPotentialBelonging(node);
        }
        double sec = youngerCommunity.getSeclusion();

        //System.out.println(representativityOfCommonNodes + " vs " + communityLikeness);
        return (representativityOfCommonNodes > threshold*youngerCommunity.getSeclusion());
    }

    public ArrayList<iLCDNodeAgent> getCommonNodes(iLCDCommunityAgent community) {
        ArrayList<iLCDNodeAgent> commonNodes = new ArrayList<>(community.getNodes());
        commonNodes.retainAll(this.getNodes());
        return commonNodes;
    }
}
