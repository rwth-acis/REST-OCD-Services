package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Edge;
import y.base.Node;
import y.base.EdgeCursor;
import y.base.NodeCursor;

/**
 * An Individual is displayed as a set of edges with the genes and the fitnes of that individual
 */
public class MLinkIndividual {
    
    private HashMap<Edge, Edge> individual;
    private double fitness;
    private ArrayList<ArrayList<Edge>> communities;
    private double edgeNr;

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

    public void calcFitness(){
        double fitness = 0;
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
            fitness += edges*( (edges-(nodeNr - 1.0 )) / ((nodeNr - 2.0) * (nodeNr - 1.0)) );
        }
        this.fitness = (2.0/this.edgeNr)*fitness;
    }
    public void calcCommunities(){
        for(Edge key : this.individual.keySet()){
            if(this.communities.isEmpty()){
                ArrayList<Edge> comm = new ArrayList<Edge>();
                addToCommunity(key, comm);
                this.communities.add(comm);
                continue;
            }
            boolean found = false;
            for(ArrayList<Edge> c : this.communities){
                if(c.contains(key) || c.contains(this.individual.get(key))){
                    addToCommunity(key, c);
                    found = true;
                    break;
                }
            }
            if(!found){
                ArrayList<Edge> comm = new ArrayList<Edge>();
                addToCommunity(key, comm);
                this.communities.add(comm);
            }
            
        }
    }
    private void addToCommunity(Edge key, ArrayList<Edge> comm){
        if(!comm.contains(key)){
            comm.add(key);
        }
        
        Edge geneValue = this.individual.get(key);
        if(!comm.contains(geneValue)){
            comm.add(geneValue);
            addToCommunity(geneValue, comm);
        }
    }
    public void mutation(){
		int mutationProbability = 5;
		HashMap<Edge, Edge> genes = this.individual;
		Random rand = new Random();
		for(Edge key : genes.keySet()){
            Edge gene = genes.get(key);
			if(rand.nextInt(101) < mutationProbability){
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
		}
		this.individual = genes;
	}
}
