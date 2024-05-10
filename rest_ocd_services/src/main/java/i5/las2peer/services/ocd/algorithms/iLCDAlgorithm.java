package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.iLCDCommunityAgent;
import i5.las2peer.services.ocd.algorithms.utils.iLCDNodeAgent;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.apache.jena.base.Sys;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Int;

import java.util.*;

public class iLCDAlgorithm implements OcdAlgorithm{
    /**
     * Size of the minimal community.
     */
    private int min_C = 3;
    /**
     *Threshhold of belonging.
     */
    private double th_integration = 0.5;
    /**
     *Threshold for fusion.
     */
    private double th_merge = 0.5;

    /*
     * PARAMETER NAMES
     */
    protected static final String MINIMAL_COMMUNITY = "minimalCommunitySize";

    protected static final String INTEGRATION_THRESHOLD = "integrationThreshold";

    protected  static final String MERGE_THRESHOLD = "mergeThreshold";

    /**
     * Default constructor that returns algorithm instance with default parameter values.
     */
    public iLCDAlgorithm() {
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
        Cover result = new Cover(graph);

        HashMap<String, iLCDNodeAgent> graphNodes = new HashMap<String, iLCDNodeAgent>();
        HashMap<Integer, iLCDCommunityAgent> graphCommunities = new HashMap<Integer, iLCDCommunityAgent>();


        if (graph instanceof DynamicGraph) {
            // Set start timestamp
            // List<DynamicInteraction> dynamicInteractions = ((DynamicGraph) graph).getDynamicInteractions();
            String currentTimestep = ((DynamicGraph) graph).getDynamicInteractions().get(0).getDate();
            //for (int i = 0; i < dynamicInteractions.size(); i++) {
            //    dynamicInteractions.get(i).
            //}
            for (DynamicInteraction dynamicInteraction: ((DynamicGraph) graph).getDynamicInteractions()) {
                HashSet<iLCDCommunityAgent> modifiedCommunities = new HashSet<iLCDCommunityAgent>();
                currentTimestep = dynamicInteraction.getDate();
                String sourceName = dynamicInteraction.getSource().getName();

                String targetName = dynamicInteraction.getTarget().getName();

                switch (dynamicInteraction.getAction()) {
                    // Case ADD
                    case "+":
                        // ADD missing vertices and edge to agent network
                        addMissingAgents(graphNodes, dynamicInteraction);

                        graphNodes.get(sourceName).addNeighbor(graphNodes.get(targetName));

                        // GROWTH //
                        // Foreach community of the source node
                        for(iLCDCommunityAgent community: graphNodes.get(sourceName).getCommunities()) {
                            //if the other node does not belong to the community, try to add it
                            if(!community.getNodes().contains(graphNodes.get(targetName))) {
                                // Send request to community
                                if(community.decideIntegration(graphNodes.get(targetName), th_integration)){
                                    community.addNodeToCommunity(graphNodes.get(targetName));
                                    // TODO Handle GROWTH event
                                    modifiedCommunities.add(community);
                                }
                            }
                        }
                        // Foreach community of the target node
                        for(iLCDCommunityAgent community: graphNodes.get(targetName).getCommunities()) {
                            // if the other node does not belong to the community, try to add it
                            if(!community.getNodes().contains(graphNodes.get(sourceName))){
                                // send request to community
                                if(community.decideIntegration(graphNodes.get(sourceName), th_integration)) {
                                    community.addNodeToCommunity(graphNodes.get(sourceName));
                                    modifiedCommunities.add(community);
                                }
                            }
                        }

                        //BIRTH
                        // If there are no common communities
                        if(graphNodes.get(sourceName).getCommonCommunities(graphNodes.get(targetName)).isEmpty()) {
                            ArrayList<iLCDCommunityAgent> newCommunities = new ArrayList<iLCDCommunityAgent>();
                            // Choose birth based on the parameter
                            switch(min_C){
                                case 3:
                                    newCommunities = birthCase3(graphNodes.get(sourceName), graphNodes.get(targetName));
                                    break;
                                case 4:
                                    newCommunities = birthCase4(graphNodes.get(sourceName), graphNodes.get(targetName));
                                    break;
                            }
                            //add each new Community to the map, set its birthdate and add to MC
                            for(iLCDCommunityAgent newCommunity: newCommunities) {
                                newCommunity.setBirth(currentTimestep);
                                graphCommunities.put(newCommunity.getId(), newCommunity);
                                //TODO handle BIRTH event
                                modifiedCommunities.add(newCommunity);
                            }
                        }

                        break;
                    // Case REMOVE
                    case "-":
                        // CONTRACTION
                        //remove connection of nodes
                        graphNodes.get(sourceName).removeNeighbor(graphNodes.get(targetName));
                        //Foreach shared community
                        for(iLCDCommunityAgent commonCommunity: graphNodes.get(sourceName).getCommonCommunities(graphNodes.get(targetName))) {
                        /*
                        CONTRACTION alternative:

                        if(decide to ban graphNodes(targetNode) from commonCommunity)
                            CONTRACT
                            set new candidateNodes <- graphNodes(targetNode).getNeighborsInC(commonCommunity)
                            set tested <- graphNodes(targetNode)
                            foreach(candidate)
                                if(decide to ban candidate)
                                    CONTRACT
                                    tested <- candidate
                                    candidateNodes + candidate.getNeighborsInC(commonCommunity)
                                    candidatesNodes - tested
                                else
                                    tested + candidate
                                    candidateNodes - tested

                         same for sourceNode
                         */
                            iLCDCommunityAgent temp = new iLCDCommunityAgent(commonCommunity);
                            ArrayList<iLCDCommunityAgent> resultingCommunities = new ArrayList<>();
                            //Contraction
                            ArrayList<iLCDCommunityAgent> resultingCommunitiesSource = getContractionResult(commonCommunity, graphNodes.get(sourceName));
                            for(iLCDCommunityAgent commonCommunity2: resultingCommunitiesSource) {
                                resultingCommunities.addAll(getContractionResult(commonCommunity2, graphNodes.get(targetName)));
                            }
                            modifiedCommunities.addAll(resultingCommunities);

                            // check if rejected nodes can form a new community
                            if(resultingCommunities.size() == 1){
                                ArrayList<iLCDNodeAgent> rejectedNodes = new ArrayList<>(temp.getNodes());
                                rejectedNodes.removeAll(resultingCommunities.get(0).getNodes());

                                for(iLCDNodeAgent rejectedNode: rejectedNodes) {
                                    for(iLCDNodeAgent nodeInCom: rejectedNode.getNeighborsInCommunity(resultingCommunities.get(0))){
                                        ArrayList<iLCDCommunityAgent> newCommunities = new ArrayList<iLCDCommunityAgent>();
                                        // Choose birth based on the parameter
                                        switch(min_C){
                                            case 3:
                                                newCommunities = birthCase3(rejectedNode, nodeInCom);
                                                break;
                                            case 4:
                                                newCommunities = birthCase4(rejectedNode, nodeInCom);
                                                break;
                                        }

                                        for(iLCDCommunityAgent newCommunity: newCommunities) {
                                            newCommunity.setBirth(currentTimestep);
                                            graphCommunities.put(newCommunity.getId(), newCommunity);
                                            //TODO handle BIRTH event
                                            modifiedCommunities.add(newCommunity);
                                        }
                                    }
                                }
                            }
                            // DEATH
                            for (iLCDCommunityAgent community: resultingCommunities) {
                                if (community.getNodes().size() < min_C) {
                                    ArrayList<iLCDNodeAgent> nodesToDelete = new ArrayList<>(community.getNodes());
                                    for(iLCDNodeAgent node: nodesToDelete) {
                                        community.removeNodeFromCommunity(node);
                                    }
                                    community.setDeath(currentTimestep);
                                    //TODO Handle Death event
                                    modifiedCommunities.remove(community);
                                    graphCommunities.remove(community.getId());
                                }
                            }

                        }
                        break;

                }

                //FUSION
                ArrayList<iLCDCommunityAgent> modifiedCommunitiesSorted = new ArrayList<>(modifiedCommunities);


                //Collections.sort(modifiedCommunitiesSorted, Comparator.comparing(iLCDCommunityAgent::getId));
                //Collections.reverse(modifiedCommunitiesSorted);
                Collections.sort(modifiedCommunitiesSorted, Comparator.comparing(iLCDCommunityAgent::getId));




                HashSet<iLCDCommunityAgent> fusionCandidates = new HashSet<iLCDCommunityAgent>();
                 //Foreach modified community
                for(iLCDCommunityAgent community: modifiedCommunitiesSorted) {
                    //Get all communities "community"'s agents are part of
                    for(iLCDNodeAgent node: community.getNodes()) {
                        fusionCandidates.addAll(node.getCommunities());
                    }

                    fusionCandidates.remove(community);

                    for(iLCDCommunityAgent candidate: fusionCandidates) {
                        // try to merge all younger candidates
                        if(candidate.isYounger(community)) {
                            // If fusion condition is fulfilled
                            if(community.decideFusion(candidate, th_merge)){

                                //Add all nodes from the younger community to the older community
                                for (iLCDNodeAgent node: candidate.getNodes()) {
                                    //If they are not yet included
                                    if(!community.getNodes().contains(node)) {
                                        if(community.decideIntegration(node,th_integration)){
                                            community.addNodeToCommunity(node);
                                        }
                                    }
                                }
                                //Remove all nodes from the community
                                ArrayList<iLCDNodeAgent> candidateNodes = new ArrayList<>(candidate.getNodes());
                                for(iLCDNodeAgent node: candidateNodes){
                                    candidate.removeNodeFromCommunity(node);
                                }

                                //Death timestamp setzen
                                candidate.setDeath(currentTimestep);
                                //TODO handle Fusion event
                                //jüngere community aus denGraphCommunities löschen
                                graphCommunities.remove(candidate.getId());
                                //death timestamp setzen
                            }
                        }
                    }
                }

            }
        }else {
            System.out.println("Graph is static");
            throw new OcdAlgorithmException("Graph is static");
        }

        Matrix community_matrix = getCommunityMatrix((DynamicGraph) graph, graphCommunities);
        printCommunities(graphCommunities);
        Cover cover = new Cover(graph, community_matrix);
        return cover;
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.ILCD_ALGORITHM;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> types = new HashSet<GraphType>();
        types.add(GraphType.DYNAMIC);
        return types;
    }

