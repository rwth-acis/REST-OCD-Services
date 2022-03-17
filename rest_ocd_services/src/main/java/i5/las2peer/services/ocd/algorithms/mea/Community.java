package i5.las2peer.services.ocd.algorithms.mea;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Community {

    public static double alpha = 1.0;
    public static Network network = Network.net;
    public ArrayList<Integer> community_nodes = new ArrayList<Integer>(); //added variable to hold node indices that are part of community
    public Link head;
    public Link rear;
    public double pin;
    public double pout;
    public double nin;
    public double nout;
    public int size;


    /**
     * make a community with a vertice i
     * community_t use a HashMap of HashSet to store its vertices
     * @param i Node index
     * @return  New community
     */
    public static Community new_community(int i){

        Community c = new Community();

        Link t = new Link();
        t.to = i;
        t.next = null;
        c.head = t;
        c.rear = t;

        c.pin = 0.0;
        c.nin = 0.0;
        c.pout = network.degree_p(i);
        c.nout = network.degree_n(i);
        c.size = 1;

        return c;
    }

    /**
     * calculate the sum of similarity between
     * two communities c1 and c2
     * between_p returns the sum of all the
     * positive similarities between c1,c2
     * the value is useful in merge two communities
     * */

    /**
     * calculate the sum of similarity between
     * two communities c1 and c2
     * between_p returns the sum of all the
     * positive similarities between c1,c2
     * the value is useful in merge two communities
     * @param c1    Input community
     * @param c2    Input community
     * @return      between_p value
     */
    public static double between_p(Community c1, Community c2){

        double b = 0;
        int i;
        int j;
        Link p1;
        Link p2;
        double similarity_i_j; // helper variable to avoid calculating same thing twice

        for (p1 = c1.head; p1 != null; p1 = p1.next){
            for (p2 = c2.head; p2 != null; p2 = p2.next){
                i = p1.to;
                j = p2.to;
                similarity_i_j = Network.similarity(i,j);
                if (similarity_i_j > 0) { // +
                    b += similarity_i_j;
                }
            }
        }

        return b;

    }


    /**
     *
     * @param c1   Input community
     * @param c2   Input community
     * @return     between_n value
     */
    public static double between_n(Community c1, Community c2){

        double b = 0;
        int i;
        int j;
        Link p1;
        Link p2;
        double similarity_i_j; // helper variable to avoid calculating same thing twice

        for (p1 = c1.head; p1 != null; p1 = p1.next){
            for (p2 = c2.head; p2 != null; p2 = p2.next){
                i = p1.to;
                j = p2.to;
                similarity_i_j = Network.sim(i, j);
                if (similarity_i_j < 0){ // -
                    b += similarity_i_j;
                }
            }
        }

        return b;
    }


    /**
     * merge two communities c1,c2
     * the sum similarity between c1,c2
     * is often calculated before, so
     * we use it as bp(+) bn(-) directly
     * @param c1      Input community
     * @param c2      Input community
     * @param bp      bp value
     * @param bn      bn value
     * @return        c1 as a merge result of c1 and c2
     */
    public static Community merge(Community c1, Community c2, double bp, double bn){

        c1.rear.next = c2.head;
        c1.rear = c2.rear;

        // update pin,pout,nin,nout
        c1.pin += (c2.pin + 2 * bp);
        c1.nin += (c2.nin + 2 * bn);
        c1.pout += (c2.pout - 2 * bp);
        c1.nout += (c2.nout - 2 * bn);

        c1.size += c2.size;

        // make c2 a empty community
        c2 = null;

        return c1;
    }

    /**
     * calculate tightness of community
     * @param c     Input community
     * @return      Tightness value
     */
    public static double tightness(Community c){

        return ((c.pin - c.nin) / Math.pow( (c.pin - c.nin + c.pout), alpha ));

    }

    /**
     *
     * @param c1     Input community
     * @param c2     Input community
     * @param bp     bp value
     * @param bn     bn value
     * @return       Tightness value
     */
    public static double tightness_inc(Community c1, Community c2, double bp, double bn) {

        double t1 = tightness(c1);
        double pin = c1.pin + c2.pin + 2 * bp;
        double nin = c1.nin + c2.nin + 2 * bn;
        double pout = c1.pout + c2.pout - 2 * bp;
        double nout = c1.nout + c2.nout - 2 * bn;

        return ((pin - nin) / (Math.pow((pin - nin + pout), alpha)) - t1);

    }

    /**
     * @param c     Input community
     * @return      Size of input community
     */
    public static int community_size(Community c){
        return c.size;
    }



    /**
     * number of joint vertices in two communities
     * c1 and c2. It is used in overlapping
     *  community detection
     * @param c1     Input community
     * @param c2     Input community
     * @return       Joint size of c1 and c2
     */
    public static int joint_size(Community c1, Community c2) {

        int[] label = new int[Network.net.network_size()];
        Link p;
        int n = 0;

        // mark label if a vertice in c1
        for (p = c1.head; p != null; p = p.next){
            label[p.to] = 1;
        }

        // check label of vertices in c2. If labeled, it is a joint vertices
        for ( p = c2.head; p != null; p = p.next){
            if (label[p.to] == 1){
                n++;
            }
        }

        return n;

    }

    /**
     * @param c1     Input community
     * @param c2     Input community
     * @return       Community
     */
    public static Community joint(Community c1, Community c2){

        int[] label = new int[Network.net.network_size()];
        double bp;
        double bn;
        int i;
        Community t;
        Link p;

        // mark label
        for (p = c1.head; p != null; p = p.next){
            label[p.to] = 1;
        }

        while(c2.head != null){
            p = c2.head;
            c2.head = p.next;
            i = p.to;
            if (label[i] == 0){ // vertice i not in c1

                /**
                 * add vertice i to community c1.
                 *  using new_community here makes
                 * 	a vertice into a single node community
                 * 	so we can use between and merge
                 * 	to merge 2 communities with
                 * 	pin,pout,nin,nout updating
                 */

                t = new_community(i);
                bp = between_p(c1, t);
                bn = between_n(c1, t);
                merge(c1, t, bp, bn);

            }
        }

        return c1;
    }


    /**
     * convert a label array p into communities c
     * and return the number of communitis
     * @param p     Label array
     * @param c     Communities array
     * @return      Number of communities
     */
    public static int label_to_community(int[] p, Community[] c){ //TODO: arraylist<Community> as input, makes sense?

        int n = Network.net.network_size();

        int[] l = new int[n];
        int cn = 0; // number of communities;
        int label;
        double bp;
        double bn;
        int i;
        Community comm;

        for (i = 0; i < n; ++i){
            label = p[i];

            /** new community
             * if will merge to other community, we
             * can use between and merge function
             * if will standalone,
             * it is alread done by this*/
            c[cn] = new_community(i);

            /** l[label]==0 means no vertice with the
             * same label(same community) has been
             * handled. standalone and mark this
             * label as the (cn+1)th community*/
            if (l[label] == 0){

                l[label] = ++cn;
            } else{

                /** there is a community for the
                 * vertices with the same label.
                 * merge to that community*/
                comm = c[ l[label] - 1 ];
                bp = between_p(c[cn], comm);
                bn = between_n(c[cn], comm);
                merge(comm, c[cn], bp, bn);
            }
        }

        return cn;
    }





    /**
     * find a community contains the vertice node
     * in cn communities c.
     * for overlapping communitis, the return community
     * is the first community contains the vertice
     * @param node     Input node
     * @param c        Input community
     * @param cn       Input size
     * @return         Community
     */
    public static Community find_community(int node, Community[] c, int cn){

        int i;
        Link p;

        for (i = 0; i != cn; ++i){
            for (p = c[i].head; p != null; p = p.next){

                if (p.to == node){
                    return c[i];
                }
            }
        }

        return null;
    }

    /**
     * @param c     Community array
     * @param l     Label array
     * @param n     Index n
     * @return      Integer array
     */
    public static int[] community_to_label(Community[] c, int[] l, int n){

        Link p;
        int i;

        for (i = 0; i != n; ++i){
            for (p = c[i].head; p != null; p = p.next){
                l[p.to] = i;
            }
        }

        return l;
    }



}
