package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.WeakClique;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
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
    private static int v = 2;


    /**
     * Maximum loops 10 times should be terminated.
     */
    private static int loops  = 10;

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
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
        // create adjacency matrix from the input graph
        Matrix adjacency_matrix = createAdjacencyMatrix(graph);
        //the memberships is an n*n Matrix, some rows are all 0.
        Matrix memberships = COPRA(adjacency_matrix, v);

        /*
        int nodeCount=graph.nodeCount();
        Matrix simplifiedMemberships = simplyMemeberships(memberships,nodeCount);
        Cover resulting_cover = new Cover(graph, simplifiedMemberships);
         */

        Cover resulting_cover = new Cover(graph, memberships);
        return resulting_cover;
    }

    /*
    private Matrix simplyMemeberships(Matrix memberships, int nodeCount) {
        Matrix simplifiedMemberships = new Basic2DMatrix(nodeCount,nodeCount);
        int j=0;
        for(int i=0;i<=nodeCount;i++){
            if(memberships.getRow(i).sum()>0){
                simplifiedMemberships.setRow(j++,memberships.getRow(i));
            }
        }
        simplifiedMemberships.sliceTopLeft(j-1,nodeCount);
        return simplifiedMemberships;
    }
     */

    private Matrix COPRA(Matrix adjacency_matrix, int v) {
        int nodeCount=adjacency_matrix.columns();
        Matrix memberships = new Basic2DMatrix(nodeCount,nodeCount);
        for(int i =1;i<=nodeCount;i++){
            memberships.set(i,i,1);
        }
        while(true){
            Matrix afterMemberships = updateMemberships(memberships,adjacency_matrix,v);
            if(afterMemberships==memberships) break;
            memberships=afterMemberships;
        }
        return memberships;//the memberships is an n*n Matrix
    }

    private Matrix updateMemberships(Matrix memberships, Matrix adjacency_matrix, int v) {
        int nodeCount=adjacency_matrix.columns();
        Matrix intermediateMemberships=new Basic2DMatrix(nodeCount,nodeCount);
        intermediateMemberships = intermediateMemberships.blank();
        for(int i=1;i<=nodeCount;i++){
            double sumOfCurrentRow=adjacency_matrix.getRow(i).sum();
            for(int j=1;i<=nodeCount;j++){
                //点j占点i所有neighbors的比重
                double propotion=adjacency_matrix.get(i,j)/sumOfCurrentRow;
                for(int k=1;k<=nodeCount;k++){
                    double belongingCoeffient=memberships.get(k,j);
                    if(belongingCoeffient>0){
                        intermediateMemberships.set(k,i,intermediateMemberships.get(k,i)+propotion*belongingCoeffient);
                    }
                }
            }
        }
        memberships=intermediateMemberships;
        for(int i=1;i<=nodeCount;i++){
            boolean hasLabelOverThreshold=false;
            double sumOfBC=0;
            for(int j=1;j<=nodeCount;j++){
                double currentBC=memberships.get(j,i);
                if(currentBC>=1/v){
                    hasLabelOverThreshold=true;
                    sumOfBC+=currentBC;
                }else {
                    memberships.set(j, i, 0);
                }
            }
            if(sumOfBC==1) break;
            if(hasLabelOverThreshold){
                //所有有效标签的bc放大的倍数
                double propotion=1/sumOfBC;
                for (int j=1;j<=nodeCount;j++){
                    double currentBC=memberships.get(j,i);
                    if(currentBC>=1/v){
                        memberships.set(j,i,memberships.get(j,i)*propotion);
                    }
                }
            }else{//all the labels BC are lower than threshold
                List<Integer> candidates=new ArrayList();
                for (int j=1;j<=nodeCount;j++){
                    double currentBC=memberships.get(j,i);
                    if(currentBC>0){
                        candidates.add(j);
                    }
                }
                Random random = new Random();
                int n = random.nextInt(candidates.size());
                int luckyBoy = candidates.get(n);
                for(int cur : candidates){
                    if(cur==luckyBoy){
                        memberships.set(cur,i,1);
                    }else{
                        memberships.set(cur,i,0);
                    }
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
