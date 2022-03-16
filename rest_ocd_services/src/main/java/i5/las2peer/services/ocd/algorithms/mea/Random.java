package i5.las2peer.services.ocd.algorithms.mea;

public class Random {

    /**
     * @param p     Input array
     * @param n     Input index
     * @return The shuffled array
     */
    public static int[] shuffle(int[] p, int n) {
        int i;
        int j;
        int t;

        java.util.Random randomno = new java.util.Random();

        for (i = 0; i != n; i++) {

            j = randomno.nextInt(n);

            if (i != j) {
                t = p[i];
                p[i] = p[j];
                p[j] = t;
            }
        }
        return p;
    }

    /**
     * @return     Random double value
     */
    public static double unirand() {

        java.util.Random rand = new java.util.Random();
        int RAND_MAX = 32767; // based on c code

        return (double) rand.nextInt(RAND_MAX) / RAND_MAX;
    }
}
