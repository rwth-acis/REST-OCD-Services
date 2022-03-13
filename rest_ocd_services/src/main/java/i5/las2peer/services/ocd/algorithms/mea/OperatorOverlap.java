package i5.las2peer.services.ocd.algorithms.mea;

import java.lang.reflect.Array;
import java.util.Arrays;

public class OperatorOverlap {


    /**
     * @param gene     Input gene array
     * @return         Individual
     */
    public static Individual decode_no_alloc(int[] gene){

        int n = Network.net.network_size();

        Community[] c = new Community[n];
        int cn = 0; // community number

        c[cn++] = Community.new_community(gene[0]);

        int i;
        int j;
        int flag;
        double bp;
        double bn;

        // insert vertice into communities
        for (i = 1; i != n; ++i){

            flag = 0;
            c[cn] = Community.new_community(gene[i]);

            for (j = 0; j != cn; ++j){

                bp = Community.between_p(c[j], c[cn]);
                bn = Community.between_n(c[j], c[cn]);
                if (Community.tightness_inc(c[j], c[cn], bp, bn) > 0){
                    // Merge but not stop, so overlapping
                    Community.merge(c[j], c[cn], bp, bn);

                    // as c[cn] is merged into c[j]
                    // so make c[cn] a single vertice
                    // community for merge again
                    c[cn] = Community.new_community(gene[i]);
                    flag = 1; // added flag
                }
            }

            //no added, vertice stand alone
            if (flag == 0){
                cn++;
            }else{
                //added but a standalone community remains
                c[cn] =null;
            }
        }

        int size1;
        int size2;
        int size_joint;

        do {

            flag = 0; // joint flag

            // for each two different communities
            for (i = 0; i != cn; ++i){
                for (j = i+1; j != cn; ++j){
                    size1 = c[i].size;
                    size2 = c[j].size;
                    size_joint = Community.joint_size(c[i], c[j]);

                    if ( ((size_joint * 2) > size1) || ((size_joint * 2) > size2)){
                        flag = 1; // joint happened
                        Community.joint(c[i], c[j]);
                        //number of communitis -1
                        //put the last community
                        //to position j as joint(c[i],c[j])
                        c[j] = c[--cn];
                        break; // joint a pair once
                    }
                }

                if (flag == 1){
                    break; // joint a pair once
                }
            }
        } while (flag == 1);

        Individual ind = new Individual();

        // simulate realloc from c
        Community[] tmp = new Community[cn];
        int sz = Math.min(c.length, cn);
        for(int k = 0; k < sz; k++){
            tmp[k] = c[k];
        }

        ind.gene = gene; // use gene directly without copy
        ind.comm = tmp;
        ind.comm_n = cn;
        ind.refcount = 1;

        Individual.eval_individual(ind);

        return ind;
    }


    /**
     * call decode_no_alloc after copy gene
     * @param gene     Input gene array
     * @return         Individual
     */
    public static Individual decode(int[] gene){

        int n = Network.net.network_size();

        // copy first
        int[] g = new int[n];
        while(n-- != 0) {
            g[n] = gene[n];
        }

        return decode_no_alloc(g);
    }

    /**

     */

    /**
     * copy n elements for s to d
     * only if an element is NOT marked by ignore
     * returns the number of elements handled in s
     * when copy is over
     * used in PMX crossover
     * @param d      Input array
     * @param s      Input array
     * @param ignore Input array
     * @param n      Input integer
     * @return       Integer
     */
    public static int copy_with_ignore(int[] d, int[] s, int[] ignore, int n){

         int i = 0;
         int j = 0;

         while (j < n){
             if (ignore[s[i]] == 0){
                 d[j++] = s[i];
             }
             i++;
         }

         return i;
    }

    /**
     * @param p1     Input individual
     * @param p2     Input individual
     * @return       Individual
     */
    public static Individual crossover(Individual p1, Individual p2){

        //if not cross over, child is
        //as same as p1
        if (Random.unirand() > Parameter.pc){
            p1.refcount++;
            return p1;
        }

        int n = Network.net.network_size();

        int[] gene = new int[n];

        int p;
        int q;
        int t;

        //find beginning and ending point
        //for crossover randomly
        //crossover at [p,q)

        // to simulate rand() from c
        java.util.Random rand = new java.util.Random();
        int RAND_MAX = 32767; // based on c code

        p = rand.nextInt(RAND_MAX) % n; // beginning
        q = rand.nextInt(RAND_MAX) % (n-p) + p + 1; // ending

        int i;

        // ignore flags
        // set p2[p,q) to be ignore
        int[] ignore = new int[n];
        for (i = p; i != q; ++i){
            ignore[p2.gene[i]] = 1;
        }

        //copy the left part  : p1[0,p)
        i = copy_with_ignore(gene, p1.gene, ignore, p);
        //copy the right part : p1[q,n)
        int[] geneSubArray = Arrays.copyOfRange(gene, q, gene.length); // simulate gene+q from c code
        int[] ithSubArray = Arrays.copyOfRange(p1.gene, i, p1.gene.length); // simulate p1->gene+i from c code

        copy_with_ignore(geneSubArray, ithSubArray, ignore, (n-q));

        for (int k = q; k < gene.length; k++){
            gene[k] = geneSubArray[k-q];
        }

        // copy the middle part: p2[p,q)
        for (i = p; i != q; ++i){
            gene[i] = p2.gene[i];
        }

        //make child of using gene
        //use decode_no_alloc to avoid copy gene again
        Individual child = decode_no_alloc(gene);

        return child;
    }

    /**
     * @param p     Input individual
     * @return      Individual
     */
    public static Individual mutation(Individual p){

        int i;
        int j;
        int t;
        int n = Network.net.network_size();
        int[] gene = null;

        // for each position in gene
        for (i = 0; i != n; ++i){
            if (Random.unirand() > (Parameter.pm / n)){
                continue;
            }

            // do mutation, copy gene first
            if (gene == null){
                gene = new int[n];
                for (j = 0; j != n; ++j){
                    gene[j] = p.gene[j];
                }
            }

            // to simulate rand() from c
            java.util.Random rand = new java.util.Random();
            int RAND_MAX = 32767; // based on c code

            // find another position j, swap(i,j)
            j = rand.nextInt(RAND_MAX) % n;
            if (i != j){
                t = gene[i];
                gene[i] = gene[j];
                gene[j] = t;
            }
        }

        if (gene != null){ // mutation happened

            // simulate set_individual from c
            if (p != null) {
                p.refcount--;
                if (p.refcount <= 0) {
                    p = null;
                }
            }
            p = decode_no_alloc(gene);
            p.refcount++;
        }

        return p;
    }
}