    @Override
    public void setParameters(Map<String, String> parameters) {
        if(parameters.containsKey(MINIMAL_COMMUNITY)) {
            min_C = Integer.parseInt(parameters.get(MINIMAL_COMMUNITY));
            if (min_C < 3 || min_C > 4) {
                throw new IllegalArgumentException("min_C should be 3 or 4");
            }
            parameters.remove(MINIMAL_COMMUNITY);
        }

        if(parameters.containsKey(INTEGRATION_THRESHOLD)) {
            th_integration = Double.parseDouble(parameters.get(INTEGRATION_THRESHOLD));
            parameters.remove(INTEGRATION_THRESHOLD);
        }

        if(parameters.containsKey(MERGE_THRESHOLD)) {
            th_merge = Double.parseDouble(parameters.get(MERGE_THRESHOLD));
            parameters.remove(MERGE_THRESHOLD);
        }

        if (parameters.size() > 0) {
            throw new IllegalArgumentException("Too many input parameters!");
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MINIMAL_COMMUNITY, Integer.toString(min_C));
        parameters.put(INTEGRATION_THRESHOLD, Double.toString(th_integration));
        parameters.put(MERGE_THRESHOLD, Double.toString(th_merge));
        return parameters;
    }

    /**
     * Adds missing node agents to the evolving graph
     * @param map   the map of node agents
     * @param dynamicInteraction the current operation
     * @return  the updated map
     */
    private void addMissingAgents(HashMap<String, iLCDNodeAgent> map, DynamicInteraction dynamicInteraction) {
        if(!map.containsKey(dynamicInteraction.getSource().getName())) {
            map.put(dynamicInteraction.getSource().getName(), new iLCDNodeAgent(dynamicInteraction.getSource()));
        }
        if (!map.containsKey(dynamicInteraction.getTarget().getName())) {
            map.put(dynamicInteraction.getTarget().getName(), new iLCDNodeAgent(dynamicInteraction.getTarget()));
        }
    }

