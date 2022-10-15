package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.jena.sparql.pfunction.library.assign;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.ArrayList;

/**
 * An Individual is displayed as a set of edges with the genes and the fitnes of that individual
 */
public class MLinkIndividual {
    
    private HashMap<Edge, Edge> individual;
    private double fitness;
    private HashMap<Edge,Integer> edges;
    private HashMap<Integer,HashSet<Edge>> communities;
    private HashMap<Integer,HashSet<Node>> nodeCommunity;
    private double edgeNr;

    // Constructors
    public MLinkIndividual(){
        individual = new HashMap<Edge, Edge>();
        communities = new HashMap<Integer,HashSet<Edge>>();
        edges = new HashMap<Edge,Integer>();
        nodeCommunity = new HashMap<Integer,HashSet<Node>>();
        edgeNr = 0;
        fitness = 0;
    }
    public MLinkIndividual(HashMap<Edge, Edge> individual){
        this.individual = new HashMap<Edge, Edge>();
        communities = new HashMap<Integer,HashSet<Edge>>();
        nodeCommunity = new HashMap<Integer,HashSet<Node>>();
        edges = new HashMap<Edge,Integer>();
        this.individual = individual;
        edgeNr = individual.size();
        calcCommunities();
        calcFitness();
    }
    // Getter and Setter
    public HashMap<Edge, Edge> getIndividual(){
        return individual;
    }
    public void addGene(Edge locus, Edge geneValue){
        this.individual.put(locus, geneValue);
        this.edgeNr = this.individual.size();
    }
    public double getFitness(){
        return fitness;
    }
    public void setFitness(double fitness){
        this.fitness = fitness;
    }

    public void setIndividual(HashMap<Edge, Edge> individual){
        this.individual = individual;
        this.edgeNr = this.individual.size();
    }
    public HashMap<Integer,HashSet<Edge>> getCommunities(){
        return this.communities;
    }
    public HashMap<Integer,HashSet<Node>> getNodeCommunity(){
        return this.nodeCommunity;
    }

    // Public methods for Individuals
    /**
     * Claculates fitness
     */
    public void calcFitness(){
        double fit = 0;
        double nodeNr;
        for(Integer i : this.communities.keySet()){
            double edges = this.communities.get(i).size();
            nodeNr = this.nodeCommunity.get(i).size();
            if(nodeNr < 3){
                fit = fit + 0;
            } else {
                fit = fit + edges*( (edges-(nodeNr - 1.0 )) / ((nodeNr - 2.0) * (nodeNr - 1.0)) );
            }
            
        }
        if(this.edgeNr != 0){
            this.fitness = (2.0/this.edgeNr)*fit;
        } else {
            this.fitness = 0;
        }
        
    }
    private double deltaFitness(HashMap<Integer,HashSet<Edge>> uCommunities, HashMap<Integer,HashSet<Node>> uNodeCommunity){
        double fit = 0;
        double nodeNr;
        double uFitness = 0;
        for(Integer i : uCommunities.keySet()){
            double edges = uCommunities.get(i).size();
            nodeNr = uNodeCommunity.get(i).size();
            if(nodeNr < 3){
                fit = fit + 0;
            } else {
                fit = fit + edges*( (edges-(nodeNr - 1.0 )) / ((nodeNr - 2.0) * (nodeNr - 1.0)) );
            }
            
        }
        if(this.edgeNr != 0){
            uFitness = (2.0/this.edgeNr)*fit;
        } else {
            uFitness = 0;
        }
        return uFitness;
    }
    /**
     * Saves the communities as ArrayLists
     */
    public void calcCommunities(){
        this.communities.clear();
        this.nodeCommunity.clear();
        // Initialize every edge with -1 as community
        HashMap<Edge,Integer> assignCommunity = new HashMap<Edge,Integer>();
        // System.out.println("ind Edges: " + this.individual.keySet().size());
        for(Edge e : this.individual.keySet()){
            assignCommunity.put(e,-1);
        }
        // Backtracking algorithm
        int current = 0;
        this.communities.put(current, new HashSet<Edge>());
        this.nodeCommunity.put(current, new HashSet<Node>());
        for(Edge e : this.individual.keySet()){
            ArrayList<Edge> previous = new ArrayList<Edge>();
            int tracing = 0;

            // If edge is not assigned yet
            if(assignCommunity.get(e).equals(-1)){
                assignCommunity.put(e, current);
                // Fill in different versions of community representation
                this.communities.get(current).add(e);
                this.nodeCommunity.get(current).add(e.getTargetNode());
                this.nodeCommunity.get(current).add(e.getSourceNode());
                Edge neighbor = this.individual.get(e);
                previous.add(e);
                tracing++;
                
                // Assign gene to the locus community
                while(assignCommunity.get(neighbor).equals(-1)){
                    previous.add(neighbor);
                    assignCommunity.put(neighbor,current);

                    this.communities.get(current).add(neighbor);
                    this.nodeCommunity.get(current).add(neighbor.getTargetNode());
                    this.nodeCommunity.get(current).add(neighbor.getSourceNode());

                    neighbor = this.individual.get(neighbor);
                    tracing++;
                }
                int neighborCommunity = assignCommunity.get(neighbor);
                // If gene is in different community -> assign the whole community to the gene's
                if(neighborCommunity != current){
                    tracing = tracing - 1;
                    this.communities.get(current).clear();
                    this.nodeCommunity.get(current).clear();
                    while(tracing >= 0){
                        Edge prev = previous.get(tracing);
                        assignCommunity.put(prev, neighborCommunity);

                        this.communities.get(neighborCommunity).add(prev);
                        this.nodeCommunity.get(neighborCommunity).add(prev.getTargetNode());
                        this.nodeCommunity.get(neighborCommunity).add(prev.getSourceNode());

                        tracing = tracing - 1;
                    }
                } else {
                    current++;
                    this.communities.put(current, new HashSet<Edge>());
                    this.nodeCommunity.put(current, new HashSet<Node>());
                }
            }
        }
        this.edges = assignCommunity;
        this.communities.remove(current);
        this.nodeCommunity.remove(current);

    }

