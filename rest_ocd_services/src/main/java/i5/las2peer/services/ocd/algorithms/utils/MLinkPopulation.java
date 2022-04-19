package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.algorithms.utils.MLinkAgent;
import java.lang.Math;


/**
 * The Population is displayed as an array where position 0 equals the root of the tree
 * and k*3+1 the first child of the entry k
 */
public class MLinkPopulation {
    
    private MLinkAgent[] tree;
    private double diversity;

    public MLinkPopulation(){
        tree = new MLinkAgent[13];
    }
    // Getter and Setter for MLinkPopulation
    public void setTree(MLinkAgent[] tree){
        this.tree = tree;
    }
    public MLinkAgent[] getTree(){
        return this.tree;
    }
    public double getDiversity(){
        return this.diversity;
    }
    // public methods
    public void calcDiversity(){
        double mean = 0;
        int totalIndividuals = (this.tree.length * this.tree[0].getIndividuals().size());
        double sum = 0;
        for(int i = 0; i < this.tree.length; i++){
            MLinkAgent curAgent = this.tree[i];
            for(int j = 0; j < curAgent.getIndividuals().size(); j++){
                mean += curAgent.getIndividuals().get(j).getFitness()/totalIndividuals;
            }
        }
        for(int i = 0; i < this.tree.length; i++){
            MLinkAgent curAgent = this.tree[i];
            for(int j = 0; j < curAgent.getIndividuals().size(); j++){
                double fitness = curAgent.getIndividuals().get(j).getFitness();
                sum += Math.pow(fitness - mean, 2);
            }
        }
        this.diversity = Math.sqrt(sum/(totalIndividuals - 1));
    }
    public void addAgent(MLinkAgent agent){
        for(int i = 0; i < this.tree.length; i++){
            if(this.tree[i] != null){
                this.tree[i] = agent;
                break;
            }
        }
    }
    /**
     * Swaps up the Agent with the fittest Pocket Individual
     */
    public void swapUp(){
        for(int i = 0; i < 4; i++){
            for(int j = 1; j <= 3; j++){
                if(tree[i].getPocket().getFitness() < tree[i*3+j].getPocket().getFitness()){
                    MLinkAgent temp = tree[i*3+j];
                    tree[i*3+j] = tree[i];
                    tree[i] = temp;
                }
            }
        }
        for(int i = 1; i < 4; i++){
            if(tree[0].getPocket().getFitness() < tree[i].getPocket().getFitness()){
                MLinkAgent temp = tree[0];
                tree[0] = tree[i];
                tree[i] = temp;
            }
        }
    }



}

