package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.util;

import java.util.ArrayList;
import java.util.Collections;

import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.FactorsContainer;
/**
 * This class that holds java version of the methods from combinatorics.cpp authored by Andrea Lancichinetti.
 * original C++ code can be found on https://sites.google.com/site/andrealancichinetti/files 
 */
public class Combinatorics {


	public static double log_factorial(int num) {

		double log_result = 0;
		for (int i = 1; i <= num; i++) {
			log_result += Math.log(i);
		}

		return log_result;
	}

	public static double log_combination(int n, int k) {

		if (k == 0) {
			return 0;
		}

		if (n < k) {
			return 0;
		}

		if (n - k < k) {
			k = n - k;
		}

		double log_c = 0;
		for (int i = n - k + 1; i <= n; i++) {
			log_c += Math.log(i);
		}

		for (int i = 1; i <= k; i++) {
			log_c -= Math.log(i);
		}

		return log_c;

	}

	public static double binomial(int n, int x, double p) { // returns the binomial distribution, n trials, x successes, p probability
														

		if (p == 0) {
			if (x == 0) {
				return 1;
			} else {
				return 0;
			}
		}

		if (p >= 1) {
			if (x == n) {
				return 1;
			} else {
				return 0;
			}
		}

		double log_b = 0;
		log_b += log_combination(n, x) + x * Math.log(p) + (n - x) * Math.log(1 - p);
		return (Math.exp(log_b));

	}

	public static ArrayList<Double> binomial_cumulative(int n, double p, ArrayList<Double> cum) { 

		cum.clear();

		double c = 0;
		for (int i = 0; i <= n; i++) {
			c += binomial(n, i, p);
			cum.add(c);

		}

		return cum;

	}

	public static ArrayList<Double> powerlaw(int n, int min, double tau, ArrayList<Double> cumulative) { 

		cumulative.clear();
		double a = 0;

		for (double h = min; h < n + 1; h++) {
			a += Math.pow((1.0 / h), tau);
		}

		double pf = 0;
		for (double i = min; i < n + 1; i++) {

			pf += 1 / a * Math.pow((1.0 / (i)), tau);
			
			
			
			cumulative.add(pf);

		}

		return cumulative;

	}

	public static ArrayList<Double> distribution_from_cumulative(ArrayList<Double> cum, ArrayList<Double> distr) { // cum is the cumulative, distr is set equal to the distribution

		distr.clear();
		double previous = 0;
		for (int i = 0; i < cum.size(); i++) {
			distr.add(cum.get(i) - previous);
			previous = cum.get(i);
		}

		return distr;

	}

	public static ArrayList<Double> cumulative_from_distribution(ArrayList<Double> cum, ArrayList<Double> distr) { // cum is set equal to the cumulative, distr is the distribution

		cum.clear();
		double sum = 0;
		for (int i = 0; i < distr.size(); i++) {
			sum+= distr.get(i);
			cum.add(sum);
		}

		return cum;

	}

	public static double poisson(int x, double mu) {

		return (Math.exp(-mu + x * Math.log(mu) - log_factorial(x)));
	}


	//this method is never called, therefore it's not implemented in the Java version
	public static int shuffle_and_set(int[] due, int dim) { // it sets due as a random sequence of integers from 0 to dim-1

		return 0;
	}

	public static ArrayList<Integer> shuffle_s(ArrayList<Integer> sq) {

		int siz = sq.size();
		if (siz == 0) {
			return sq; // nothing to shuffle if input is empty
		}

		for (int i = 0; i < sq.size(); i++) {

			int random_pos = Random.irand(siz - 1);

			int random_card_ = sq.get(random_pos);

			sq.set(random_pos, sq.get(siz - 1));
			sq.set(siz - 1, random_card_);
			siz--;

		}

		return sq;

	}

	public static double compute_r(int x, int k, int kout, int m) {

		double r = 0;

		for (int i = x; i <= k; i++) {
			r += binomial(k, i, (double) kout / (double) m);
		}

		return r;

	}

	
	public static FactorsContainer add_factors(ArrayList<Double> num, ArrayList<Double> den, int n, int k) {

		if (n < k) {
			return null; 
		}

		if (n - k < k) {
			k = n - k;
		}

		if (k == 0) {
			return null; 
		}

		for (int i = n - k + 1; i <= n; i++) {
			num.add((double) i);
		}

		for (int i = 1; i <= k; i++) {
			den.add((double) i);
		}

		return (new FactorsContainer(num, den)); // returns container class that holds arraylists modified in this method
	}

	public static double compute_hypergeometric(int i, int k, int kout, int m) {

		if (i > k || i > kout || k > m || kout > m) {
			return 0;
		}

		double prod = 1;
		ArrayList<Double> num = new ArrayList<Double>();
		ArrayList<Double> den = new ArrayList<Double>();

		FactorsContainer add_factors_output = add_factors(num, den, kout, i); // added variable to hold 'num' and 'den' values that were modified by add_factors method
		if (add_factors_output == null) {
			return 0;
		}
		num = add_factors_output.getNum(); // overwrite old num value with modified value resulting from add_factors method call
		den = add_factors_output.getDen(); // overwrite old den value with modified value resulting from add_factors method call
		
		add_factors_output = add_factors(num, den, m - kout, k - i);
		if (add_factors_output == null) {
			return 0;
		}
		num = add_factors_output.getNum(); 
		den = add_factors_output.getDen();
		
		add_factors_output = add_factors(den, num, m, k);
		if (add_factors(den, num, m, k) == null) {
			return 0;
		}
		num = add_factors_output.getNum(); 
		den = add_factors_output.getDen();

		Collections.sort(num);
		Collections.sort(den);

		for (int h = 0; h < den.size(); h++) {
			if (den.get(h) <= 0) {
				System.out.println("denominator has zero or less (in the hypergeometric)");
				return 0;
			}
		}

		for (int h = 0; h < num.size(); h++) {
			if (num.get(h) <= 0) {
				System.out.println("numerator has zero or less (in the hypergeometric)");
				return 0;
			}
		}

		for (int ii = 0; ii < num.size(); ii++) { // changed i to ii compared to C++ code, since there was name conflict
			prod = prod * num.get(ii) / den.get(ii);
		}

		return prod;

	}
	
	public static double compute_self_links (int k, int n, int x) {
		
		if (2 * x > k) {
			return 0;
		}

		double prod = log_combination(n / 2, k - x) + log_combination(k - x, x) + (k - 2 * x) * Math.log(2) - log_combination(n, k);

		return Math.exp(prod);

	}

}