    /**
     * Mutates the individual with a mutation probability
     * @param mutationProbability mutation probability
     */
    public void mutate(int mutationProbability){
		HashMap<Edge, Edge> genes = this.individual;
        Random rand = new Random();
		for(Edge key : genes.keySet()){
            if(rand.nextInt(100) < mutationProbability){
                Edge gene = genes.get(key);
                Set<Edge> neighbors = new HashSet<Edge>();
                Edge[] targetEdges = key.getTargetNode().edges().toArray(Edge[]::new);
                int nghbSizeTrgt = targetEdges.length;
                for(int i = 0; i < nghbSizeTrgt; i++){
                    if(targetEdges[i] != key && targetEdges[i] != gene){
                        neighbors.add(targetEdges[i]);
                    }

                }
                Edge[] srcEdges = key.getSourceNode().edges().toArray(Edge[]::new);
                int nghbSizeSrc = srcEdges.length;
                for(int i = 0; i < nghbSizeSrc; i++){
                    if(srcEdges[i] != key && srcEdges[i] != gene){
                        neighbors.add(srcEdges[i]);
                    }

                }
                int indSize = neighbors.size();
                if(indSize > 0){
                    int edge = new Random().nextInt(indSize);
                    int i = 0;
                    for(Edge e : neighbors){
                        if(i == edge){
                            if(e != null){
                                genes.put(key, e);
                            }
                            
                        }
                        i++;
                    }
                }
            }
		}
		this.individual = genes;
        this.calcCommunities();
        this.calcFitness();
	}   
    /**
     * If the fitness increases with a community change, update the communities.
     * @param edge1 edge that changes community
     * @param edge2 community where edge 1 will be inserted
     * @return true if it updated false if not
     */
    private boolean updateCommunities(Edge edge1, Edge edge2){
        // Copy current communities and code communities
        HashMap<Integer,HashSet<Edge>> uCommunities = new HashMap<>(this.communities);
        HashMap<Integer,HashSet<Node>> uNodeCommunity = new HashMap<>(this.nodeCommunity);

        int community1 = this.edges.get(edge1);
        int community2 = this.edges.get(edge2);
        
        for(Integer i : this.communities.keySet()){
            uCommunities.put(i, new HashSet<Edge>(this.communities.get(i)));
            uNodeCommunity.put(i, new HashSet<Node>(this.nodeCommunity.get(i)));
        }

        // remove edge from its community and add to neighbor community
        boolean deleted = false;
        uCommunities.get(community1).remove(edge1);
        if(uCommunities.get(community1).isEmpty()){
            uCommunities.remove(community1);
            deleted = true;
        }
        uCommunities.get(community2).add(edge1);


        Edge[] edges = edge1.getSourceNode().edges().toArray(Edge[]::new);
        int cyclicNext = 0;
        for(int i = 0; i < edges.length; i++){
            Edge e = edges[cyclicNext];
            if(e != edge1 && this.edges.get(e) == community1){
                break;
            }
            if(i == edges.length-1){
                uNodeCommunity.get(community1).remove(edge1.getSourceNode());
            }
            cyclicNext++;
            if (cyclicNext >= edges.length){
                cyclicNext = 0;
            }
        }
        edges = edge1.getTargetNode().edges().toArray(Edge[]::new);
        cyclicNext = 0;
        for(int i = 0; i < edges.length; i++){
            Edge e = edges[cyclicNext];
            if(e != edge1 && this.edges.get(e) == community1){
                break;
            }
            if(i == edges.length-1){
                uNodeCommunity.get(community1).remove(edge1.getTargetNode());
            }
            cyclicNext++;
            if (cyclicNext >= edges.length){
                cyclicNext = 0;
            }
        }

        HashSet<Node> tmp = uNodeCommunity.get(community1);
        tmp = uNodeCommunity.get(community2);
        tmp.add(edge1.getSourceNode());
        tmp.add(edge1.getTargetNode());

        // if the fitness improves rearrange the genes
        double uFitness = deltaFitness(uCommunities, uNodeCommunity);
        if(uFitness - this.fitness > 0){
            this.edges.put(edge1, community2);
            this.communities = uCommunities;
            this.nodeCommunity = uNodeCommunity;
            this.individual.put(edge1, edge2);
            this.fitness = uFitness;
            if(!deleted){
                rearrange:
                for(Edge e : this.communities.get(community1)){
                    if(this.individual.get(e) == edge1){
                        Edge[] srcNgh = e.getSourceNode().edges().toArray(Edge[]::new);
                        Edge[] trgNgh = e.getTargetNode().edges().toArray(Edge[]::new);

                        if(this.communities.get(community1).size() == 1){
                            this.individual.put(e,e);
                            break;
                        }

                        for(int i = 0; i < srcNgh.length; i++){
                            Edge srcEdge = trgNgh[i];
                            if(this.edges.get(srcEdge) == community1 && srcEdge != e){
                                this.individual.put(e,srcEdge);
                                continue rearrange;
                            }

                        }
                        
                        for(int i = 0; i < trgNgh.length; i++){
                            Edge trgEdge = trgNgh[i];
                            if(this.edges.get(trgEdge) == community1 && trgEdge != e){
                                this.individual.put(e, trgEdge);
                                continue rearrange;
                            }

                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Local Search to improve fitness by checking for every edge if changing
     *  the gene would improve fitness
     */
    public void localSearch(){
        Genes:
        for(Edge key : this.individual.keySet()){
            Edge[] tgtNeighbors = key.getTargetNode().edges().toArray(Edge[]::new);
            Edge[] srcNeighbors = key.getSourceNode().edges().toArray(Edge[]::new);
            // Run through every target edge
            for(int i = 0; i < tgtNeighbors.length; i++){
                Edge cur = tgtNeighbors[i];
                if(edges.get(cur) != edges.get(key)){
                    if(updateCommunities(key, cur)){
                        continue Genes;
                    }
                }

            }

            // Run through every source edge
            for(int i = 0; i < srcNeighbors.length; i++){
                Edge cur = srcNeighbors[i];
                if(edges.get(cur) != edges.get(key)){
                    if(updateCommunities(key, cur)){
                        continue Genes;
                    }
                }

            }
        }

    }
}