    /**
     * The method handling the birth of communities, when min_c is 3.
     * @param source the first node of the current operation
     * @param target the second node of the current operation
     * @return the list of resulting communities
     */
    private ArrayList<iLCDCommunityAgent> birthCase3(iLCDNodeAgent source, iLCDNodeAgent target) {
        ArrayList<iLCDCommunityAgent> result = new ArrayList<iLCDCommunityAgent>();
        HashSet<iLCDNodeAgent> commonNeighbors = new HashSet<iLCDNodeAgent>(source.getNeighbors());
        commonNeighbors.retainAll(target.getNeighbors());
        for(iLCDNodeAgent commonNeighbor: commonNeighbors) {
            iLCDCommunityAgent newCommunity = new iLCDCommunityAgent();
            newCommunity.addNodeToCommunity(source);
            newCommunity.addNodeToCommunity(target);
            newCommunity.addNodeToCommunity(commonNeighbor);
            result.add(newCommunity);
        }
        return result;
    }

    /**
     * The method handling the birth of communities, when min_c is 4.
            * @param source the first node of the current operation
     * @param target the second node of the current operation
     * @return the list of resulting communities
     */
    private ArrayList<iLCDCommunityAgent> birthCase4(iLCDNodeAgent source, iLCDNodeAgent target) {
        ArrayList<iLCDCommunityAgent> result = new ArrayList<iLCDCommunityAgent>();
        ArrayList<iLCDNodeAgent> commonNeighbors = new ArrayList<iLCDNodeAgent>(source.getNeighbors());
        commonNeighbors.retainAll(target.getNeighbors());
        for (int i = 0; i < commonNeighbors.size(); i++) {
            for (int j = i; j < commonNeighbors.size(); j++) {
                if(commonNeighbors.get(i).getNeighbors().contains(commonNeighbors.get(j))) {
                    iLCDCommunityAgent newCommunity = new iLCDCommunityAgent();
                    newCommunity.addNodeToCommunity(source);
                    newCommunity.addNodeToCommunity(target);
                    newCommunity.addNodeToCommunity(commonNeighbors.get(i));
                    newCommunity.addNodeToCommunity(commonNeighbors.get(j));
                    result.add(newCommunity);
                }
            }
        }
        return result;
    }

