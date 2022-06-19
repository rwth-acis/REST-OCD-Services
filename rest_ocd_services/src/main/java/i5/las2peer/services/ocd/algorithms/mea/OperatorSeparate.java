package i5.las2peer.services.ocd.algorithms.mea;

import jnr.ffi.annotations.In;

import java.util.HashSet;
import java.util.Iterator;

public class OperatorSeparate {


    /**
     * convert a permutation into a individual
     * @param gene     Input gene array
     * @return         Individual
     */
    public static Individual decode(int[] gene){

        int n = Network.net.network_size();

        Community[] c = new Community[n];
        int cn = 0;

        int i;
        int j;
        double bp;
        double bn;

        c[cn++] = Community.new_community(gene[0]);

        for (i = 1; i != n; ++i){

            //make vertice to a community
            //easy to calc between_p,between_n
            //and tightness_inc, and merge
            c[cn] = Community.new_community(gene[i]);

            for (j = 0; j != cn; ++j){

                bp = Community.between_p(c[j], c[cn]);
                bn = Community.between_n(c[j], c[cn]);

                if (Community.tightness_inc(c[j], c[cn], bp, bn) > 0) {

                    Community.merge(c[j], c[cn], bp, bn);
                    c[cn] = null;
                    break; // so separated

                }
            }
            if ( j == cn){
                cn++;
            }
        }

        Individual ind = new Individual();

        // simulate realloc from c
        Community[] tmp = new Community[cn];
        int sz = Math.min(c.length, cn);
        for (int k = 0; k < sz; k++) {
            tmp[k] = c[k];
        }

        ind.comm = tmp;
        ind.comm_n = cn;
        ind.refcount = 1;

        //genotype of seperated communities is
        //the label of vertices
        ind.gene = new int[n];
        Community.community_to_label(c, ind.gene, cn);

        // evaluate
        Individual.eval_individual(ind);

        return ind;
    }


    /**
     * @param p1     Input individual
     * @param p2     Input individual
     * @return       Individual
     */
    public static Individual crossover(Individual p1, Individual p2){

        // to simulate rand() from c
        java.util.Random rand = new java.util.Random();
        int RAND_MAX = 32767; // based on c code

        int[] gene;
        int node;
        int i;
        int n = Network.net.network_size();

        Community c;
        Link l;

        if (Random.unirand() > Parameter.pc){ // will not crossover

            p1.refcount++; //child is as same as p1
            return p1;

        } else{

            //copy p1's gene
            gene = new int[n];
            for(i = 0; i != n; ++i){
                gene[i] = p1.gene[i];

            }

            // random a point node
            node = rand.nextInt(RAND_MAX) % n;

            c = Community.find_community(node, p2.comm, p2.comm_n);

            // set the labels in p1 of
            // all vertices in the same communities
            // of node in p2 to equal to p2
            for (l = c.head; l != null; l = l.next){

                gene[l.to] = p2.gene[l.to];

            }

            // use new genotype to get an individual
            return Individual.new_individual(gene);
        }
    }


    /**
     /

    /**
     * roulette selection
     * only positive similarities
     * participate in selection
     * if no positive similarity, choose
     * the minimium negative similarity
     * @param i     Input node index
     * @return      Node index
     */
    public static int roulette_neighbor(int i){

       Link l = Network.neighbor(i);
       int min_id = l.to;

        // roulette end value
        double end = Network.degree_n(i) * Random.unirand();
        double s;

        while(l != null){

            s = Network.similarity(i, l.to);

            // update minimum vertice
            if (s < Network.similarity(i, min_id)){
                min_id = l.to;
            }

            // ignore negative similarities
            if (s <= 0){
                l = l.next;
            } else{
                if(end <= s){
                    return l.to;
                } else{
                    end -= s;
                    l = l.next;
                }
            }
        }

        //not return in while,
        //means no positive similarity
        return min_id;
    }

    /**
     * @param ind     Input individual
     * @return        Individual
     */
    public static Individual mutation(Individual ind){

        int n = Network.net.network_size();
        int[] label = new int[n];
        int[] gene = null;
        Community comm;

        if( Random.unirand() > Parameter.pm ){
            return ind;
        }

        // generate labels for find community
        Community.community_to_label(ind.comm, label, ind.comm_n);

        int i;
        int j;
        for (i = 0; i != n; ++i){

            comm = ind.comm[label[i]];

            if (Random.unirand() < Community.tightness(comm)){

                continue;
            }

            if (gene == null){ // do mutation
                //copy parent genotype
                gene = new int[n];
                for (j = 0; j != n; ++j){
                    gene[j] = ind.gene[j];
                }
            }

            j = roulette_neighbor(i);
            gene[i] = gene[j];

        }



        if (gene != null){ // mutation happened

            // simulate set_individual from c
            if (ind != null) {
                ind.refcount--;
                if (ind.refcount <= 0) {
                    ind = null;
                }
            }
            ind = Individual.new_individual(gene);
            ind.refcount++;
        }

        return ind;
    }

}
