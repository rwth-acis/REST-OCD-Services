package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;

import y.base.Edge;
import y.base.Node;
import y.base.EdgeCursor;

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
    /**
     * Saves the communities as ArrayLists
     */
    public void calcCommunities(){
        // Initialize every edge with -1 as community
        HashMap<Edge,Integer> assignCommunity = new HashMap<Edge,Integer>();
        for(Edge e : this.individual.keySet()){
            assignCommunity.put(e,-1);
        }
        // Backtracking algorithm
        int current = 0;
        for(Edge e : this.individual.keySet()){
            ArrayList<Edge> previous = new ArrayList<Edge>();
            int tracing = 0;

            // If edge is not assigned yet
            if(assignCommunity.get(e) == -1){
                assignCommunity.put(e, current);
                Edge neighbor = this.individual.get(e);
                previous.add(e);
                tracing++;
                
                // Assign gene to the locus community
                while(assignCommunity.get(neighbor) == -1){
                    previous.add(neighbor);
                    assignCommunity.put(neighbor,current);
                    neighbor = this.individual.get(neighbor);
                    tracing++;
                }
                int neighborCommunity = assignCommunity.get(neighbor);
                // If gene is in different community -> assign the whole community to the gene's
                if(neighborCommunity != current){
                    tracing = tracing - 1;
                    while(tracing >= 0){
                        assignCommunity.put(previous.get(tracing), neighborCommunity);
                        tracing = tracing - 1;
                    }
                } else {
                    current++;
                }
            }
        }
        this.edges = assignCommunity;
        this.communities.clear();
        this.nodeCommunity.clear();

        // Fill communities and community of nodes
        for(int i = 0; i < current; i++){
            this.communities.put(i, new HashSet<Edge>());
            this.nodeCommunity.put(i, new HashSet<Node>());
        }
        for(Edge e : this.edges.keySet()){
            int comm = edges.get(e);
            this.communities.get(comm).add(e);
            this.nodeCommunity.get(comm).add(e.target());
            this.nodeCommunity.get(comm).add(e.source());
        }
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
                EdgeCursor targetEdges = key.target().edges();
                int nghbSizeTrgt = targetEdges.size();
                for(int i = 0; i < nghbSizeTrgt; i++){
                    if(targetEdges.edge() != key && targetEdges.edge() != gene){
                        neighbors.add(targetEdges.edge());
                    }
                    targetEdges.cyclicNext();
                }
                EdgeCursor srcEdges = key.source().edges();
                int nghbSizeSrc = srcEdges.size();
                for(int i = 0; i < nghbSizeSrc; i++){
                    if(srcEdges.edge() != key && srcEdges.edge() != gene){
                        neighbors.add(srcEdges.edge());
                    }
                    srcEdges.cyclicNext();
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
     * Local Search to improve fitness by checking for every edge if changing
     *  the gene would improve fitness
     */
    public void localSearch(){
        Genes:
        for(Edge key : this.individual.keySet()){
            Edge originalGene = this.individual.get(key);
            double originalFitness = this.fitness;
            EdgeCursor tgtNeighbors = key.target().edges();
            EdgeCursor srcNeighbors = key.source().edges();

            // Run through every target edge
            for(int i = 0; i < tgtNeighbors.size(); i++){
                Edge cur = tgtNeighbors.edge();
                if(edges.get(cur) != edges.get(key)){
                    if(cur != originalGene && cur != null){
                        this.individual.put(key, cur);
                        this.calcCommunities();
                        this.calcFitness();
                        if(this.fitness > originalFitness){
                            continue Genes;
                        }
                    }
                }
                tgtNeighbors.cyclicNext();
            }

            // Run through every source edge
            for(int i = 0; i < srcNeighbors.size(); i++){
                Edge cur = srcNeighbors.edge();
                if(edges.get(cur) != edges.get(key)){
                    if(cur != originalGene && cur != null){
                        this.individual.put(key, cur);
                        this.calcCommunities();
                        this.calcFitness();
                        if(this.fitness > originalFitness){
                            continue Genes;
                        }
                    }
                }
                srcNeighbors.cyclicNext();
            }
            this.individual.put(key, originalGene);
            this.calcCommunities();
            this.calcFitness();
        }

    }
}
