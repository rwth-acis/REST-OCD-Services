package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import y.base.Edge;
import y.base.EdgeCursor;

import java.util.*;

public class CommunityOverlapPropagationAlgorithm implements OcdAlgorithm{
    /**
     * Each vertex can belong to up to v communities.
     */
    private static int v = 5;


    /**
     * Maximum loops times should be terminated.(some graphs will never terminate)
     */
    private static int loops  =300;

    /*
     * PARAMETER NAME
     */
    protected static final String MAX_COMMUNITY_NUMBER_OF_EACH_NODE = "max community number of each node";

    protected static final String MAX_LOOPS = "max loops";

    /**
     * Default constructor that returns algorithm instance with default parameter values
     */
    public CommunityOverlapPropagationAlgorithm() {
    }


    @Override
    public Cover detectOverlappingCommunities(CustomGraph customGraph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {

        if(customGraph.isOfType(GraphType.DYNAMIC)){
            //as implement in map way, the nodes of dynamic graphs should have accurate ids when building
            Cover resulting_cover = new Cover(customGraph,new ArrayList<Cover>());
            Map<Integer,Map<Integer,Double>> membershipsMaps=new HashMap<>();
            int maxCommunitiesId=0;//if the following graphs delete some nodes, the label of its index should be kept.
            // (As all labels may be used to represent a community)
            for(CustomGraph staticGraph : customGraph.getGraphSeries()) {
                int maxNodeIdOfCurStaticGraph=staticGraph.getMaxNodeId();
                if(maxNodeIdOfCurStaticGraph > maxCommunitiesId) maxCommunitiesId=maxNodeIdOfCurStaticGraph;
                Map<Integer,Map<Integer,Double>> adjacencyMaps = createAdjacencyMaps(staticGraph);
                membershipsMaps=initMembershipsMaps(staticGraph,membershipsMaps);//if has new nodes,set all these new nodes by its own id as label with bc=1
                //set every nodes with its own id as its label with bc=1
                int loopsCopy=loops;
                membershipsMaps=COPRAMaps(adjacencyMaps,membershipsMaps,v,loopsCopy);
                Matrix memberships=formMemberships(maxNodeIdOfCurStaticGraph,maxCommunitiesId,membershipsMaps);
                //printCommunities(memberships);
                resulting_cover.addCoverintoCoverSeries(new Cover(staticGraph,memberships));
            }

            return resulting_cover;
        }else{//else it's a static Graph
            // create adjacency matrix from the input graph
            Matrix adjacency_matrix = createAdjacencyMatrix(customGraph);
            //the memberships is an n*n Matrix, some columns are all 0.
            Matrix memberships = COPRAMatrix(adjacency_matrix, loops);
            memberships=simplifyMemeberships(memberships);
            printCommunities(memberships);//just for test
            Cover resulting_cover = new Cover(customGraph, memberships);
            return resulting_cover;
        }
    }

    private void printCommunities(Matrix memberships) {
        for (int i = 0; i < memberships.rows(); i++) {
            for (int j = 0; j < memberships.columns(); j++) {
                System.out.println("Node" + i + ": Communities" + j + ": " + memberships.get(i, j));
            }
        }
    }

    private Map<Integer,Map<Integer,Double>> COPRAMaps(Map<Integer,Map<Integer,Double>> adjacencyMaps, Map<Integer,Map<Integer,Double>> membershipsMaps, int v, int loops) {
        while(loops-->0){
            Map<Integer,Map<Integer,Double>> afterMembershipsMap=updateMembershipsMap(membershipsMaps,adjacencyMaps,v);
            if(membershipsMaps.equals(afterMembershipsMap)) break;//todo: write an own equlas function for the maps(abs<0.0001 maybe)
            membershipsMaps=afterMembershipsMap;
        }
        System.out.println("loops left:"+loops);
        return membershipsMaps;
    }


    //form the memberships matrix from membershipsMaps
    private Matrix formMemberships(int maxNodeIdOfCurStaticGraph,int maxCommunityId,Map<Integer,Map<Integer,Double>> membershipsMaps) {
        Matrix memberships=new Basic2DMatrix();

//        int maxLabel=0,maxNode=0;
//        for(Map.Entry<Integer,Map<Integer,Double>> entryMembershipMaps:membershipsMaps.entrySet()){
//
//            for (int label : entryMembershipMaps.getValue().keySet()){
//                if (label>maxLabel) maxLabel=label;
//            }
//            int curNode=entryMembershipMaps.getKey();
//            if(curNode>maxNode) maxNode= curNode;
//        }
//        memberships=memberships.resize(maxNode+1,maxLabel+1);//as index start from 0

        memberships=memberships.resize(maxNodeIdOfCurStaticGraph+1, maxCommunityId+1);
        memberships=memberships.blank();
        for(Map.Entry<Integer,Map<Integer,Double>> entryMembershipMaps:membershipsMaps.entrySet()){
            for(Map.Entry<Integer,Double> entryLabelsOfCurNode: entryMembershipMaps.getValue().entrySet()){
                memberships.set(entryMembershipMaps.getKey(),entryLabelsOfCurNode.getKey(),entryLabelsOfCurNode.getValue());
            }
        }
        printCommunities(memberships);
        //memberships=simplifyMemeberships(memberships);
        return simplifyMemeberships(memberships);
    }

    private Map<Integer,Map<Integer,Double>> initMembershipsMaps(CustomGraph customGraph,Map<Integer,Map<Integer,Double>> membershipsMaps) {
        Set<Integer> nodeIds=customGraph.getNodeIds();
        for(int i : nodeIds){
            if(!membershipsMaps.containsKey(i)){//new node i added into the customGraph
                Map<Integer,Double> BcsOfCurNode=new HashMap<>();
                BcsOfCurNode.put(i,1.0);
                membershipsMaps.put(i,BcsOfCurNode);
            }
        }
        return membershipsMaps;
    }

    private Map<Integer,Map<Integer,Double>> createAdjacencyMaps(CustomGraph customGraph) {
        Map<Integer,Map<Integer,Double>> adjacencyMaps=new HashMap<>();
        EdgeCursor edge_list = customGraph.edges(); // added
        while (edge_list.ok()) {
            Edge edge = edge_list.edge();
            addEdgeIntoAdjacencyMaps(customGraph,adjacencyMaps,edge);
            edge_list.next();
        }
        return adjacencyMaps;
    }

    private void addEdgeIntoAdjacencyMaps(CustomGraph customGraph, Map<Integer, Map<Integer, Double>> adjacencyMaps, Edge edge) {
        Map<Integer,Double> NeighborsOfCurNode = new HashMap<>();
        if(adjacencyMaps.get(edge.source().index())!=null){
            NeighborsOfCurNode= adjacencyMaps.get(edge.source().index());
        }
        NeighborsOfCurNode.put(edge.target().index(),customGraph.getEdgeWeight(edge));
        adjacencyMaps.put(edge.source().index(),NeighborsOfCurNode);
    }

    private Matrix COPRAMatrix(Matrix adjacency_matrix, int loops) {
        int nodeCount=adjacency_matrix.columns();
        Matrix memberships = new Basic2DMatrix(nodeCount,nodeCount);
        for(int i =0;i<nodeCount;i++){
            memberships.set(i,i,1.0);
        }
        while(loops-- >0){
            Matrix afterMemberships = updateMemberships(memberships,adjacency_matrix);
            if(isEqualMatrix(memberships,afterMemberships))  break;
            memberships=afterMemberships;
        }
        System.out.println("When the algorithms finishes, loops still left:"+loops);
        return memberships;//the memberships is an n*n Matrix
    }

    private boolean isEqualMatrix(Matrix m1, Matrix m2) {
        if(m1.rows()!=m2.rows() || m1.columns()!=m2.columns()) return false;
        for(int i=0;i<m1.rows();i++){
            for(int j=0;j<m1.columns();j++){
                if(!isEqualNumber(m1.get(i,j),m2.get(i,j))) return false;
            }
        }
        return true;
    }

    private boolean isEqualNumber(double v1, double v2) {
        if(Math.abs(v1-v2)>0.0001) return false;
        return true;
    }

    /**
     * delete all the zero colomns
     */
    private Matrix simplifyMemeberships(Matrix memberships) {
        int nodeCount=memberships.columns();
        Matrix simplifiedMemberships = new Basic2DMatrix(nodeCount,nodeCount);
        int curColumn=0;
        for(int i=0;i<nodeCount;i++){
            if(memberships.getColumn(i).sum() >= 1.0/v){// >0 maybe also okay
                simplifiedMemberships.setColumn(curColumn++,memberships.getColumn(i));
            }
        }
        simplifiedMemberships=simplifiedMemberships.slice(0,0,nodeCount,curColumn);
        return simplifiedMemberships;
    }

    private Map<Integer,Map<Integer,Double>> updateMembershipsMap(Map<Integer,Map<Integer,Double>> membershipsMaps, Map<Integer,Map<Integer,Double>> adjacencyMaps,int v) {
        //update the memberships bc values
        Map<Integer,Map<Integer,Double>> intermediateMembershipsMaps =new HashMap<>();
        for(Map.Entry<Integer, Map<Integer,Double>> entryOfAdjacencyMaps: adjacencyMaps.entrySet()) {
            int curNode= entryOfAdjacencyMaps.getKey();
            double sumOfCurrentRow=0;
            for(Map.Entry<Integer,Double> NeighborAndItsWeightsOfCurNode: entryOfAdjacencyMaps.getValue().entrySet()){
                sumOfCurrentRow+=NeighborAndItsWeightsOfCurNode.getValue();
            }
            for(Map.Entry<Integer,Double> NeighborAndItsWeightOfCurNode: entryOfAdjacencyMaps.getValue().entrySet()){
                double propotion=NeighborAndItsWeightOfCurNode.getValue()/sumOfCurrentRow;
                int curNeighbor=NeighborAndItsWeightOfCurNode.getKey();
                for(Map.Entry<Integer,Double> LabelAndItsBcOfCurNeighbor : membershipsMaps.get(curNeighbor).entrySet()){
                    int curLabelOfCurNeighbor=LabelAndItsBcOfCurNeighbor.getKey();
                    double bcOfCurLabelOfCurNeighbor=LabelAndItsBcOfCurNeighbor.getValue();
                    Map<Integer,Double> labelAndItsBcOfCurNode=new HashMap<>();
                    double curBc=0;
                    if(intermediateMembershipsMaps.containsKey(curNode)){
                        labelAndItsBcOfCurNode=intermediateMembershipsMaps.get(curNode);
                        if(labelAndItsBcOfCurNode.containsKey(curLabelOfCurNeighbor)){
                            curBc=labelAndItsBcOfCurNode.get(curLabelOfCurNeighbor);
                        }
                    }
//                    if(intermediateMembershipsMaps.containsKey(curNeighbor)&&intermediateMembershipsMaps.get(curNeighbor).containsKey(curLabelOfCurNeighbor)){
//                        curBc = intermediateMembershipsMaps.get(curNeighbor).get(curLabelOfCurNeighbor);
//                    }
//                    Map<Integer,Double> newBc=intermediateMembershipsMaps.get(curNeighbor);
                    labelAndItsBcOfCurNode.put(curLabelOfCurNeighbor,curBc+propotion*bcOfCurLabelOfCurNeighbor);
                    intermediateMembershipsMaps.put(curNode,labelAndItsBcOfCurNode);
                }
            }
        }
        membershipsMaps=intermediateMembershipsMaps;
        

        //adjust the bc values in membershipMaps
        for(Map.Entry<Integer,Map<Integer,Double>> entryOfMembershipMaps: membershipsMaps.entrySet()){
            List<Integer> labelsOverThrehold=new ArrayList<>();
            List<Integer> labelsUnderThreholdButNotZero=new ArrayList<>();
            double maxBcUnderThreholdButNotZero=0;
            List<Integer> MaxLabelsUnderThreholdButNotZero=new ArrayList<>();
            double sumBcOfOverThrehold=0;
            for(Map.Entry<Integer,Double> LabelAndItsBcOfCurNeighbor : entryOfMembershipMaps.getValue().entrySet()){
                double curBc=LabelAndItsBcOfCurNeighbor.getValue();
                int curLabel=LabelAndItsBcOfCurNeighbor.getKey();
                if(curBc==0) continue;
                if(curBc >= 1.0/v){
                    labelsOverThrehold.add(curLabel);
                    sumBcOfOverThrehold+=curBc;
                }else{ //0<curBc<1/v
                    labelsUnderThreholdButNotZero.add(curLabel);
                    if(curBc>maxBcUnderThreholdButNotZero){
                        maxBcUnderThreholdButNotZero=curBc;
                        MaxLabelsUnderThreholdButNotZero.clear();
                        MaxLabelsUnderThreholdButNotZero.add(curLabel);
                    }else if (curBc==maxBcUnderThreholdButNotZero){
                        MaxLabelsUnderThreholdButNotZero.add(curLabel);
                    }
                }
            }
            if(MaxLabelsUnderThreholdButNotZero.isEmpty()) continue;//don't need to adjust the bc values

            for(int curLabel: labelsUnderThreholdButNotZero) {//remove all the labels under threhold but not zero
                Map<Integer, Double> LabelsOfCurNode = entryOfMembershipMaps.getValue();
                LabelsOfCurNode.remove(curLabel);
                membershipsMaps.put(entryOfMembershipMaps.getKey(), LabelsOfCurNode);
            }
            if(labelsOverThrehold.isEmpty()){//all the labels BC are lower than threshold,need to choose one of the maxLabels randomly
                Random random = new Random();
                int n = random.nextInt(MaxLabelsUnderThreholdButNotZero.size());
                int luckyBoy = MaxLabelsUnderThreholdButNotZero.get(n);
                Map<Integer,Double> labelsOfCurNode=entryOfMembershipMaps.getValue();
                labelsOfCurNode.put(luckyBoy,1.0);
                membershipsMaps.put(entryOfMembershipMaps.getKey(),labelsOfCurNode);
            }else{//need to normalize all the labels over Threshold
                //The enlarge propotion of all valid labels. The sumOfBC should be in (1/v, 1) now.
                double propotion=1.0 / sumBcOfOverThrehold;
                for(int curLabelOverThrehold : labelsOverThrehold){
                    double currentBC=entryOfMembershipMaps.getValue().get(curLabelOverThrehold);
                    Map<Integer,Double> LabelAndItsBcOfCurNode=entryOfMembershipMaps.getValue();
                    LabelAndItsBcOfCurNode.put(curLabelOverThrehold,currentBC*propotion);
                    membershipsMaps.put(entryOfMembershipMaps.getKey(),LabelAndItsBcOfCurNode);
                }
            }
        }
        return membershipsMaps;
    }

    private Matrix updateMemberships(Matrix memberships, Matrix adjacency_matrix) {
        int nodeCount=adjacency_matrix.columns();
        Matrix intermediateMemberships=new Basic2DMatrix(nodeCount,nodeCount);
        //intermediateMemberships = intermediateMemberships.blank();
        for(int i=0;i<nodeCount;i++){
            double sumOfCurrentRow=adjacency_matrix.getRow(i).sum();
            for(int j=0;j<nodeCount;j++){
                if(adjacency_matrix.get(i,j)==0) continue;
                //the propotion node j in all neighbors of node i
                double propotion=adjacency_matrix.get(i,j)/sumOfCurrentRow;
                for(int k=0;k<nodeCount;k++){
                    double bc=memberships.get(j,k);//bc of community k for node j from the memberships(not intermediate)
                    if(bc>=1.0/v){
                        intermediateMemberships.set(i,k,intermediateMemberships.get(i,k)+propotion*bc);
                    }
                }
            }
        }
        memberships=intermediateMemberships;

        for(int i=0;i<nodeCount;i++){
            List<Integer> labelsOverThrehold=new ArrayList<>();
            //List<Integer> labelsUnderThreholdButNotZero=new ArrayList<>();
            double maxBcUnderThreholdButNotZero=0;
            List<Integer> MaxLabelsUnderThreholdButNotZero=new ArrayList<>();
            double sumBcOfOverThrehold=0;
            for(int j=0;j<nodeCount;j++){
                double curBc=memberships.get(i,j);
                if(curBc==0) continue;
                if(curBc >= 1.0/v){
                    labelsOverThrehold.add(j);
                    sumBcOfOverThrehold+=curBc;
                }else{ // 0 < curBc < threhold
                    memberships.set(i,j,0);
                    if(curBc>maxBcUnderThreholdButNotZero){
                        maxBcUnderThreholdButNotZero=curBc;
                        MaxLabelsUnderThreholdButNotZero.clear();
                        MaxLabelsUnderThreholdButNotZero.add(j);
                    }else if (curBc==maxBcUnderThreholdButNotZero){
                        MaxLabelsUnderThreholdButNotZero.add(j);
                    }
                }

            }
            if(MaxLabelsUnderThreholdButNotZero.isEmpty()) continue;//don't need to adjust the bc values
            if(labelsOverThrehold.isEmpty()){//all the labels BC are lower than threshold,need to choose one of max labels randomly
                Random random = new Random();
                int luckyBoy = MaxLabelsUnderThreholdButNotZero.get(random.nextInt(MaxLabelsUnderThreholdButNotZero.size()));
                memberships.set(i,luckyBoy,1.0);
            }else{//normalize all the labels over Threshold
                //The enlarge propotion of all valid labels. The sumOfBC should be in (1/v, 1) now.
                double propotion=1.0 / sumBcOfOverThrehold;
                for(int j : labelsOverThrehold){
                    memberships.set(i,j,memberships.get(i,j)*propotion);
                }
            }
        }
        return memberships;
    }

    /**
     * This method creates Adjacency matrix that also holds edge weights. If entry
     * i,j is 0, then there is no edge between the nodes i,j, if it's positive, then
     * there is an edge and the value represents the weight
     *
     * @param graph     Graph based on which the adjacency matrix should be built
     * @return          Adjacency matrix based on the input graph
     */
    public Matrix createAdjacencyMatrix(CustomGraph graph) {
        Matrix A = new Basic2DMatrix(graph.nodeCount(), graph.nodeCount());
        A = A.blank(); // create an empty matrix of size n
        EdgeCursor edge_list = graph.edges(); // added
        while (edge_list.ok()) {
            Edge edge = edge_list.edge();
            A.set(edge.source().index(), edge.target().index(), graph.getEdgeWeight(edge));
            edge_list.next();
        }
        return A;
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.COMMUNITY_OVERLAP_PROPAGATION_ALGORITHM;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        compatibilities.add(GraphType.WEIGHTED);
        compatibilities.add(GraphType.DYNAMIC);
        return compatibilities;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException{
        if(parameters.containsKey(MAX_COMMUNITY_NUMBER_OF_EACH_NODE)) {
            v = Integer.parseInt(parameters.get(MAX_COMMUNITY_NUMBER_OF_EACH_NODE));
            if(v < 1) {
                throw new IllegalArgumentException();
            }
            parameters.remove(MAX_COMMUNITY_NUMBER_OF_EACH_NODE);
        }
        if(parameters.containsKey(MAX_LOOPS)) {
            loops = Integer.parseInt(parameters.get(MAX_LOOPS));
            if(loops < 1) {
                throw new IllegalArgumentException();
            }
            parameters.remove(MAX_LOOPS);
        }
        if(parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MAX_COMMUNITY_NUMBER_OF_EACH_NODE, Integer.toString(v));
        parameters.put(MAX_LOOPS, Integer.toString(loops));
        return parameters;
    }
}
