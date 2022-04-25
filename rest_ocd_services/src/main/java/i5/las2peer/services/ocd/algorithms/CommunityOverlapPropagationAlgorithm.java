package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.WeakClique;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.glassfish.jersey.internal.inject.Custom;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.util.Tuple;
import y.view.Graph2D;

import java.util.*;

public class CommunityOverlapPropagationAlgorithm implements OcdAlgorithm{
    /**
     * Each vertex can belong to up to v communities.
     */
    private static int v =5;


    /**
     * Maximum loops 100 times should be terminated.
     */
    private static int loops  = 100;

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
            Cover resulting_cover = new Cover(customGraph,new ArrayList<>());
            Map<Integer,Map<Integer,Double>> membershipsMaps=initMembershipsMaps(customGraph);
            for(CustomGraph cg : customGraph.getGraphSeries()) {
                Map<Integer,Map<Integer,Double>> adjacencyMaps = createAdjacencyMaps(cg);
                membershipsMaps=COPRAMaps(adjacencyMaps,membershipsMaps,v,loops);
                Matrix memberships=formMemberships(membershipsMaps);
                resulting_cover.addCoverSeries(new Cover(cg,memberships));
            }
            return resulting_cover;
        }else{//else it's a static Graph
            // create adjacency matrix from the input graph
            Matrix adjacency_matrix = createAdjacencyMatrix(customGraph);
            //the memberships is an n*n Matrix, some columns are all 0.
            Matrix memberships = COPRAMatrix(adjacency_matrix, v, loops);
            memberships=simplifyMemeberships(memberships);
            //printCommunities(memberships);//just for test
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
            if(isEqualMaps(membershipsMaps,afterMembershipsMap)) break;
            membershipsMaps=afterMembershipsMap;
        }
        return membershipsMaps;
    }

    private boolean isEqualMaps(Map<Integer,Map<Integer,Double>> membershipsMaps, Map<Integer,Map<Integer,Double>> afterMembershipsMap) {
    }

    //form the memberships matrix from membershipsMaps
    private Matrix formMemberships(Map<Integer,Map<Integer,Double>> membershipsMaps) {

    }

    private Map<Integer,Map<Integer,Double>> initMembershipsMaps(CustomGraph customGraph) {
        Set<Integer> nodeIds=customGraph.getNodeIds();
        Map<Integer,Map<Integer,Double>> membershipsMaps=new HashMap<>();
        for(int i : nodeIds){
            Map<Integer,Double> BCsOfCurNode=membershipsMaps.get(i);
            BCsOfCurNode.put(i,1.0);
        }
        return membershipsMaps;
    }

    private Map<Integer,Map<Integer,Double>> createAdjacencyMaps(CustomGraph customGraph) {
        Map<Integer,Map<Integer,Double>> adjacencyMaps=new HashMap<>();
        EdgeCursor edge_list = customGraph.edges(); // added
        while (edge_list.ok()) {
            Edge edge = edge_list.edge();
            Map<Integer,Double> curNodeNeighbours= adjacencyMaps.get(edge.source().index());
            curNodeNeighbours.put(edge.target().index(),customGraph.getEdgeWeight(edge));
            adjacencyMaps.put(edge.source().index(),curNodeNeighbours);
            edge_list.next();
        }
        return adjacencyMaps;
    }

    private Matrix COPRAMatrix(Matrix adjacency_matrix, int v, int loops) {
        int nodeCount=adjacency_matrix.columns();
        Matrix memberships = new Basic2DMatrix(nodeCount,nodeCount);
        for(int i =0;i<nodeCount;i++){
            memberships.set(i,i,1);
        }
        while(loops-- >0){
            Matrix afterMemberships = updateMemberships(memberships,adjacency_matrix,v);
            if(isEqualMatrix(memberships,afterMemberships))  break;
            memberships=afterMemberships;
        }
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
        if(Math.abs(v1-v2)>0.00001) return false;
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
            if(memberships.getColumn(i).sum()>0){
                simplifiedMemberships.setColumn(curColumn++,memberships.getColumn(i));
            }
        }
        simplifiedMemberships=simplifiedMemberships.slice(0,0,nodeCount,curColumn);
        return simplifiedMemberships;
    }

    private Map<Integer,Map<Integer,Double>> updateMembershipsMap(Map<Integer,Map<Integer,Double>> membershipsMaps, Map<Integer,Map<Integer,Double>> adjacencyMaps,int v) {
        Map<Integer,Map<Integer,Double>> intermediateMembershipsMaps =new HashMap<>();
        for(Map.Entry<Integer, Map<Integer,Double>> entryAdjacencyMaps: adjacencyMaps.entrySet()) {
            double sumOfCurrentRow=0;
            for(Map.Entry<Integer,Double> entryNeighboursOfCurNode: entryAdjacencyMaps.getValue().entrySet()){
                sumOfCurrentRow+=entryNeighboursOfCurNode.getValue();
            }
            for(int i : entryAdjacencyMaps.getValue().keySet()){
                double propotion=entryAdjacencyMaps.getValue().get(i)/sumOfCurrentRow;
            }
        }


    }

    private Matrix updateMemberships(Matrix memberships, Matrix adjacency_matrix, int v) {
        int nodeCount=adjacency_matrix.columns();
        Matrix intermediateMemberships=new Basic2DMatrix(nodeCount,nodeCount);
        intermediateMemberships = intermediateMemberships.blank();
        for(int i=0;i<nodeCount;i++){
            double sumOfCurrentRow=adjacency_matrix.getRow(i).sum();
            for(int j=0;j<nodeCount;j++){
                //the propotion node j in all neighbors of node i
                double propotion=adjacency_matrix.get(i,j)/sumOfCurrentRow;
                for(int k=0;k<nodeCount;k++){
                    double bc=memberships.get(j,k);//bc of community k for node j from the memberships(not intermediate)
                    if(bc>0){
                        intermediateMemberships.set(i,k,intermediateMemberships.get(i,k)+propotion*bc);
                    }
                }
            }
        }
        memberships=intermediateMemberships;
        for(int i=0;i<nodeCount;i++){
            boolean hasLabelOverThreshold=false;
            double sumOfBC=0;
            for(int j=0;j<nodeCount;j++){
                double currentBC=memberships.get(i,j);
                if(currentBC >= 1.0/v){
                    hasLabelOverThreshold=true;
                    sumOfBC+=currentBC;
                }
            }
            if(sumOfBC==1) continue;
            if(hasLabelOverThreshold){//normalize all the labels over Threshold
                //The enlarge propotion of all valid labels. The sumOfBC should be in (1/v, 1) now.
                double propotion=1.0 / sumOfBC;
                for (int j=0;j<nodeCount;j++){
                    double currentBC=memberships.get(i,j);
                    if(currentBC>=1.0 / v){
                        memberships.set(i,j,currentBC*propotion);
                    }else{
                        memberships.set(i, j, 0);
                    }
                }
            }else{//all the labels BC are lower than threshold
                List<Integer> candidates=new ArrayList();
                for (int j=0;j<nodeCount;j++){
                    double currentBC=memberships.get(i,j);
                    if(currentBC>0){
                        candidates.add(j);
                        memberships.set(i, j, 0);
                    }
                }
                Random random = new Random();
                int n = random.nextInt(candidates.size());
                int luckyBoy = candidates.get(n);
                memberships.set(i,luckyBoy,1);
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
        if(parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(MAX_COMMUNITY_NUMBER_OF_EACH_NODE, Integer.toString(v));
        return parameters;
    }
}
