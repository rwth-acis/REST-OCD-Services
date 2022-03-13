package i5.las2peer.services.ocd.algorithms.mea;

import i5.las2peer.services.ocd.utils.Pair;
import org.apache.batik.svggen.font.table.GsubTable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Network {

    public int n;
    double[] w;
    double[] s;

    public HashMap<Integer,Double> dp;
    public HashMap<Integer, Double> dn;

    public Link[] e;

    // representation of the network the algorithm uses for execution
    public static Network net = new Network();


    public Network() {
        dp = new HashMap<>();
        dn = new HashMap<>();

    }


    /**
     * @param n     Number of nodes in the network
     * @return      Hashmap with an entry for each node
     */
    public static HashMap<Integer, Double> initializeMap(int n){
        HashMap<Integer, Double> res = new HashMap<Integer, Double>();
        for(int i = 0; i < n; i++){
            res.put(i, 0.0);
        }
        return res;
    }

    public int network_size(){
        return net.n;
    }



    /**
     * get W(i,j) weight of edge from i to j
     * @param i     Node index
     * @param j     Node index
     * @return      Weight between nodes i and j
     */
    public static double weight(int i, int j){
        return net.w[i*(net.n) + j];
    }

    /*similarity between two vertices i & j
     * This is NOT the calculation function
     * just a interface
     */
    public static double similarity(int i, int j){
        return net.s[i*(net.n)+j];
    }

    /*neighbors*/
    public static Link neighbor(int i){
        return net.e[i];
    }




    /**
     * sum of all the vertices of i's neighbor
     * which has positive similarities to i
     * This is NOT the calculation function
     * @param i     Node index
     * @return      Degree_p value for node i
     */
    public static double degree_p(int i){ //TODO: representing dp and dn as HashMap<Integer, Double>
        return net.dp.get(i);
    }

    /**
     * sum of all the vertices of i's neighbor
     * which has positive similarities to i
     * This is NOT the calculation function
     * @param i     Node index
     * @return      Degree_n value for node i
     */
    public static double degree_n(int i){
        return net.dn.get(i);
    }

    public static boolean is_network_ok(){
        return (net!=null);
    }



    /**
     *add a edge from i to j with weight w
     *to network, this function only add a directed
     *link from i to j, but not j to i.
     *if the network is undirected,
     * add_link(i,j,w), add_link(j,i,w);
     * @param i     Node index
     * @param j     Node index
     * @param w     Weight
     */
    public static void add_link(int i, int j, double w){

        Link t = new Link();
        t.to = j;
        t.next = net.e[i];
        net.e[i] = t;

        net.w[i*(net.n) + j ] = w;

    }





    /**
     *this is the function calculates the
     * similarity between vertice i and j
     * @param i     Node index
     * @param j     Node index
     * @return      Double value representing similarity
     */
    public static double sim(int i, int j) {

        Link l;
        double uu = 0;
        double uv = 0;
        double vv = 0;
        double ux;
        double vx;

        /*calculate sum(W(i,x)^2) for all x*/
        for (l = net.e[i]; l != null; l=l.next){
            uu += net.weight(i, l.to) * net.weight(i, l.to);
        }



        /*calculate sum(W(j,x)^2) for all x*/
        for (l = net.e[j]; l != null; l = l.next){
            vv += weight(j, l.to) * weight(j, l.to);
        }


        /*calculate sum(W(i,x)*W(j,x) for all x*/
        for (l = net.e[i]; l != null; l = l.next){
            ux = weight(i, l.to);
            vx = weight(j, l.to);
            if (ux < 0 && vx < 0){
                continue;
            }
            uv += ux * vx;
        }


        return (uv / Math.sqrt(uu) / Math.sqrt(vv));
    }


    //

    /**
     *generate all the similarities between each
     * pair of vertices
     * this function will also calculate degree_p
     * and degree_n at the same time*
     * @return     Array of similarities
     */
    public static double[] gen_sim() {

        double[] s = new double[(net.n * net.n)];
        int i;
        int j;
        double x;
        Link l;

        for (i = 0; i != net.n; ++i){
            //only neighbors have similarity
            for(l = net.e[i]; l != null; l = l.next){
                j = l.to;
                x = sim(i,j);

               s[i*(net.n) + j] = x;

                //calculate degree_n and degree_p
                if (x > 0) {
                    net.dp.put(i, net.dp.get(i) + x);
                } else{
                    net.dn.put(i, net.dn.get(i) + x);
                }
            }
        }

        return s;
    }


    /**
     * Reads in network in pajek format
     * @param name     File name to read
     */
    public static void read_pajek(String name){

        int en = 0; // number of edges;

        Network.net = new Network(); // start with empty network

        try{
            BufferedReader br = new BufferedReader(new FileReader(name));

            String line = br.readLine();

            if (Parameter.verbose >= 1){
                System.out.println("reading network : " + name);
            }

            // read network size from file
            String[] vertices_line = line.split("\t");
            net.n = Integer.parseInt(vertices_line[1]);

            // initialize fields with non null values
            net.dp = initializeMap(net.n);
            net.dn = initializeMap(net.n);
            net.e = new Link[net.n];
            net.w = new double[(net.n*net.n)];

            if (Parameter.verbose >= 2){
                System.out.println("vertices : " + net.n);
            }

            line = br.readLine(); // skip Edges line

            while((line = br.readLine()) != null){
                if(line.length() >0) {
                    en++;
                    String[] line_array = line.split("\t");
                    int i = Integer.parseInt(line_array[0]); // from
                    int j = Integer.parseInt(line_array[1]); // to
                    double w = Double.parseDouble(line_array[2]); // weight

                    //System.out.println("adding edge between " + (i-1) + " and " + (j-1));
                    add_link(i - 1, j - 1, w);
                    add_link(j - 1, i - 1, w); // undirect
                }
            }

            if (Parameter.verbose >= 2){
                System.out.println("edges : " + en);
            }

            // generate similarity
            if (Parameter.verbose >= 3){
                System.out.println("generating similarities");
            }

            net.s = gen_sim();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
