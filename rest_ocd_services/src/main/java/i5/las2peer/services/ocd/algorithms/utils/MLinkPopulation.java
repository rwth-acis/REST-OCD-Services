package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.algorithms.utils.MLinkAgent;


/**
 * The Population is displayed as an array where position 0 equals the root of the tree
 * and k*3+1 the first child of the entry k
 */
public class MLinkPopulation {
    
    private MLinkAgent[] tree;

    public MLinkPopulation(){
        tree = new MLinkAgent[13];
    }
    public void setTree(MLinkAgent[] tree){
        this.tree = tree;
    }
    public MLinkAgent[] getTree(){
        return this.tree;
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