    /**
     * Returns the community after contraction. If the node is to be removed from the community, remove the node and recursively
     * check for the neighbors of the node whether they need to be removed
     * @param community the community to check removal
     * @param node the node to check
     * @return the resulting community
     */
    public ArrayList<iLCDCommunityAgent> getContractionResult(iLCDCommunityAgent community, iLCDNodeAgent node) {
        ArrayList<iLCDCommunityAgent> result = new ArrayList<>();
        result.add(community);

        if(!node.getCommunities().contains(community)){
            return result;
        }

        double adaptedSeclusion = community.getSeclusion()- node.getRepresentativeness(community);

        if(community.getPotentialBelonging(node) >= th_integration*adaptedSeclusion) {
            return result;
        } else {
            community.removeNodeFromCommunity(node);
            for(iLCDNodeAgent neighbor: node.getNeighborsInCommunity(community)) {
                getContractionResult(community, neighbor);
            }
            return result;
        }
    }

    public Matrix getCommunityMatrix(DynamicGraph graph, HashMap<Integer, iLCDCommunityAgent> communities) {
        if(communities == null) {
            throw new RuntimeException("Communities empty!");
        }

        ArrayList<iLCDCommunityAgent> communityList = new ArrayList<iLCDCommunityAgent>(communities.values());
        Matrix result = new Basic2DMatrix(graph.getNodeCount(), communityList.size());
        int community_index = 0;
        for(iLCDCommunityAgent community: communityList){
            if(community != null){
                for(iLCDNodeAgent node: community.getNodes()) {
                    Node realNode = graph.getNode(node.getNode().getName());
                    result.set(realNode.getIndex(),community_index,1);
                }
                community_index++;
            }

        }
        return result;
    }

    public void printCommunities(HashMap<Integer, iLCDCommunityAgent> communities) {
        ArrayList<iLCDCommunityAgent> communityList = new ArrayList<>(communities.values());
        int index = 0;
        for(iLCDCommunityAgent community: communityList){
            System.out.print(index + "\t[");
            for(iLCDNodeAgent node: community.getNodes()){
                System.out.print(node.getNode().getName()+", ");
            }
            System.out.println("]");
            index++;
        }
    }
}
