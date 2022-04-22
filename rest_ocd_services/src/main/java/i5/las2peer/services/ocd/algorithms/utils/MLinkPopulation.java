package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.algorithms.utils.MLinkAgent;
import java.lang.Math;
import java.util.Random;
import java.util.AbstractMap.SimpleEntry;



/**
 * The Population is displayed as an array where position 0 equals the root of the tree
 * and k*3+1 the first child of the entry k
 */
public class MLinkPopulation {
    
    private MLinkAgent[] tree;
    private double diversity;

    public MLinkPopulation(int treeSize){
        tree = new MLinkAgent[treeSize];
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
    public MLinkAgent getAgent(int index){
        return this.tree[index];
    }
    // public methods
    public double calcDiversity(){
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
        return this.diversity;
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
    /**
     * Selects 2 Parents from the same branch
     * @return Entry with 2 parents
     */
    public SimpleEntry<MLinkIndividual,MLinkIndividual> closeSelect(int index){
        Random rand = new Random();
        // Select Random first parent
        MLinkAgent firstAgent = this.tree[index];
        MLinkIndividual firstParent = firstAgent.getRandomIndividual();
        // Select second parent from the same branch as first parent
        MLinkAgent closeAgent;
        if(index < 4){
            closeAgent = this.tree[rand.nextInt(3)+(index*3)+1];
        } else {
            if(index%3 == 0){
                closeAgent = this.tree[index-1];
            } else {
                closeAgent = this.tree[index+1];
            }
        }
        MLinkIndividual secondParent = closeAgent.getRandomIndividual();

        return new SimpleEntry<MLinkIndividual,MLinkIndividual>(firstParent,secondParent);
    }
    /**
     * 
     * @return 2 Parents from different branch
     */
    public SimpleEntry<MLinkIndividual,MLinkIndividual> farSelect(int index){
        Random rand = new Random();
        // Select Random first parent
        MLinkAgent randomAgent = this.tree[index];
        MLinkIndividual firstParent = randomAgent.getRandomIndividual();
        MLinkAgent farAgent;
        if(index == 0){
            farAgent = this.tree[rand.nextInt(12)+1];
        } else if(index < 4) {
            farAgent = this.tree[(index + rand.nextInt(11))%12 + 1];
        } else {
            farAgent = this.tree[(index + rand.nextInt(7)+4)%12 + 1];
        }
        MLinkIndividual secondParent = farAgent.getRandomIndividual();

        return new SimpleEntry<MLinkIndividual,MLinkIndividual>(firstParent,secondParent);
    }


}

