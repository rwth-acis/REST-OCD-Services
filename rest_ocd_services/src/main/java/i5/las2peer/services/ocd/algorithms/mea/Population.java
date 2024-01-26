package i5.las2peer.services.ocd.algorithms.mea;

import i5.las2peer.services.ocd.algorithms.EvolutionaryAlgorithmBasedOnSimilarity;
import org.apache.jena.sparql.exec.http.Params;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Population {

    public int id;
    public Individual ind;
    public double lambda;
    public Population[] neighbor;

    public static double[] ref = new double[2]; //f_*_ref ideal point


    /**
     * @param p     Input population
     */
    public static void population_info(Population p){
        System.out.println(p.id + " " + p.lambda + " f=" + p.ind.obj[0] + "," + p.ind.obj[1] + " #" + p.ind.comm_n );
    }


    /**
     * initialize populations
     * generate popsize population
     */

    /**
     * Initializes population array
     * @return     Population array
     */
    public static Population[] init_population(){

        Population[] pop = new Population[Parameter.popsize];

        int n = Network.net.network_size();
        int i;
        int j;

        // this line is needed to avoid accessing elements out of array bounds in java
        int lambda_size = Math.max(Parameter.popsize,n);

        int[] perm = new int[n];
        double[] lambda = new double[lambda_size];

        // initialize permutation and lambda
        for (i = 0; i != n; ++i){
            perm[i] = i;
            lambda[i] = Random.unirand();
        }

        /**
         * sort lambda
         *as neighbor of a pop is T nearest pops
         *to it in Eculid distance which
         *is sqrt((l1p-l2p)^2 + (l1n-l2n)^2)
         *and l1p+l1n=1,l2p+l2n=1,so it is
         *|l1p-l2p| it means the nearer lambda,
         * the nearer distance, so sort
         * */
        Arrays.sort(lambda);

        // move populated values at the beginning of the lambda array to imitate c code
        if(Parameter.popsize > n) {
            double[] tmp = new double[Parameter.popsize];
            int start_index = Parameter.popsize - n;
            for (int k = 0; k < n; k++) {
                tmp[k] = lambda[start_index + k];

            }

            lambda = tmp;
        }

        for( i = 0; i != Parameter.popsize; ++i){

            // random order of the permutation
            Random.shuffle(perm, n);

            if (pop[i] == null){ // avoid null pointer exception
                pop[i] = new Population();
            }

            pop[i].id = i;

            // decode the permutation to individual
            pop[i].ind = OperatorOverlap.decode(perm);
            pop[i].lambda = lambda[i];

            if (Parameter.verbose >= 2){
                population_info(pop[i]);
            }
        }

        int l;
        int h;

        // set neighbors of pop
        for (i = 0; i != Parameter.popsize; ++i){

            /**
             * as lambda is generated in a uniform
             * so the nearest T pops to pop[i]
             * is approximately pop[i-T/2]~pop[i+T/2]
             * */

            // neighbor [l,h]
            l = i - Parameter.T / 2;
            h = i + Parameter.T / 2;

            // set l, h if one is out of range
            if (l < 0 ){
                l = 0;
                h = Parameter.T - 1;


            }
            if (h >= Parameter.popsize){
                h = Parameter.popsize - 1;
                l = Parameter.popsize - Parameter.T;

            }

            // try to move [l, h] if better
            while ((l > 0) && //move left
                    ((pop[i].lambda - pop[l - 1].lambda) < (pop[h].lambda - pop[i].lambda))) {
                l--;
                h--;
            }

            while ((h < (Parameter.popsize - 1)) //move right
                    && ((pop[i].lambda - pop[l].lambda) < (pop[h + 1]).lambda - pop[i].lambda)) {
                l++;
                h++;
            }

            pop[i].neighbor = Arrays.copyOfRange(pop, l, pop.length);  // this is instead of pop + l in c, due to how arrays work differently in c/java

        }


        // update ideal point
        ref[0] = pop[0].ind.obj[0];
        ref[1] = pop[0].ind.obj[1];



        for (i = 1; i != Parameter.popsize; ++i){
            if (pop[i].ind.obj[0] > ref[0]){
                ref[0] = pop[i].ind.obj[0];
            }
            if (pop[i].ind.obj[1] > ref[1]){
                ref[1] = pop[i].ind.obj[1];
            }
        }


        return pop;
    }



    /**
     * use an individual ind to udpate
     * the neighbors of a pop
     * @param pop     Input population
     * @param ind     Input individual
     */
    public static void update_pop_neighbor(Population pop, Individual ind){

        double f_old;
        double f_new;
        int i;
        double lambda;

        Population[] nb = pop.neighbor;

        for (i = 0; i != Parameter.T; ++i){

            lambda = nb[i].lambda;

            // compare tchebycheff of two ind
            f_old = Individual.tchebycheff(nb[i].ind, ref, lambda);
            f_new = Individual.tchebycheff(ind, ref, lambda);


            if (f_new > f_old){

                // equivalent to free_individual(d) in c code
                if(nb[i].ind != null) {
                    nb[i].ind.refcount--;
                    if (nb[i].ind.refcount <= 0) {
                        nb[i].ind = null;
                    }
                }

                // equivalent to (*d) = s in c code
                nb[i].ind = ind;
                ind.refcount++;

                if (Parameter.verbose >= 2){
                    population_info(nb[i]);
                }
            }
        }

    }


    /**
     * evolution population
     * @param pop     Input population
     */
    public static void evolve_population(Population[] pop){

        int i;
        int j;
        Individual child;

        // to simulate rand() from c
        java.util.Random rand = new java.util.Random();
        int RAND_MAX = 32767; // based on c code

        for (i = 0; i != Parameter.popsize; ++i){

            do {
                j = rand.nextInt(RAND_MAX) % Parameter.popsize;

            } while(j == i);

            child = OperatorOverlap.crossover(pop[i].ind, pop[j].ind);

            child = OperatorOverlap.mutation(child);

            // update ideal point
            if (child.obj[0] > ref[0]){
                ref[0] = child.obj[0];
            }
            if (child.obj[1] > ref[1]){
                ref[1] = child.obj[1];
            }

            // update neighbors
            update_pop_neighbor(pop[i], child);

            child.refcount--;
            if(child.refcount <= 0){
                child = null;
            }

        }
    }

    /**
     * Writes population information to a file
     * @param name    Input file name
     * @param pop     Input population
     */
    public static void dump_pop(String name, Population pop){
        //System.out.println(name + " " + pop.id);
        try {
            FileWriter fileWriter = new FileWriter(EvolutionaryAlgorithmBasedOnSimilarity.DirectoryPath + name);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.printf("#id      %d\n",pop.id);
            printWriter.printf("#lambda  %f\n",pop.lambda);
            printWriter.printf("#pos_in  %f\n",pop.ind.obj[0]);
            printWriter.printf("#neg_out %f\n", pop.ind.obj[1]);
            printWriter.printf("#number  %d\n", pop.ind.comm_n);
            Link p;
            for(int i = 0; i != pop.ind.comm_n; ++i){
                printWriter.printf("#community %d size %d\n",i,pop.ind.comm[i].size);
                p = pop.ind.comm[i].head;
                while(p != null){
                    printWriter.printf("%d\n",p.to);
                    p = p.next;
                }
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Writes population array onto a file
     * @param pop     Input population array
     */
    public static void dump_population(Population[] pop){
        int i;
        String of;

        for ( i = 0; i != Parameter.popsize; ++i){
            if(Parameter.verbose >= 1){
                population_info(pop[i]);
            }
            //generate output filename
            of = String.format("%s.%04d.pop", Parameter.network_file, i);
            //dump
            dump_pop(of, pop[i]);
        }
    }

}
