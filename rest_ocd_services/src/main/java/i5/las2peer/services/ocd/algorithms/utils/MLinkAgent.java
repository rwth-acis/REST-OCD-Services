package i5.las2peer.services.ocd.algorithms.utils;

import java.util.ArrayList;
import java.util.Random;
import i5.las2peer.services.ocd.algorithms.utils.MLinkIndividual;


/**
 * An Agent consists of 6 Individuals with 1 Pocket individual which is the best one of the agent
 */
public class MLinkAgent{

    private ArrayList<MLinkIndividual> individuals;
    private MLinkIndividual pocket;

    public MLinkAgent(){
        individuals = new ArrayList<MLinkIndividual>();
        pocket = new MLinkIndividual();
    }
    public MLinkIndividual getPocket(){
        return pocket;
    }
    public void setPocket(MLinkIndividual pocket){
        this.pocket = pocket;
    }

    public ArrayList<MLinkIndividual> getIndividuals(){
        return individuals;
    }

    /**
     *  New Offspring is added to the agent either as the pocket individual or just a normal individual
     * @param individual  New Offspring
     */
    public void addIndividual(MLinkIndividual individual){
        if(this.individuals.isEmpty() || individual.getFitness() > pocket.getFitness()){
            this.pocket = individual;
            individuals.add(0, individual);
        } else {
            individuals.add(1, individual);
        }
        if(this.individuals.size() > 6){
            this.individuals.remove(this.individuals.size()-1);
        }
    }

    public MLinkIndividual getRandomIndividual(){
        return this.individuals.get(new Random().nextInt(6));
    }
}
