package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.graphs.CustomNode;

import java.util.HashSet;

/**
 * The class corresponding to a node agent used in the iLCD algorithm
 */
public class iLCDNodeAgent {
    /**
     * The node in the DynamicGraph
     */
    private CustomNode node;
    /**
     * The set of neighboring nodes (node agents)
     */
    private HashSet<iLCDNodeAgent> neighbors = new HashSet<iLCDNodeAgent>();
    /**
     * The set of communities this node agent is part of
     */
    private HashSet<iLCDCommunityAgent> communities = new HashSet<iLCDCommunityAgent>();

    public iLCDNodeAgent(CustomNode node) {
        this.node = node;
    }

    ///////////////////////////////////////////////Getters & Setters////////////////////////////////////////////////////

    public CustomNode getNode() {
        return node;
    }

    public void setNode(CustomNode node) {
        this.node = node;
    }

    public HashSet<iLCDNodeAgent> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(HashSet<iLCDNodeAgent> neighbors) {
        this.neighbors = neighbors;
    }

    public HashSet<iLCDCommunityAgent> getCommunities() {
        return communities;
    }

    public void setCommunities(HashSet<iLCDCommunityAgent> communities) {
        this.communities = communities;
    }

    /////////////////////////////////////////////////////Methods////////////////////////////////////////////////////////

    /**
     * Adds a community to the node agent's set of communities
     * @param community the community to add
     */
    public void addCommunity(iLCDCommunityAgent community) {
        if(!communities.contains(community)) {
            communities.add(community);
        }else {
            System.out.println("addCommunity: Node " + this.node.getName() + " already in community " + community.getId());
        }
    }

    /**
     * Removes a community from the node agent's set of communities
     * @param community the community to remove
     */
    public void removeCommunity(iLCDCommunityAgent community) {
        if(communities.contains(community)) {
            communities.remove(community);
        } else {
            System.out.println("removeCommunity: Node " + this.node.getName() + " not in community " + community.getId());
        }
    }

    /**
     * Adds a neighboring node. Automatically adds this node to the neighbors of the neighbor.
     * @param node the neighboring agent
     */
    public void addNeighbor(iLCDNodeAgent node) {
        if(!this.neighbors.contains(node)) {
            this.neighbors.add(node);
            node.neighbors.add(this);
        } else {
            System.out.println("addNeighbor: Nodes " + this.node.getName() + " - " + node.node.getName() + " already connected.");
        }
    }

    /**
     * Removes a neighboring node
     * @param node the neighboring agent
     */
    public void removeNeighbor(iLCDNodeAgent node) {
        if(this.neighbors.contains(node)) {
            this.neighbors.remove(node);
            node.neighbors.remove(this);
        } else {
            System.out.println("removeNeighbor: Nodes " + this.node.getName() + " - " + node.node.getName() + " not connected.");
        }
    }

    /**
     * Computes the representativness of a node to a community. Represents the connectivity of a node.
     * @param community the community to compute the representativeness.
     * @return the value of representativeness
     */
    public double getRepresentativeness(iLCDCommunityAgent community) {
        int nbNeighbors = neighbors.size();

        HashSet<iLCDNodeAgent> neighborsInC = new HashSet<iLCDNodeAgent>(this.neighbors);
        neighborsInC.retainAll(community.getNodes());
        int nbNeighborsInC = neighborsInC.size();
        if(nbNeighbors != 0) {
            return (double) nbNeighborsInC/nbNeighbors;
        } else {
            return 0;
        }

    }

    /**
     * Gets the set of shared communities with another NodeAgent.
     * @param node the other NodeAgent
     * @return the shared communities
     */
    public HashSet<iLCDCommunityAgent> getCommonCommunities(iLCDNodeAgent node) {
        HashSet<iLCDCommunityAgent> result = new HashSet<iLCDCommunityAgent>(this.getCommunities());
        result.retainAll(node.getCommunities());
        return result;
    }

    /**
     * Gets the set of neighbors that are also included in a community.
     * @param community the community to refer to
     * @return the set of neighbors that are in the community too
     */
    public HashSet<iLCDNodeAgent> getNeighborsInCommunity(iLCDCommunityAgent community) {
        HashSet<iLCDNodeAgent> result = new HashSet<iLCDNodeAgent>(community.getNodes());
        result.retainAll(this.neighbors);
        return result;
    }

}
