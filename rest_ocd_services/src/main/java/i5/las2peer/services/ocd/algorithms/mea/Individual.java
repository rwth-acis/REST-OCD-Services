package i5.las2peer.services.ocd.algorithms.mea;

public class Individual {
    public int[] gene; // genotype
    public Community[] comm; // communities
    public int comm_n; // number of communities
    public double[] obj = new double[2]; // f_pos_in, f_neg_out
    public int refcount;

    /**
     * get f_pos_in and f_neg_out of individual
     * @param ind     Input individual
     */
    public static void eval_individual(Individual ind){

        Community[] comm = ind.comm;
        int n = ind.comm_n;

        int i;

        double pin;
        double pout;
        double nin;
        double nout;

        ind.obj[0] = 0.0;
        ind.obj[1] = 0.0;

        for (i = 0; i != n; ++i){
            pin = comm[i].pin;
            nin = comm[i].nin;
            pout = comm[i].pout;
            nout = comm[i].nout;

            /**
             * protected divide
             */
            if ( (pin + pout) == 0) {
                ind.obj[0] += 0.0;
            } else{
                ind.obj[0] += ( (pin / (pin + pout)) );
            }

            if ( (nin + nout) == 0 ) {
                ind.obj[1] += 0.0;
            }else{
                ind.obj[1] += ( (nout / (nin + nout)) );
            }
        }

        ind.obj[0] /= n;
        ind.obj[1] /= n;

    }



    /**
     * new a individual with genotype geno
     * actually it is only used in separate
     * as the genotype here is the label
     * the genotype array will NOT be copied
     * @param gene      Gene array
     * @return
     */
    public static Individual new_individual(int[] gene){

        Individual ind = new Individual();
        ind.gene = gene;
        ind.comm = new Community[Network.net.network_size()];
        ind.comm_n = Community.label_to_community(gene, ind.comm);

        // change array size, equivalent to realloc of c
        Community[] temp = new Community[ind.comm_n];
        int size = Math.min(ind.comm.length, ind.comm_n);
        for (int i = 0; i < size; i++){
            temp[i] = ind.comm[i];
        }
        ind.comm = temp;

        // evaluate individual
        eval_individual(ind);

        ind.refcount = 1;

        return ind;
    }



    /**
     * tchebycheff function
     * max (lambda_pos * |f_pos_in-f_pos_in_ref|,
     * lambda_neg * |f_neg_in-f_neg_out_ref|)
     * @param ind     Input individual
     * @param ref     Input ref array
     * @param lambda  Input lambda value
     * @return        Double value representing function result
     */
    public static double tchebycheff(Individual ind, double[] ref, double lambda){

        double lp = lambda; // lambda_pos
        double ln = 1 - lambda; // lambda_neg as lp+ln===1
        double fp = ind.obj[0]; // f_pos_in
        double fn = ind.obj[1]; // f_neg_out
        double rp = ref[0]; // f_pos_in_ref
        double rn = ref[1]; // f_neg_out_ref

        /**
         * as maximium problem, f* is always greater than f
         * so we need not use abs
         */
        double t1 = lp * (rp - fp);
        double t2 = ln * (rn - fn);

        if (t1 > t2){

            return t1;
        } else{

            return t2;
        }
    }

}
