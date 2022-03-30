package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashMap;

/**
 * An Individual is displayed as a set of edges with the genes and the fitnes of that individual
 */
public class MLinkIndividual {
    
    private HashMap<Integer, Integer> individual;
    private int fitness = 0;

    public MLinkIndividual(){

    }
    public MLinkIndividual(HashMap<Integer, Integer> individual){
        this.individual = individual;
    }

    public HashMap<Integer, Integer> getIndividual(){
        return individual;
    }
    public void addGene(int locus, int geneValue){
        this.individual.put(locus, geneValue);
    }
    public void setFitness(){
        //calculate Fitness
    }
    public int getFitness(){
        return fitness;
    }
}
