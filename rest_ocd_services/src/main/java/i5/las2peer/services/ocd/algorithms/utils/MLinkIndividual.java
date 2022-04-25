package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.apache.jena.sparql.function.library.e;

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
        this.edges.clear();
        this.nodeCommunity.clear();
        HashSet<HashSet<Edge>> communitiesSet = new HashSet<HashSet<Edge>>();
        for(Edge key : this.individual.keySet()){
            HashSet<Edge> tmp = new HashSet<Edge>();
            tmp.add(key);
            communitiesSet.add(tmp);
        }
        boolean changes = true;
        while(changes){
            changes = false;
            HashSet<Edge> commLocus = null;
            HashSet<Edge> commGene = null;
            for(Edge locus : this.individual.keySet()){
                Edge gene = this.individual.get(locus);
                for(HashSet<Edge> comm : communitiesSet){
                    if(comm.contains(gene)){
                        commGene = comm;
                    }
                    if(comm.contains(locus)){
                        commLocus = comm;
                    }
                    if(commGene != null && commLocus != null){
                        break;
                    }
                }
                if(commGene != commLocus){
                    commGene.addAll(commLocus);
                    communitiesSet.remove(commLocus);
                    changes = true;
                }
                commGene = null;
                commLocus = null;
            }
        }
        int counter = 0;
        for(HashSet<Edge> community : communitiesSet){
            this.communities.put(counter,community);
            this.nodeCommunity.put(counter,new HashSet<Node>());
            for(Edge e : community){
                this.edges.put(e, counter);
                this.nodeCommunity.get(counter).add(e.target());
                this.nodeCommunity.get(counter).add(e.source());
            }
            counter++;
        }
        
    }

    /**
     * Mutation operator to keep the diversity high
     */
    public void mutate(){
		HashMap<Edge, Edge> genes = this.individual;
		for(Edge key : genes.keySet()){
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
                        genes.put(key, e);
                    }
                    i++;
                }
            }
		}
		this.individual = genes;
        this.calcCommunities();
        this.calcFitness();
	}   
    public void localSearch(){
        Genes:
        for(Edge key : this.individual.keySet()){
            System.out.println("####### INSIDE LOCAL ######");
            Edge originalGene = this.individual.get(key);
            double originalFitness = this.fitness;
            EdgeCursor tgtNeighbors = key.target().edges();
            EdgeCursor srcNeighbors = key.source().edges();
            
            for(int i = 0; i < tgtNeighbors.size(); i++){
                Edge cur = tgtNeighbors.edge();
                if(edges.get(cur) != edges.get(key)){
                    if(cur != originalGene){
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
            for(int i = 0; i < srcNeighbors.size(); i++){
                Edge cur = srcNeighbors.edge();
                if(edges.get(cur) != edges.get(key)){
                    if(cur != originalGene){
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
