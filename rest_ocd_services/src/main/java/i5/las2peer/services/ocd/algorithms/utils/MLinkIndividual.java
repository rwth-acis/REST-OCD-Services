package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.jena.sparql.pfunction.library.assign;

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
                this.nodeCommunity.get(current).add(e.target());
                this.nodeCommunity.get(current).add(e.source());
                Edge neighbor = this.individual.get(e);
                previous.add(e);
                tracing++;
                
                // Assign gene to the locus community
                while(assignCommunity.get(neighbor).equals(-1)){
                    previous.add(neighbor);
                    assignCommunity.put(neighbor,current);

                    this.communities.get(current).add(neighbor);
                    this.nodeCommunity.get(current).add(neighbor.target());
                    this.nodeCommunity.get(current).add(neighbor.source());

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
                        this.nodeCommunity.get(neighborCommunity).add(prev.target());
                        this.nodeCommunity.get(neighborCommunity).add(prev.source());

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
     * Updates the community after the gene is changed
     * @param locus locus of where the change happens
     * @param gene updated gene
     */
    private void updateCommunities(Edge locus, Edge gene){
        int oldCommunity = edges.get(locus);
        int newCommunity = edges.get(gene);
        
        // Put the locus into new community
        this.individual.put(locus, gene);
        this.edges.put(locus, newCommunity);
        this.communities.get(oldCommunity).remove(locus);
        this.communities.get(newCommunity).add(locus);
        this.nodeCommunity.remove(oldCommunity);
        this.nodeCommunity.get(newCommunity).add(locus.source());
        this.nodeCommunity.get(newCommunity).add(locus.target());

        // if the locus was in a community only containing itself, this method is done
        HashSet<Edge> oldEdges = this.communities.remove(oldCommunity);
        if(oldEdges.isEmpty()){
            return;
        }
        
        // Sort community names 
        // TODO: VIELLEICHT NICHT NÖTIG
        int lastCommunity = 0;
        for(Integer i : this.communities.keySet()){
            if(i > lastCommunity){
                lastCommunity = i;
            }            
        }
        HashSet<Edge> tmp = this.communities.remove(lastCommunity);
        this.communities.put(oldCommunity, tmp);
        HashSet<Node> tmpN = this.nodeCommunity.remove(lastCommunity);
        this.nodeCommunity.put(oldCommunity, tmpN);
        for(Edge e : tmp){
            this.edges.put(e,oldCommunity);
        }
    
        // Counter where the new communities are starting
        int starting = this.communities.size();

        HashMap<Edge,Integer> assignCommunity = new HashMap<Edge,Integer>();

        // Assign no community to every community connected to the locus
        for(Edge e : oldEdges){
            assignCommunity.put(e,-1);
        }

        // Backtracking algorithm but only for the old community of the locus
        int current = starting;
        this.communities.put(current, new HashSet<Edge>());
        this.nodeCommunity.put(current, new HashSet<Node>());
        for(Edge e : oldEdges){
            ArrayList<Edge> previous = new ArrayList<Edge>();
            int tracing = 0;

            // If edge is not assigned yet
            if(assignCommunity.get(e).equals(-1)){
                assignCommunity.put(e, current);
                // Fill in different versions of community representation
                this.communities.get(current).add(e);
                this.nodeCommunity.get(current).add(e.target());
                this.nodeCommunity.get(current).add(e.source());

                Edge neighbor = this.individual.get(e);
                previous.add(e);
                tracing++;
                
                // Assign gene to the locus community if genes are not assigned yet
                if(assignCommunity.containsKey(neighbor)){
                    while(assignCommunity.get(neighbor).equals(-1) ){
                        previous.add(neighbor);
                        assignCommunity.put(neighbor,current);
    
                        this.communities.get(current).add(neighbor);
                        this.nodeCommunity.get(current).add(neighbor.target());
                        this.nodeCommunity.get(current).add(neighbor.source());
    
                        neighbor = this.individual.get(neighbor);
                        tracing++;
                        if(!assignCommunity.containsKey(neighbor)){
                            break;
                        }
                    }
                }
                int neighborCommunity;
                if(!assignCommunity.containsKey(neighbor)){
                    neighborCommunity = edges.get(neighbor);
                } else {
                    neighborCommunity = assignCommunity.get(neighbor);
                }
                
                // If gene is in different community -> assign the whole previous community to the gene's community
                if(neighborCommunity != current){
                    tracing = tracing - 1;
                    this.communities.get(current).clear();
                    this.nodeCommunity.get(current).clear();
                    while(tracing >= 0){
                        Edge prev = previous.get(tracing);
                        assignCommunity.put(prev, neighborCommunity);

                        this.communities.get(neighborCommunity).add(prev);
                        this.nodeCommunity.get(neighborCommunity).add(prev.target());
                        this.nodeCommunity.get(neighborCommunity).add(prev.source());

                        tracing = tracing - 1;
                    }
                } else {
                    current++;
                    this.communities.put(current, new HashSet<Edge>());
                    this.nodeCommunity.put(current, new HashSet<Node>());
                }
            }
        }

        this.edges.putAll(assignCommunity);
        this.communities.remove(current);
        this.nodeCommunity.remove(current);

        calcFitness();

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
            if(tgtNeighbors.size() == 1 && srcNeighbors.size() == 1)
            // Run through every target edge
            for(int i = 0; i < tgtNeighbors.size(); i++){
                Edge cur = tgtNeighbors.edge();
                if(edges.get(cur) != edges.get(key)){
                    updateCommunities(key, cur);
                    if(this.fitness > originalFitness){
                        continue Genes;
                    }
                }
                tgtNeighbors.cyclicNext();
            }

            // Run through every source edge
            for(int i = 0; i < srcNeighbors.size(); i++){
                Edge cur = srcNeighbors.edge();
                if(edges.get(cur) != edges.get(key)){
                    updateCommunities(key, cur);
                    if(this.fitness > originalFitness){
                        continue Genes;
                    }
                }
                srcNeighbors.cyclicNext();
            }
            if(edges.get(key) != edges.get(originalGene)){
                updateCommunities(key, originalGene);
            } else {
                this.individual.put(key, originalGene);
                calcCommunities();
                calcFitness();
            }
            
        }

    }
}
