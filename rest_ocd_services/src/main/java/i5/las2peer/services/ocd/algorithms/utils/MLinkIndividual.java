package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import y.base.Edge;
import y.base.Node;
import y.base.EdgeCursor;

/**
 * An Individual is displayed as a set of edges with the genes and the fitnes of that individual
 */
public class MLinkIndividual {
    
    private HashMap<Edge, Edge> individual;
    private double fitness;
    private ArrayList<ArrayList<Edge>> communities;
    private double edgeNr;

    // Constructors
    public MLinkIndividual(){
        individual = new HashMap<Edge, Edge>();
        communities = new ArrayList<ArrayList<Edge>>();
    }
    public MLinkIndividual(HashMap<Edge, Edge> individual){
        this.individual = new HashMap<Edge, Edge>();
        this.communities = new ArrayList<ArrayList<Edge>>();
        this.individual = individual;
        this.edgeNr = individual.size();
        calcCommunities();
        calcFitness();
    }
    // Getter and Setter
    public HashMap<Edge, Edge> getIndividual(){
        return individual;
    }
    public void addGene(Edge locus, Edge geneValue){
        this.individual.put(locus, geneValue);
    }
    public double getFitness(){
        return fitness;
    }
    public void setIndividual(HashMap<Edge, Edge> individual){
        this.individual = individual;
    }
    public ArrayList<ArrayList<Edge>> getCommunities(){
        return this.communities;
    }

    // Public methods for Individuals
    /**
     * Claculates fitness
     */
    public void calcFitness(){
        double fit = 0;
        double nodeNr;
        ArrayList<Node> tmp = new ArrayList<Node>();
        for(int i = 0; i < this.communities.size(); i++){
            tmp.clear();
            double edges = this.communities.get(i).size();
            for(int j = 0; j < edges; j++){
                Node source = this.communities.get(i).get(j).source();
                Node target = this.communities.get(i).get(j).target();
                if(!(tmp.contains(source))){
                    tmp.add(source);
                }
                if(!(tmp.contains(target))){
                    tmp.add(target);
                }
            }
            nodeNr = tmp.size();
            if(nodeNr < 3){
                fit += 0;
            } else {
                fit += edges*( (edges-(nodeNr - 1.0 )) / ((nodeNr - 2.0) * (nodeNr - 1.0)) );
            }
            
        }
        this.fitness = (2.0/this.edgeNr)*fit;
    }
    /**
     * Saves the communities as ArrayLists
     */
    public void calcCommunities(){
        this.communities = new ArrayList<ArrayList<Edge>>();
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
        for(HashSet<Edge> comm : communitiesSet){
            this.communities.add(new ArrayList<Edge>(comm));
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
            int edge = new Random().nextInt(indSize);
            int i = 0;
            for(Edge e : neighbors){
                if(i == edge){
                    genes.put(key, e);
                }
                i++;
            }
		}
		this.individual = genes;
        this.calcCommunities();
        this.calcFitness();
	}   
    public void localSearch(){
        Genes:
        for(Edge key : this.individual.keySet()){
            Edge originalGene = this.individual.get(key);
            double originalFitness = this.fitness;
            EdgeCursor tgtNeighbors = key.target().edges();
            EdgeCursor srcNeighbors = key.source().edges();
            for(int i = 0; i < tgtNeighbors.size(); i++){
                Edge cur = tgtNeighbors.edge();
                if(cur != originalGene){
                    this.individual.put(key, cur);
                    this.calcCommunities();
                    this.calcFitness();
                    if(this.fitness > originalFitness){
                        continue Genes;
                    }
                }
                tgtNeighbors.cyclicNext();
            }
            for(int i = 0; i < srcNeighbors.size(); i++){
                Edge cur = srcNeighbors.edge();
                if(cur != originalGene){
                    this.individual.put(key, cur);
                    this.calcCommunities();
                    this.calcFitness();
                    if(this.fitness > originalFitness){
                        continue Genes;
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
