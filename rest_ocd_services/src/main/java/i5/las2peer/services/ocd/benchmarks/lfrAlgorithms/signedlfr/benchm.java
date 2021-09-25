package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.signedlfr;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;


import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.BuildSubgraphsContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.EinEoutContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.InternalDegreeAndMembershipContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.CustomMultiMap;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.CustomMultiMapIterator;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.HelperMethods;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.Pair;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.PairComparator;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.ReadWrite;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.util.Combinatorics;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.util.Random;

/**
 * This class is Java implementation of directed networks which is equivalent to signed LFR algorithm 
 * and it is based on the C++ version authored by Andrea Lancichinetti
 * Original C++ code can be found on https://sites.google.com/site/andrealancichinetti/files 
 */
public class benchm {


	// it computes the sum of ArrayList<Integer> (changed from deque<int> in C++ original code)

	public static int deque_int_sum(ArrayList<Integer> a) {

		int s = 0;
		for (int i = 0; i < a.size(); i++) {
			s += a.get(i);
		}

		return s;
	}


	// computes the integral of a power law
	public static double integral(double a, double b) {

		if (Math.abs(a + 1.0) > 1e-10) {
			return (1.0 / (a + 1.0) * Math.pow(b, a + 1.0));

		} else {
			return Math.log(b);
		}

	}

	// returns the average degree of a power law
	public static double average_degree(final double dmax, final double dmin, final double gamma) {
		return (1.0 / (integral(gamma, dmax) - integral(gamma, dmin))) * (integral(gamma + 1, dmax) - integral(gamma + 1, dmin));
	}

	// bisection method to find the inferior limit, in order to have the expected average degree
	public static double solve_dmin(final double dmax, final double dmed, final double gamma) {

		double dmin_l = 1;
		double dmin_r = dmax;
		double average_k1 = average_degree(dmin_r, dmin_l, gamma);
		double average_k2 = dmin_r;

		if ((average_k1 - dmed > 0) || (average_k2 - dmed < 0)) {

			throw new RuntimeException("the average degree is out of range");

		}

		while (Math.abs(average_k1 - dmed) > 1e-7) {

			double temp = average_degree(dmax, ((dmin_r + dmin_l) / 2.0), gamma);
			if ((temp - dmed) * (average_k2 - dmed) > 0) {

				average_k2 = temp;
				dmin_r = ((dmin_r + dmin_l) / 2.0);

			} else {

				average_k1 = temp;
				dmin_l = ((dmin_r + dmin_l) / 2.0);

			}

		}

		return dmin_l;
	}

	// computes the correct (i.e. discrete) average of a power law
	public static double integer_average(int n, int min, double tau) {
		double a = 0;

		for (double h = min; h < n + 1; h++) {
			a += Math.pow((1.0 / h), tau);
		}

		double pf = 0;
		for (double i = min; i < n + 1; i++) {
			pf += 1 / a * Math.pow((1.0 / (i)), tau) * i;
		}

		return pf;
	}

	// this function changes the community sizes merging the smallest communities
	public static ArrayList<Integer> change_community_size(ArrayList<Integer> seq) {

		if (seq.size() <= 2) {
			return null;
		}

		int min1 = 0;
		int min2 = 0;

		for (int i = 0; i < seq.size(); i++) {
			if (seq.get(i) <= seq.get(min1)) {
				min1 = i;
			}
		}

		if (min1 == 0) {
			min2 = 1;
		}

		for (int i = 0; i < seq.size(); i++) {
			if (seq.get(i) <= seq.get(min2) && seq.get(i) > seq.get(min1)) {
				min2 = i;
			}
		}

		seq.set(min1, (seq.get(min1) + seq.get(min2)));

		int c = seq.get(0);
		seq.set(0, seq.get(min2));
		seq.set(min2, c);
		seq.remove(0);

		return seq;
	}

	// this function builds a bipartite network with num_seq and member_numbers which are the degree sequences. in member matrix links of the communities are
	// stored, this means member_matrix has num_seq.size() rows and each row has num_seq[i] elements
	public static ArrayList<ArrayList<Integer>> build_bipartite_network(ArrayList<ArrayList<Integer>> member_matrix, final ArrayList<Integer> member_numbers,
			final ArrayList<Integer> num_seq) {

		ArrayList<TreeSet<Integer>> en_in = new ArrayList<TreeSet<Integer>>(); // this is the Ein of the subgraph (using TreeSet instead of set in C++ code)
		ArrayList<TreeSet<Integer>> en_out = new ArrayList<TreeSet<Integer>>(); // this is the Eout of the subgraph


		for (int i = 0; i < member_numbers.size(); i++) {
			TreeSet<Integer> first = new TreeSet<Integer>();
			en_in.add(first);
		}

		for (int i = 0; i < num_seq.size(); i++) {
			TreeSet<Integer> first = new TreeSet<Integer>();
			en_out.add(first);
		}


		

		CustomMultiMap<Integer> degree_node_out = new CustomMultiMap<Integer>();
		ArrayList<Pair<Integer>> degree_node_in = new ArrayList<Pair<Integer>>();

		for (int i = 0; i < num_seq.size(); i++) {
			degree_node_out.put(new Pair<Integer>(num_seq.get(i), i));
		}

		for (int i = 0; i < member_numbers.size(); i++) {
			degree_node_in.add(new Pair<Integer>(member_numbers.get(i), i));
		}

		Collections.sort(degree_node_in, new PairComparator<Integer>());

		int itlast = degree_node_in.size(); // counter for degree_node_in, instead of iterator, since degree_node_in can be accessed using 'get' with index


		while (itlast > 0) {

			itlast--;

			CustomMultiMapIterator<Integer> itit = degree_node_out.iterator(); // note: this custom iterator goes from last to first index, to imitate C++ code

			
			ArrayList<Pair<Integer>> erasenda = new ArrayList<Pair<Integer>>(); // Using array of pairs instead of iterators to pairs as in C++ code, to deal how iterators work in Java compared to C++

			for (int i = 0; i < degree_node_in.get(itlast).getFirst(); i++) {

				if (itit.hasNext()) {

					Pair<Integer> itit_current = itit.next(); // equivalent to itit-- in C++ code
					Pair<Integer> itlast_current = degree_node_in.get(itlast);

					en_in.get(itlast_current.getSecond()).add(itit_current.getSecond());
					en_out.get(itit_current.getSecond()).add(itlast_current.getSecond());

				
					
					erasenda.add(itit_current);    


				} else {
					return null;
				}
			}



			for (int i = 0; i < erasenda.size(); i++) {

				Pair<Integer> erasenda_current = erasenda.get(i); 

				if (erasenda_current.getFirst() > 1) {
					degree_node_out.put(new Pair<Integer>(erasenda_current.getFirst() - 1, erasenda_current.getSecond()));
				}

				degree_node_out.erase(erasenda_current); // this implementation of 'erase' either takes Pair or Iterator

			}

		}

		// this is to randomize the subgraph --------------------------

		ArrayList<Integer> degree_list = new ArrayList<Integer>();
		for (int kk = 0; kk < member_numbers.size(); kk++) {
			for (int k2 = 0; k2 < member_numbers.get(kk); k2++) {
				degree_list.add(kk);
			}
		}

		for (int run = 0; run < 10; run++) {
			for (int node_a = 0; node_a < num_seq.size(); node_a++) {
				for (int krm = 0; krm < en_out.get(node_a).size(); krm++) {

					int random_mate = degree_list.get(Random.irand(degree_list.size() - 1));

					if (!(en_out.get(node_a).contains(random_mate))) { // equivalent to C++ line: en_out[node_a].find(random_mate)==en_out[node_a].end()

						ArrayList<Integer> external_nodes = new ArrayList<Integer>();
						for (Iterator<Integer> it_est = en_out.get(node_a).iterator(); it_est.hasNext();) {
							external_nodes.add(it_est.next());
						}

						;
						int old_node = external_nodes.get(Random.irand(external_nodes.size() - 1));

						ArrayList<Integer> not_common = new ArrayList<Integer>();
						for (Iterator<Integer> it_est = en_in.get(random_mate).iterator(); it_est.hasNext();) {
							int current = it_est.next(); // added variable to avoid calling '.next()' multiple times
							if (!(en_in.get(old_node).contains(current))) {
								not_common.add(current);
							}
						}

						if (not_common.isEmpty()) {
							break;
						}


						int node_h = not_common.get(Random.irand(not_common.size() - 1));

						en_out.get(node_a).add(random_mate);
						en_out.get(node_a).remove(old_node);

						en_in.get(old_node).add(node_h);
						en_in.get(old_node).remove(node_a);

						en_in.get(random_mate).add(node_a);
						en_in.get(random_mate).remove(node_h);

						en_out.get(node_h).remove(random_mate);
						en_out.get(node_h).add(old_node);
					}

				}
			}

		}


		member_matrix.clear();

		for (int i = 0; i < en_out.size(); i++) {

			ArrayList<Integer> first = new ArrayList<Integer>(); // moved inside loop, compared to original C++ code
			member_matrix.add(first);
			for (Iterator<Integer> its = en_out.get(i).iterator(); its.hasNext();) {
				member_matrix.get(i).add(its.next());
			}

		}

		return member_matrix;
	}

	public static InternalDegreeAndMembershipContainer internal_degree_and_membership(double mixing_parameter, int overlapping_nodes, int max_mem_num, int num_nodes,
			ArrayList<ArrayList<Integer>> member_matrix, boolean excess, boolean defect, ArrayList<Integer> degree_seq_in, ArrayList<Integer> degree_seq_out,
			ArrayList<Integer> num_seq, ArrayList<Integer> internal_degree_seq_in, ArrayList<Integer> internal_degree_seq_out, boolean fixed_range, int nmin, int nmax, double tau2)
			 {


		if (num_nodes < overlapping_nodes) {

			throw new RuntimeException("\"there are more overlapping nodes than nodes in the whole network! Please, decrease the former ones or increase the latter ones \"");
		}

		member_matrix.clear();
		internal_degree_seq_in.clear();

		ArrayList<Double> cumulative = new ArrayList<Double>();

		// it assigns the internal degree to each node ---------------------------------
		int max_degree_actual = 0; // maximum internal degree

		for (int i = 0; i < degree_seq_in.size(); i++) {

			double interno = (1 - mixing_parameter) * degree_seq_in.get(i);
			int int_interno = (int) (interno);

			if (Random.ran4() < (interno - int_interno)) {
				int_interno++;
			}

			if (excess) {

				while ((((double) int_interno) / degree_seq_in.get(i) < (1 - mixing_parameter)) && (int_interno < degree_seq_in.get(i))) {
					int_interno++;
				}
			}

			if (defect) {

				while ((((double) int_interno) / degree_seq_in.get(i) > (1 - mixing_parameter)) && (int_interno > 0)) {
					int_interno--;
				}

			}

			internal_degree_seq_in.add(int_interno);

			if (int_interno > max_degree_actual) {
				max_degree_actual = int_interno;
			}

		}

		for (int i = 0; i < degree_seq_out.size(); i++) {

			double interno = (1 - mixing_parameter) * degree_seq_out.get(i);
			int int_interno = (int) (interno);

			if (Random.ran4() < (interno - int_interno)) {
				int_interno++;
			}

			if (excess) {

				while ((((double) int_interno) / degree_seq_out.get(i) < (1 - mixing_parameter)) && (int_interno < degree_seq_out.get(i))) {
					int_interno++;
				}

			}

			if (defect) {

				while ((((double) int_interno) / degree_seq_out.get(i) > (1 - mixing_parameter)) && (int_interno > 0)) {
					int_interno--;
				}

			}

			internal_degree_seq_out.add(int_interno);

		}

		// it assigns the community size sequence -----------------------------

		cumulative = Combinatorics.powerlaw(nmax, nmin, tau2, cumulative); // variable assignment because input is not pointer to 'cumulative' like in C++

		if (num_seq.isEmpty()) {

			int _num_ = 0;
			if (!fixed_range && (max_degree_actual + 1) > nmin) {

				_num_ = max_degree_actual + 1; // this helps the assignment of the memberships (it assures that at least one module is big enough to host each node)
				num_seq.add(max_degree_actual + 1);

			}


			while (true) {

				int nn = HelperMethods.lower_bound(cumulative, 0, cumulative.size(), Random.ran4()) + nmin; 

				if (nn + _num_ <= num_nodes + overlapping_nodes * (max_mem_num - 1)) {

					num_seq.add(nn);
					_num_ += nn;

				} else {

					break;
				}

			}

			int num_seq_min_index = num_seq.indexOf(Collections.min(num_seq)); // index of min element, to imitate C++ method 'min_element' from original code
			num_seq.set(num_seq_min_index, num_seq.get(num_seq_min_index) + num_nodes + overlapping_nodes * (max_mem_num - 1) - _num_);

		}

		int ncom = num_seq.size();

		ArrayList<Integer> member_numbers = new ArrayList<Integer>();
		for (int i = 0; i < overlapping_nodes; i++) {
			member_numbers.add(max_mem_num);
		}
		for (int i = overlapping_nodes; i < degree_seq_in.size(); i++) {
			member_numbers.add(1);
		}



		member_matrix = build_bipartite_network(member_matrix, member_numbers, num_seq); // overwrite member_matrix with output of build_bipartite_network which includes modifications done to member_matrix within that method (in C++ input was member_matrix address)
		if (member_matrix == null) {
			System.out.println(
					"it seems that the overlapping nodes need more communities that those I provided. Please increase the number of communities or decrease the number of overlapping nodes");
			return null;
		}


		ArrayList<Integer> available = new ArrayList<Integer>();
		for (int i = 0; i < num_nodes; i++) {
			available.add(0);
		}

		for (int i = 0; i < member_matrix.size(); i++) {
			for (int j = 0; j < member_matrix.get(i).size(); j++) {
				available.set((member_matrix.get(i)).get(j), (available.get((member_matrix.get(i)).get(j)) + member_matrix.get(i).size() - 1));
			}
		}

		ArrayList<Integer> available_nodes = new ArrayList<Integer>();
		for (int i = 0; i < num_nodes; i++) {
			available_nodes.add(i);
		}

		ArrayList<Integer> map_nodes = new ArrayList<Integer>(); // in the position i there is the new name of the node i
		for (int i = 0; i < num_nodes; i++) {
			map_nodes.add(0);
		}

		for (int i = degree_seq_in.size() - 1; i >= 0; i--) {

			int degree_here = internal_degree_seq_in.get(i);
			int try_this = Random.irand(available_nodes.size() - 1);

			int kr = 0;

			while (internal_degree_seq_in.get(i) > available.get(available_nodes.get(try_this))) {

				kr++;
				try_this = Random.irand(available_nodes.size() - 1);
				if (kr == 3 * num_nodes) {

					num_seq = change_community_size(num_seq); // overwrite num_seq with output of change_community_size, which in Java is num_seq, in C++ this method input was num_seq address
					if (num_seq == null) {
						
						throw new RuntimeException("***********************\\nERROR: this program needs more than one community to work fine\"");

					}

					System.out.println("it took too long to decide the memberships; I will try to change the community sizes");

					System.out.print("new community sizes");
					for (int j = 0; j < num_seq.size(); j++) {
						System.out.println(num_seq.get(j));
					}

					return (internal_degree_and_membership(mixing_parameter, overlapping_nodes, max_mem_num, num_nodes, member_matrix, excess, defect, degree_seq_in,
							degree_seq_out, num_seq, internal_degree_seq_in, internal_degree_seq_out, fixed_range, nmin, nmax, tau2));

				}
			}


			map_nodes.set(available_nodes.get(try_this), i);

			available_nodes.set(try_this, available_nodes.get(available_nodes.size() - 1));
			available_nodes.remove(available_nodes.size() - 1); // equivalent to pop_back() in C++ code

		}

		for (int i = 0; i < member_matrix.size(); i++) {
			for (int j = 0; j < member_matrix.get(i).size(); j++) {
				member_matrix.get(i).set(j, map_nodes.get(member_matrix.get(i).get(j)));

			}
		}

		for (int i = 0; i < member_matrix.size(); i++) {
			Collections.sort(member_matrix.get(i));
		}

		// return class that holds variables that were modified in this method
		return new InternalDegreeAndMembershipContainer(member_matrix, degree_seq_in, degree_seq_out, num_seq, internal_degree_seq_in, internal_degree_seq_out);
	}

	public static ArrayList<Integer> compute_internal_degree_per_node(int d, int m, ArrayList<Integer> a) {

		// d is the internal degree
		// m is the number of memebership

		a.clear();
		int d_i = d / m;
		for (int i = 0; i < m; i++) {
			a.add(d_i);
		}

		for (int i = 0; i < d % m; i++) {
			a.set(i, (a.get(i) + 1));
		}

		return a;

	}

	public static EinEoutContainer build_subgraph(ArrayList<TreeSet<Integer>> Ein, ArrayList<TreeSet<Integer>> Eout, ArrayList<Integer> nodes, ArrayList<Integer> d_in,
			ArrayList<Integer> d_out) {

		if (d_in.size() < 3) {
			System.out.println("it seems that some communities should have only 2 nodes! This does not make much sense (in my opinion) Please change some parameters!");
			return null;

		}

		// this function is to build a network with the labels stored in nodes and the degree seq in degrees (correspondence is based on the vectorial index)
		// the only complication is that you don't want the nodes to have neighbors they already have


		// labels will be placed in the end
		ArrayList<TreeSet<Integer>> en_in = new ArrayList<TreeSet<Integer>>(); // this is the Ein of the subgraph
		ArrayList<TreeSet<Integer>> en_out = new ArrayList<TreeSet<Integer>>(); // this is the Eout of the subgraph


		for (int i = 0; i < nodes.size(); i++) {
			en_in.add(new TreeSet<>());
			en_out.add(new TreeSet<>());
		}

		CustomMultiMap<Integer> degree_node_out = new CustomMultiMap<Integer>();
		ArrayList<Pair<Integer>> degree_node_in = new ArrayList<Pair<Integer>>();

		for (int i = 0; i < d_out.size(); i++) {
			degree_node_out.put(new Pair<Integer>(d_out.get(i), i));
		}

		ArrayList<Integer> fakes = new ArrayList<Integer>();
		for (int i = 0; i < d_in.size(); i++) {
			fakes.add(i);
		}

		fakes = Combinatorics.shuffle_s(fakes); // Java version of this method returns 'fakes' after it's been modified within the method, therefore old fakes value is overwritten with this new value

		ArrayList<Integer> antifakes = new ArrayList<Integer>(fakes.size());
		for (int i = 0; i < fakes.size(); i++) {
			antifakes.add(0);
		}

		for (int i = 0; i < d_in.size(); i++) {
			if (fakes.get(i) >= antifakes.size()) {
				antifakes.add(fakes.get(i), i); // if index is out of bounds, use 'add' instead of 'set' (to add element instead of replacing at that index)
			} else {
				antifakes.set(fakes.get(i), i);
			}
		}

		for (int i = 0; i < d_in.size(); i++) {
			degree_node_in.add(new Pair<Integer>(d_in.get(i), fakes.get(i)));
		}

		Collections.sort(degree_node_in, new PairComparator<Integer>());

		for (int i = 0; i < d_in.size(); i++) {
			degree_node_in.get(i).setSecond(antifakes.get(degree_node_in.get(i).getSecond()));
		}

		int itlast = degree_node_in.size(); // counter for degree_node_in, instead of iterator, since degree_node_in can be accessed using 'get' with index


		ArrayList<Integer> self_loop = new ArrayList<Integer>();

		int inserted = 0;

		while (itlast > 0) {

			itlast--;

			CustomMultiMapIterator<Integer> itit = degree_node_out.iterator(); // note: this custom iterator goes from last to first index when using 'next()', to imitate C++ code
			
			ArrayList<Pair<Integer>> erasenda = new ArrayList<Pair<Integer>>(); 
			
			for (int i = 0; i < degree_node_in.get(itlast).getFirst(); i++) {

				if (itit.hasNext()) {

					Pair<Integer> itit_current = itit.next(); // equivalent to itit-- in C++ code
					Pair<Integer> itlast_current = degree_node_in.get(itlast);

					if (!(itit_current.getSecond().equals(itlast_current.getSecond()))) { 

						en_in.get(itlast_current.getSecond()).add(itit_current.getSecond());
						en_out.get(itit_current.getSecond()).add(itlast_current.getSecond());
						inserted++;

					} else {
						self_loop.add(itlast_current.getSecond());
					}

					erasenda.add(itit_current);
				
				} else {
					break;
				}
			}

			for (int i = 0; i < erasenda.size(); i++) {

				Pair<Integer> erasenda_current = erasenda.get(i); 

				if (erasenda_current.getFirst() > 1) {
					degree_node_out.put(new Pair<Integer>(erasenda_current.getFirst() - 1, erasenda_current.getSecond()));
				}

				degree_node_out.erase(erasenda_current); // this implementation of 'erase' either takes Pair or Iterator

			}

		}

		ArrayList<Integer> degree_list_in = new ArrayList<Integer>();
		for (int kk = 0; kk < d_in.size(); kk++) {
			for (int k2 = 0; k2 < d_in.get(kk); k2++) {
				degree_list_in.add(kk);
			}
		}

		int not_done = 0;

		for (int i = 0; i < self_loop.size(); i++) {

			int node = self_loop.get(i);

			int stopper = d_in.size() * d_in.size();
			int stop = 0;

			boolean breaker = false;

			while (stop++ < stopper) {

				while (true) {

					int random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
					if (random_mate == node || en_in.get(node).contains(random_mate)) {
						break;
					}

					ArrayList<Integer> not_common = new ArrayList<Integer>();
					for (Iterator<Integer> it_est = en_out.get(random_mate).iterator(); it_est.hasNext();) {
						int current = it_est.next();
						if (!(en_out.get(node).contains(current))) {
							not_common.add(current);
						}
					}

					if (not_common.isEmpty()) {
						break;
					}

					int random_neigh = not_common.get(Random.irand(not_common.size() - 1));

					en_out.get(node).add(random_neigh);
					en_in.get(node).add(random_mate);

					en_in.get(random_neigh).add(node);
					en_in.get(random_neigh).remove(random_mate);

					en_out.get(random_mate).add(node);
					en_out.get(random_mate).remove(random_neigh);

					breaker = true;
					break;

				}

				if (breaker) {
					break;
				}
			}

			if (!breaker) {
				not_done++;
			}

		}

		// this is to randomize the subgraph -------------------------------------------------------------------
		for (int run = 0; run < 10; run++) {
			for (int node_a = 0; node_a < d_in.size(); node_a++) {
				for (int krm = 0; krm < en_out.get(node_a).size(); krm++) {

					int random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
					while (random_mate == node_a) {
						random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
					}

					if (!(en_out.get(node_a).contains(random_mate))) { // equivalent to C++ line: en_out[node_a].find(random_mate)==en_out[node_a].end()


						ArrayList<Integer> external_nodes = new ArrayList<Integer>();
						for (Iterator<Integer> it_est = en_out.get(node_a).iterator(); it_est.hasNext();) {
							external_nodes.add(it_est.next());
						}

						int old_node = external_nodes.get(Random.irand(external_nodes.size() - 1));

						ArrayList<Integer> not_common = new ArrayList<Integer>();
						for (Iterator<Integer> it_est = en_in.get(random_mate).iterator(); it_est.hasNext();) {
							int current = it_est.next(); // added line to avoid calling '.next()' multiple times
							if ((old_node != current) && !(en_in.get(old_node).contains(current))) {
								not_common.add(current);
							}
						}

						if (not_common.isEmpty()) {
							break;
						}

						int node_h = not_common.get(Random.irand(not_common.size() - 1));

						en_out.get(node_a).add(random_mate);
						en_out.get(node_a).remove(old_node);

						en_in.get(old_node).add(node_h);
						en_in.get(old_node).remove(node_a);

						en_in.get(random_mate).add(node_a);
						en_in.get(random_mate).remove(node_h);

						en_out.get(node_h).remove(random_mate);
						en_out.get(node_h).add(old_node);
					}
				}
			}

		}

		// now I try to insert the new links into the already done network. If some multiple links come out, I try to rewire them

		ArrayList<Pair<Integer>> multiple_edge = new ArrayList<Pair<Integer>>();
		for (int i = 0; i < en_in.size(); i++) {

			for (Iterator<Integer> its = en_in.get(i).iterator(); its.hasNext();) {

				int current = its.next(); // added line to avoid calling '.next()' multiple times

				boolean already = !((Ein.get(nodes.get(i))).add(nodes.get(current))); // true if the insertion didn't take place
				if (already) {
					multiple_edge.add(new Pair<Integer>(nodes.get(i), nodes.get(current)));
				} else {
					Eout.get(nodes.get(current)).add(nodes.get(i));
				}
			}
		}



		for (int i = 0; i < multiple_edge.size(); i++) {

			int a = multiple_edge.get(i).getFirst();
			int b = multiple_edge.get(i).getSecond();

			// now, I'll try to rewire this multiple link among the nodes stored in nodes.
			int stopper_ml = 0;

			while (true) {

				stopper_ml++;

				int random_mate = nodes.get(degree_list_in.get(Random.irand(degree_list_in.size() - 1)));
				while (random_mate == a || random_mate == b) {
					random_mate = nodes.get(degree_list_in.get(Random.irand(degree_list_in.size() - 1)));
				}

				if (!(Ein.get(a).contains(random_mate))) { // equivalent to C++ line of 'Ein[a].find(random_mate) == Ein[a].end()' in original code

					ArrayList<Integer> not_common = new ArrayList<Integer>();
					for (Iterator<Integer> it_est = Eout.get(random_mate).iterator(); it_est.hasNext();) {

						int current = it_est.next(); // added variable to avoid calling '.next()' multiple times
						Collections.sort(nodes); // sort is needed before doing binary search in the next line
						if ((b != current) && (!(Eout.get(b).contains(current))) && (Collections.binarySearch(nodes, current) > 0)) {
							not_common.add(current);
						}
					}

					if (not_common.size() > 0) {

						int node_h = not_common.get(Random.irand(not_common.size() - 1));

						Eout.get(random_mate).add(a);
						Eout.get(random_mate).remove(node_h);

						Ein.get(node_h).remove(random_mate);
						Ein.get(node_h).add(b);

						Eout.get(b).add(node_h);
						Ein.get(a).add(random_mate);

						break;
					}
				}

				if (stopper_ml == 2 * Ein.size()) {
					System.out.println("sorry, I need to change the degree distribution a little bit (one less link)");
					break;
				}

			}

		}

		// returns container class that holds variables that were modified in this method
		return new EinEoutContainer(Ein, Eout);
	}

	public static BuildSubgraphsContainer build_subgraphs(ArrayList<TreeSet<Integer>> Ein, ArrayList<TreeSet<Integer>> Eout, ArrayList<ArrayList<Integer>> member_matrix,
			ArrayList<ArrayList<Integer>> member_list, ArrayList<ArrayList<Integer>> link_list_in, ArrayList<ArrayList<Integer>> link_list_out,
			ArrayList<Integer> internal_degree_seq_in, ArrayList<Integer> degree_seq_in, ArrayList<Integer> internal_degree_seq_out, ArrayList<Integer> degree_seq_out,
			boolean excess, boolean defect)  {

		Ein.clear();
		Eout.clear();
		member_list.clear();
		link_list_in.clear();
		link_list_out.clear();

		int num_nodes = degree_seq_in.size();

		{
			for (int i = 0; i < num_nodes; i++) {
				ArrayList<Integer> first = new ArrayList<Integer>(); // moved inside the loop compared to C++ original code
				member_list.add(first);
			}
		}

		for (int i = 0; i < member_matrix.size(); i++) {
			for (int j = 0; j < member_matrix.get(i).size(); j++) {
				member_list.get(member_matrix.get(i).get(j)).add(i);
			}
		}

		for (int i = 0; i < member_list.size(); i++) {

			ArrayList<Integer> liin = new ArrayList<Integer>();
			ArrayList<Integer> liout = new ArrayList<Integer>();

			for (int j = 0; j < member_list.get(i).size(); j++) {

				liin = compute_internal_degree_per_node(internal_degree_seq_in.get(i), member_list.get(i).size(), liin); // unlike C++ version of the code, here output of this method is modified version of liin, hence we overwrite old liin value with new liin value
				liin.add(degree_seq_in.get(i) - internal_degree_seq_in.get(i));
				liout = compute_internal_degree_per_node(internal_degree_seq_out.get(i), member_list.get(i).size(), liout); // same as above comment
				liout.add(degree_seq_out.get(i) - internal_degree_seq_out.get(i));

			}

			link_list_in.add(liin);
			link_list_out.add(liout);
		}

		// ------------------------ this is done to check if the sums of the internal degrees (in and out) are equal. if not, the program will change it in such a way to assure that.

		for (int i = 0; i < member_matrix.size(); i++) {

			int internal_cluster_in = 0;
			int internal_cluster_out = 0;

			for (int j = 0; j < member_matrix.get(i).size(); j++) {

				int right_index = HelperMethods.lower_bound_int(member_list.get(member_matrix.get(i).get(j)), 0, member_list.get(member_matrix.get(i).get(j)).size(), i);
				internal_cluster_in += link_list_in.get(member_matrix.get(i).get(j)).get(right_index);
				internal_cluster_out += link_list_out.get(member_matrix.get(i).get(j)).get(right_index);
			}

			int initial_diff = Math.abs(internal_cluster_in - internal_cluster_out);
			for (int diffloop = 0; diffloop < 3 * initial_diff; diffloop++) {

				if ((internal_cluster_in - internal_cluster_out) == 0) {
					break;
				}

				// if this does not work in a reasonable time the degree sequence will be changed


				for (int j = 0; j < member_matrix.get(i).size(); j++) {

					int random_mate = member_matrix.get(i).get(Random.irand(member_matrix.get(i).size() - 1));
					int right_index = HelperMethods.lower_bound_int(member_list.get(random_mate), 0, member_list.get(random_mate).size(), i);

					if (internal_cluster_in > internal_cluster_out) {

						if ((link_list_out.get(random_mate).get(right_index) < member_matrix.get(i).size() - 1)
								&& (link_list_out.get(random_mate).get(link_list_out.get(random_mate).size() - 1) > 0)) {

							link_list_out.get(random_mate).set(right_index, link_list_out.get(random_mate).get(right_index) + 1); // increase value by 1
							link_list_out.get(random_mate).set(link_list_out.get(random_mate).size() - 1,
									link_list_out.get(random_mate).get(link_list_out.get(random_mate).size() - 1) - 1); // reduce value by 1
							internal_cluster_out++;

							break;
						}

					} else if (link_list_out.get(random_mate).get(right_index) > 0) {
						link_list_out.get(random_mate).set(right_index, link_list_out.get(random_mate).get(right_index) - 1); // reduce value by 1
						link_list_out.get(random_mate).set(link_list_out.get(random_mate).size() - 1,
								link_list_out.get(random_mate).get(link_list_out.get(random_mate).size() - 1) + 1); // increase value by 1
						internal_cluster_out--;

						break;
					}

				}

			}

			for (int diffloop = 0; diffloop < 3 * initial_diff; diffloop++) {

				if ((internal_cluster_in - internal_cluster_out) == 0) {
					break;
				}

				// if this does not work in a reasonable time the degree sequence will be changed

				for (int j = 0; j < member_matrix.get(i).size(); j++) {

					int random_mate = member_matrix.get(i).get(Random.irand(member_matrix.get(i).size() - 1));
					int right_index = HelperMethods.lower_bound_int(member_list.get(random_mate), 0, member_list.get(random_mate).size(), i);

					if (internal_cluster_in > internal_cluster_out) {

						if ((link_list_out.get(random_mate).get(right_index)) < member_matrix.get(i).size() - 1) {

							link_list_out.get(random_mate).set(right_index, link_list_out.get(random_mate).get(right_index) + 1); 
							internal_cluster_out++;

							break;
						}

					} else {

						link_list_out.get(random_mate).set(right_index, link_list_out.get(random_mate).get(right_index) - 1); 
						internal_cluster_out--;

						break;
					}

				}

			}

		}


		// ------------------------ this is done to check if the sums of the internal degrees (in and out) are equal. if not, the program will change it in such a way to assure that.

		{

			for (int i = 0; i < num_nodes; i++) {
				// moved new TreeSet instantiation and assignment inside the loop compared to C++ original code, otherwise semantics are different
				Ein.add(new TreeSet<Integer>());
				Eout.add(new TreeSet<Integer>());
			}
		}



		for (int i = 0; i < member_matrix.size(); i++) {


			ArrayList<Integer> internal_degree_in = new ArrayList<Integer>();
			ArrayList<Integer> internal_degree_out = new ArrayList<Integer>();

			for (int j = 0; j < member_matrix.get(i).size(); j++) {

				int right_index = HelperMethods.lower_bound_int(member_list.get(member_matrix.get(i).get(j)), 0, member_list.get(member_matrix.get(i).get(j)).size(), i);
				internal_degree_in.add(link_list_in.get(member_matrix.get(i).get(j)).get(right_index));
				internal_degree_out.add(link_list_out.get(member_matrix.get(i).get(j)).get(right_index));

			}
			
			EinEoutContainer build_subgraph_output = build_subgraph(Ein, Eout, member_matrix.get(i), internal_degree_in, internal_degree_out); // added variable to hold output of the build_subgraph method (since Java doesn't use pointer inputs like C++ does)
			if (build_subgraph_output == null) {
				return null;

			} else {

				// following are added lines that overwrite variables that were modified by build_subgraph method, with the modified values (in C++ version of the code, pointers were given to the method as input)
				Ein = build_subgraph_output.getEin();
				Eout = build_subgraph_output.getEout();
				// ----------------------------------------------
			}

		}


	
		// returns container class that holds input variables that were modified
		return new BuildSubgraphsContainer(Ein, Eout, member_list, link_list_in, link_list_out);
	}


	public static boolean they_are_mate(int a, int b, ArrayList<ArrayList<Integer>> member_list) {

		for (int i = 0; i < member_list.get(a).size(); i++) {

			Collections.sort(member_list.get(b)); // need to sort before binary search, to avoid undefined result
			if (Collections.binarySearch(member_list.get(b), member_list.get(a).get(i)) >= 0) {
				return true;
			}

		}

		return false;
	}

	public static int compute_var_mate(ArrayList<TreeSet<Integer>> en_in, ArrayList<ArrayList<Integer>> member_list) {

		int var_mate = 0;
		for (int i = 0; i < en_in.size(); i++) {

			for (Iterator<Integer> itss = en_in.get(i).iterator(); itss.hasNext();) {
				if (they_are_mate(i, itss.next(), member_list)) {
					var_mate++;
				}
			}
		}
		return var_mate;
	}

	public static EinEoutContainer connect_all_the_parts(ArrayList<TreeSet<Integer>> Ein, ArrayList<TreeSet<Integer>> Eout, ArrayList<ArrayList<Integer>> member_list,
			ArrayList<ArrayList<Integer>> link_list_in, ArrayList<ArrayList<Integer>> link_list_out)  {

		ArrayList<Integer> d_in = new ArrayList<Integer>();
		for (int i = 0; i < link_list_in.size(); i++) {
			d_in.add(link_list_in.get(i).get(link_list_in.get(i).size() - 1));
		}

		ArrayList<Integer> d_out = new ArrayList<Integer>();
		for (int i = 0; i < link_list_out.size(); i++) {
			d_out.add(link_list_out.get(i).get(link_list_out.get(i).size() - 1));
		}

		ArrayList<TreeSet<Integer>> en_in = new ArrayList<TreeSet<Integer>>(); // this is the Ein of the subgraph
		ArrayList<TreeSet<Integer>> en_out = new ArrayList<TreeSet<Integer>>(); // this is the Eout of the subgraph

		{

			for (int i = 0; i < member_list.size(); i++) {
				// moved new TreeSet instantiation and assignment inside the loop compared to C++ original code, otherwise semantics are different
				en_in.add(new TreeSet<Integer>());
				en_out.add(new TreeSet<Integer>());
			}
		}

		CustomMultiMap<Integer> degree_node_out = new CustomMultiMap<Integer>();
		ArrayList<Pair<Integer>> degree_node_in = new ArrayList<Pair<Integer>>();

		for (int i = 0; i < d_out.size(); i++) {
			degree_node_out.put(new Pair<Integer>(d_out.get(i), i));
		}

		ArrayList<Integer> fakes = new ArrayList<Integer>();
		for (int i = 0; i < d_in.size(); i++) {
			fakes.add(i);
		}

		fakes = Combinatorics.shuffle_s(fakes); // Java version of this method returns 'fakes' after it's been modified within the method, therefore old fakes value is overwritten with this new value

		ArrayList<Integer> antifakes = new ArrayList<Integer>(fakes.size());
		for (int i = 0; i < fakes.size(); i++) {
			antifakes.add(0);
		}

		for (int i = 0; i < d_in.size(); i++) {
			if (fakes.get(i) >= antifakes.size()) {
				antifakes.add(fakes.get(i), i); // if index is out of bounds, use 'add' instead of 'set' (to add element instead of replacing at that index)
			} else {
				antifakes.set(fakes.get(i), i);
			}
		}

		for (int i = 0; i < d_in.size(); i++) {
			degree_node_in.add(new Pair<Integer>(d_in.get(i), fakes.get(i)));
		}

		Collections.sort(degree_node_in, new PairComparator<Integer>());

		for (int i = 0; i < d_in.size(); i++) {
			degree_node_in.get(i).setSecond(antifakes.get(degree_node_in.get(i).getSecond()));
		}

		int itlast = degree_node_in.size(); // counter for degree_node_in, instead of iterator, since degree_node_in can be accessed using 'get' with index

		ArrayList<Integer> self_loop = new ArrayList<Integer>();

		while (itlast > 0) {

			itlast--;

			CustomMultiMapIterator<Integer> itit = degree_node_out.iterator(); // note: this custom iterator goes from last to first index, to imitate C++ code
			
			ArrayList<Pair<Integer>> erasenda = new ArrayList<Pair<Integer>>(); 

			for (int i = 0; i < degree_node_in.get(itlast).getFirst(); i++) {

				if (itit.hasNext()) {

					Pair<Integer> itit_current = itit.next(); // variable to avoid calling .next() multiple times
					Pair<Integer> itlast_current = degree_node_in.get(itlast);

					
					if (!(itit_current.getSecond().equals(itlast_current.getSecond()))) {  

						en_in.get(itlast_current.getSecond()).add(itit_current.getSecond());
						en_out.get(itit_current.getSecond()).add(itlast_current.getSecond());

					} else {
						self_loop.add(itlast_current.getSecond());
					}

				
					
					erasenda.add(itit_current);  

				} else {
					break;
				}

			}

			for (int i = 0; i < erasenda.size(); i++) {

				Pair<Integer> erasenda_current = erasenda.get(i); 
				if (erasenda_current.getFirst() > 1) {
					degree_node_out.put(new Pair<Integer>(erasenda_current.getFirst() - 1, erasenda_current.getSecond()));
				}

				degree_node_out.erase(erasenda_current); // this implementation of 'erase' either takes Pair or Iterator
			}

		}

		ArrayList<Integer> degree_list_in = new ArrayList<Integer>();
		for (int kk = 0; kk < d_in.size(); kk++) {
			for (int k2 = 0; k2 < d_in.get(kk); k2++) {
				degree_list_in.add(kk);
			}
		}

		for (int i = 0; i < self_loop.size(); i++) {

			int node = self_loop.get(i);

			int stopper = d_in.size() * d_in.size();
			int stop = 0;

			boolean breaker = false;

			while (stop++ < stopper) {

				while (true) {

					int random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
					if (random_mate == node || en_in.get(node).contains(random_mate)) {
						break;
					}

					ArrayList<Integer> not_common = new ArrayList<Integer>();
					for (Iterator<Integer> it_est = en_out.get(random_mate).iterator(); it_est.hasNext();) {
						int current = it_est.next(); // added variable to avoid calling '.next()' multiple times
						if (!(en_out.get(node).contains(current))) {
							not_common.add(current);
						}
					}

					if (not_common.isEmpty()) {
						break;
					}

					int random_neigh = not_common.get(Random.irand(not_common.size() - 1));

					en_out.get(node).add(random_neigh);
					en_in.get(node).add(random_mate);

					en_in.get(random_neigh).add(node);
					en_in.get(random_neigh).remove(random_mate);

					en_out.get(random_mate).add(node);
					en_out.get(random_mate).remove(random_neigh);

					breaker = true;
					break;

				}

				if (breaker) {
					break;
				}
			}

		}

		// this is to randomize the subgraph -------------------------------------------------------------------

		for (int run = 0; run < 10; run++) {
			for (int node_a = 0; node_a < d_in.size(); node_a++) {
				for (int krm = 0; krm < en_out.get(node_a).size(); krm++) {

					int random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
					while (random_mate == node_a) {
						random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
					}

					if (!(en_out.get(node_a).contains(random_mate))) { // equivalent to C++ line: en_out[node_a].find(random_mate)==en_out[node_a].end()

						ArrayList<Integer> external_nodes = new ArrayList<Integer>();
						for (Iterator<Integer> it_est = en_out.get(node_a).iterator(); it_est.hasNext();) {
							external_nodes.add(it_est.next());
						}

						int old_node = external_nodes.get(Random.irand(external_nodes.size() - 1));

						ArrayList<Integer> not_common = new ArrayList<Integer>();
						for (Iterator<Integer> it_est = en_in.get(random_mate).iterator(); it_est.hasNext();) {
							int current = it_est.next(); // added variable to avoid calling '.next()' multiple times
							if ((old_node != current) && !(en_in.get(old_node).contains(current))) {
								not_common.add(current);
							}
						}

						if (not_common.isEmpty()) {
							break;
						}

						int node_h = not_common.get(Random.irand(not_common.size() - 1));

						en_out.get(node_a).add(random_mate);
						en_out.get(node_a).remove(old_node);

						en_in.get(old_node).add(node_h);
						en_in.get(old_node).remove(node_a);

						en_in.get(random_mate).add(node_a);
						en_in.get(random_mate).remove(node_h);

						en_out.get(node_h).remove(random_mate);
						en_out.get(node_h).add(old_node);
					}
				}
			}

		}

		// now there is a rewiring process to avoid "mate nodes" (nodes with al least one membership in common) to link each other

		int var_mate = compute_var_mate(en_in, member_list);

		int stopper_mate = 0;
		int mate_trooper = 10;

		while (var_mate > 0) {

			int best_var_mate = var_mate;

			for (int a = 0; a < d_in.size(); a++) {
				for (Iterator<Integer> its = en_in.get(a).iterator(); its.hasNext();) {

					int current = its.next(); // added variable to avoid using '.next()' multiple times
					if (they_are_mate(a, current, member_list)) {

						int b = current;
						int stopper_m = 0;

						while (true) {

							stopper_m++;
							int random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
							while (random_mate == a || random_mate == b) {
								random_mate = degree_list_in.get(Random.irand(degree_list_in.size() - 1));
							}

							if (!(they_are_mate(a, random_mate, member_list)) && !(en_in.get(a).contains(random_mate))) {

								ArrayList<Integer> not_common = new ArrayList<Integer>();
								for (Iterator<Integer> it_est = en_out.get(random_mate).iterator(); it_est.hasNext();) {
									int it_est_current = it_est.next(); // added variable to avoid using '.next()' multiple times
									if ((b != it_est_current) && !(en_out.get(b).contains(it_est_current))) {
										not_common.add(it_est_current);
									}
								}

								if (not_common.size() > 0) {

									int node_h = not_common.get(Random.irand(not_common.size() - 1));

									en_out.get(random_mate).remove(node_h);
									en_out.get(random_mate).add(a);

									en_in.get(node_h).remove(random_mate);
									en_in.get(node_h).add(b);

									en_out.get(b).remove(a);
									en_out.get(b).add(node_h);

									en_in.get(a).add(random_mate);
									en_in.get(a).remove(b);

									if (!they_are_mate(b, node_h, member_list)) {
										var_mate--;
									}

									if (they_are_mate(random_mate, node_h, member_list)) {
										var_mate--;
									}

									break;
								}

							}

							if (stopper_m == en_in.get(a).size()) {
								break;
							}
						}

						break; // this break is done because if you erased some link you have to stop this loop (en[i] changed)


					}
				}
			}

			// ************************************************ rewiring
			if (var_mate == best_var_mate) {

				stopper_mate++;

				if (stopper_mate == mate_trooper) {
					break;
				}
			} else {
				stopper_mate = 0;
			}
		}

		for (int i = 0; i < en_in.size(); i++) {

			for (Iterator<Integer> its = en_in.get(i).iterator(); its.hasNext();) {

				int current_its = its.next(); // added variable to avoid calling '.next()' multiple times

				Ein.get(i).add(current_its);
				Eout.get(current_its).add(i);
			}
		}

		// returns container class holding input variables of this method that were modified
		return new EinEoutContainer(Ein, Eout);
	}

	public static int internal_kin(ArrayList<TreeSet<Integer>> Ein, ArrayList<ArrayList<Integer>> member_list, int i) {

		int var_mate2 = 0;
		for (Iterator<Integer> itss = Ein.get(i).iterator(); itss.hasNext();) {
			if (they_are_mate(i, itss.next(), member_list)) {
				var_mate2++;
			}
		}

		return var_mate2;
	}

	public static int internal_kin_only_one(TreeSet<Integer> Ein, ArrayList<Integer> member_matrix_j) { // return the overlap between Ein and member_matrix_j

		int var_mate2 = 0;

		for (Iterator<Integer> itss = Ein.iterator(); itss.hasNext();) {

			if (member_matrix_j.contains(itss.next())) { // 'contains' here is equivalent to binary_search in C++
				var_mate2++;
			}
		}

		return var_mate2;
	}

	public static EinEoutContainer erase_links(ArrayList<TreeSet<Integer>> Ein, ArrayList<TreeSet<Integer>> Eout, ArrayList<ArrayList<Integer>> member_list, boolean excess,
			boolean defect, double mixing_parameter) {

		int num_nodes = member_list.size();

		int eras_add_times = 0;

		if (excess) {

			for (int i = 0; i < num_nodes; i++) {

				while ((Ein.get(i).size() > 1) && ((((double) (internal_kin(Ein, member_list, i))) / Ein.get(i).size()) < (1 - mixing_parameter))) {

					// ---------------------------------------------------------------------------------

					System.out.println("degree sequence changed to respect the option -sup ... ");
					System.out.println(++eras_add_times);

					ArrayList<Integer> deqar = new ArrayList<Integer>();
					for (Iterator<Integer> it_est = Ein.get(i).iterator(); it_est.hasNext();) {
						int it_est_current = it_est.next(); // added variable to avoid calling '.next' multiple times
						if (!(they_are_mate(i, it_est_current, member_list))) {
							deqar.add(it_est_current);
						}
					}

					if (deqar.size() == Ein.get(i).size()) { // this shouldn't happpen...

						System.out.println("sorry, something went wrong: there is a node which does not respect the constraints. (option -sup)");
						return null;

					}

					int random_mate = deqar.get(Random.irand(deqar.size() - 1));

					Ein.get(i).remove(random_mate);
					Eout.get(random_mate).remove(i);
				}
			}
		}

		if (defect) {

			for (int i = 0; i < num_nodes; i++) {
				while ((Ein.get(i).size() < Ein.size()) && ((((double) (internal_kin(Ein, member_list, i))) / Ein.get(i).size()) > (1 - mixing_parameter))) {

					// ---------------------------------------------------------------------------------

					System.out.println("degree sequence changed to respect the option -inf ... ");
					System.out.println(++eras_add_times);

					int stopper_here = num_nodes;
					int stopper_ = 0;

					int random_mate = Random.irand(num_nodes - 1);
					while (((they_are_mate(i, random_mate, member_list)) || Ein.get(i).contains(random_mate)) && (stopper_ < stopper_here)) {

						random_mate = Random.irand(num_nodes - 1);
						stopper_++;

					}

					if (stopper_ == stopper_here) { // this shouldn't happen

						System.out.println("sorry, something went wrong: there is a node which does not respect the constraints. (option -inf)");
						return null;


					}

					Ein.get(i).add(random_mate);
					Eout.get(random_mate).add(i);
				}
			}
		}

		// returns container class holding variables that were given to this method as input and modified within the method
		return new EinEoutContainer(Ein, Eout);
	}


	public static int print_network(ArrayList<TreeSet<Integer>> Ein, ArrayList<TreeSet<Integer>> Eout, ArrayList<ArrayList<Integer>> member_list,
			ArrayList<ArrayList<Integer>> member_matrix, ArrayList<Integer> num_seq) throws IOException {


		int edges = 0;

		int num_nodes = member_list.size();

		ArrayList<Double> double_mixing_in = new ArrayList<Double>();
		for (int i = 0; i < Ein.size(); i++) {
			if (Ein.get(i).size() != 0) {

				double one_minus_mu = ((double) (internal_kin(Ein, member_list, i))) / Ein.get(i).size();

				double_mixing_in.add(Math.abs(1.0 - one_minus_mu));
				edges += Ein.get(i).size();
			}
		}

		ArrayList<Double> double_mixing_out = new ArrayList<Double>();
		for (int i = 0; i < Eout.size(); i++) {
			if (Eout.get(i).size() != 0) {

				double one_minus_mu = ((double) (internal_kin(Eout, member_list, i))) / Eout.get(i).size();

				double_mixing_out.add(Math.abs(1.0 - one_minus_mu));
			}
		}


		double density = 0;
		double sparsity = 0;

		for (int i = 0; i < member_matrix.size(); i++) {

			double media_int = 0;
			double media_est = 0;

			for (int j = 0; j < member_matrix.get(i).size(); j++) {

				double kinj = (double) (internal_kin_only_one(Ein.get(member_matrix.get(i).get(j)), member_matrix.get(i)));
				media_int += kinj;
				media_est += Ein.get(member_matrix.get(i).get(j)).size() - (double) (internal_kin_only_one(Ein.get(member_matrix.get(i).get(j)), member_matrix.get(i)));
			}

			double pair_num = (member_matrix.get(i).size() * (member_matrix.get(i).size() - 1));
			double pair_num_e = ((num_nodes - member_matrix.get(i).size()) * (member_matrix.get(i).size()));


			if (pair_num != 0) {
				density += media_int / pair_num;
			}
			if (pair_num_e != 0) {
				sparsity += media_est / pair_num_e;
			}
		}

		density = density / member_matrix.size();
		sparsity = sparsity / member_matrix.size();


		Files.deleteIfExists(Paths.get("ocd/signedLfr/network.dat")); // deletes network.dat if it already existed, before writing to it
		PrintStream networkStream = new PrintStream("ocd/signedLfr/network.dat"); // PrintStream to write to 'network.dat'
		for (int u = 0; u < Eout.size(); u++) {

			for (Iterator<Integer> itb = Eout.get(u).iterator(); itb.hasNext();) {
				Integer itb_current = itb.next(); // added line to avoid calling 'itb.next()' multiple times

				networkStream.append(Integer.toString((u + 1)));
				networkStream.append(' '); // instead of "\t" in C++ code
				networkStream.append(Integer.toString((itb_current + 1)));
				networkStream.println(); // new line
			}
		}
		networkStream.close();


		Files.deleteIfExists(Paths.get("ocd/signedLfr/community.dat")); // deletes community.dat if it already existed, before writing to it
		PrintStream communityStream = new PrintStream("ocd/signedLfr/community.dat"); // PrintStream where community.dat will be written
		for (int i = 0; i < member_list.size(); i++) {

			communityStream.append(Integer.toString((i + 1)));
			communityStream.append(' ');
			for (int j = 0; j < member_list.get(i).size(); j++) {
				communityStream.append(Integer.toString((member_list.get(i).get(j) + 1)));
				communityStream.append(' ');
			}
			communityStream.println();

		}
		communityStream.close();

		System.out.println("----------------------------------------------------------------------------");
		System.out.println("network of " + num_nodes + " vertices and " + edges + " edges");
		System.out.println("average degree =  " + (double) edges / num_nodes);



		return 0;
	}


	/**
	 * Method from which Signed LFR algorithm is initiated, which is based on
	 * directed benchmark originally written in C++, authored by Andrea
	 * Lancichinetti
	 * 
	 * @param excess,            to produce a benchmark whose distribution of
	 *                           the ratio of external in-degree/total in-degree is
	 *                           superiorly (inferiorly) bounded by the mixing
	 *                           parameter (only for the topology). In other words,
	 *                           if you use one of these options, the mixing
	 *                           parameter is not the average ratio of external
	 *                           degree/total degree (as it used to be) but the
	 *                           maximum (or the minimum) of that distribution. When
	 *                           using one of these options, what the program
	 *                           essentially does is to approximate the external
	 *                           degree always by excess (or by defect) and if
	 *                           necessary to modify the degree distribution.
	 *                           Nevertheless, this last possibility occurs for a
	 *                           few nodes and numerical simulations show that it
	 *                           does not affect the degree distribution
	 *                           appreciably.
	 * @param defect             similar excess, but inferiorly bounded instead of
	 *                           superiorly
	 * @param num_nodes          number of nodes
	 * @param average_k          average degree
	 * @param max_degree         maximum degree
	 * @param tau                exponent for the degree sequence
	 * @param tau2               exponent for the community size distribution
	 * @param mixing_parameter   mixing parameter for the topology
	 * @param overlapping_nodes  number of overlapping nodes
	 * @param overlap_membership number of memberships of overlapping nodes
	 * @param nmin               minimum for the community sizes
	 * @param nmax               maximum for the community sizes
	 * @param fixed_range        To have a random network: using this option will
	 *                           set muw=0, mut=0, and minc=maxc=N, i.e. there will
	 *                           be one only community.
	 * @return                   integer representing execution status
	 */
	public static int directed_network_benchmark(boolean excess, boolean defect, int num_nodes, double average_k, int max_degree, double tau, double tau2, double mixing_parameter,
			int overlapping_nodes, int overlap_membership, int nmin, int nmax, boolean fixed_range){

		
		try {
			Random.srand_file(); // this method is to ensure that different graph is generated each time benchmark method is called (it updates seed)
		} catch (NumberFormatException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		
		// change signs to opposite of what input is, since C++ based implementation of
		// LFR algorithms uses positive t1, t2
		// input, while LFR algorithm from WebOCD used negative inputs
		tau = -tau;
		tau2 = -tau2;
		
		
		// it finds the minimum degree -----------------------------------------------------------------------

		double dmin = solve_dmin(max_degree, average_k, -tau);
		if (dmin == -1) {
			return -1; 
		}

		int min_degree = (int) (dmin);

		double media1 = integer_average(max_degree, min_degree, tau);
		double media2 = integer_average(max_degree, min_degree + 1, tau);

		if (Math.abs(media1 - average_k) > Math.abs(media2 - average_k)) {
			min_degree++;
		}


		// range for the community sizes
		if (!fixed_range) {

			nmax = max_degree;
			nmin = Math.max((int) (min_degree), 3);
			System.out.println("----------------------------------------------");
			System.out.println("community size range automatically set equal to [" + nmin + " , " + nmax + "]");

		}



		// ----------------------------------------------------------------------------------------------------
		// ----------------------------------------------------------------------------------------------------


		ArrayList<Integer> degree_seq_in = new ArrayList<Integer>(); // degree sequence of the nodes (in-links)
		ArrayList<Integer> degree_seq_out = new ArrayList<Integer>(); // degree sequence of the nodes (out-links)
		ArrayList<Double> cumulative = new ArrayList<Double>();
		cumulative = Combinatorics.powerlaw(max_degree, min_degree, tau, cumulative); // variable assignment because input is not pointer to cumulative like in C++

		for (int i = 0; i < num_nodes; i++) {

			int nn = HelperMethods.lower_bound(cumulative, 0, cumulative.size(), Random.ran4()) + min_degree;
			degree_seq_in.add(nn);

		}


		Collections.sort(degree_seq_in);

		int inarcs = deque_int_sum(degree_seq_in);
		degree_seq_out = compute_internal_degree_per_node(inarcs, degree_seq_in.size(), degree_seq_out); // in C++ code, degree_seq_out address was input. In Java method outputs degree_seq_out that was given as input and this output overwrites the previous degree_seq_out value



		ArrayList<ArrayList<Integer>> member_matrix = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> num_seq = new ArrayList<Integer>();
		ArrayList<Integer> internal_degree_seq_in = new ArrayList<Integer>();
		ArrayList<Integer> internal_degree_seq_out = new ArrayList<Integer>();

		// ******************************** internal_degree and membership ***************************************************


		InternalDegreeAndMembershipContainer internal_degree_and_membership_output = internal_degree_and_membership(mixing_parameter, overlapping_nodes, overlap_membership,
				num_nodes, member_matrix, excess, defect, degree_seq_in, degree_seq_out, num_seq, internal_degree_seq_in, internal_degree_seq_out, fixed_range, nmin, nmax, tau2); // added variable to hold output of the internal_degree_and_membership method (since Java doesn't use pointer inputs like C++ does)

		if (internal_degree_and_membership_output == null) {
			return 0; 
		}


		// following are added lines that overwrite variables that were modified by internal_degree_and_membership method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		member_matrix = internal_degree_and_membership_output.getMember_matrix();
		degree_seq_in = internal_degree_and_membership_output.getDegree_seq_in();
		degree_seq_out = internal_degree_and_membership_output.getDegree_seq_out();
		num_seq = internal_degree_and_membership_output.getNum_seq();
		internal_degree_seq_in = internal_degree_and_membership_output.getInternal_degree_seq_in();
		internal_degree_seq_out = internal_degree_and_membership_output.getInternal_degree_seq_out();
		// ------------------------------------------------------------------------------------------------


		ArrayList<TreeSet<Integer>> Ein = new ArrayList<TreeSet<Integer>>(); // Ein is the adjacency matrix written in form of list of edges (in-links)
		ArrayList<TreeSet<Integer>> Eout = new ArrayList<TreeSet<Integer>>(); // Eout is the adjacency matrix written in form of list of edges (out-links)
		ArrayList<ArrayList<Integer>> member_list = new ArrayList<ArrayList<Integer>>(); // row i cointains the memberships of node i
		ArrayList<ArrayList<Integer>> link_list_in = new ArrayList<ArrayList<Integer>>(); // row i cointains degree of the node i respect to member_list[i][j]; there is one more number that is the external degree (in-links)
		ArrayList<ArrayList<Integer>> link_list_out = new ArrayList<ArrayList<Integer>>(); // row i contains degree of the node i respect to member_list[i][j]; there is one more number that is the external degree (out-links)


		System.out.println("building Communities... ");
		BuildSubgraphsContainer build_subgraphs_output = build_subgraphs(Ein, Eout, member_matrix, member_list, link_list_in, link_list_out, internal_degree_seq_in, degree_seq_in,
				internal_degree_seq_out, degree_seq_out, excess, defect); // added variable to hold output of the build_subgraphs method (since Java doesn't use pointer inputs like C++ does)

		if (build_subgraphs_output == null) {
			return -1; 
		}


		// following are added lines that overwrite variables that were modified by build_subgraphs method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		Ein = build_subgraphs_output.getEin();
		Eout = build_subgraphs_output.getEout();
		member_list = build_subgraphs_output.getMember_list();
		link_list_in = build_subgraphs_output.getLink_list_in();
		link_list_out = build_subgraphs_output.getLink_list_out();
		// ------------------------------------------------------------------------------------------------


		System.out.println("connecting communities...");
		EinEoutContainer connect_all_parts_output = connect_all_the_parts(Ein, Eout, member_list, link_list_in, link_list_out);


		// following are added lines that overwrite variables that were modified by connect_all_the_parts method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		Ein = connect_all_parts_output.getEin();
		Eout = connect_all_parts_output.getEout();
		// ------------------------------------------------------------------------------------------------




		EinEoutContainer erase_links_output = erase_links(Ein, Eout, member_list, excess, defect, mixing_parameter);
		if (erase_links_output == null) {
			return -1; 
		}

		// following are added lines that overwrite variables that were modified by erase_links method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		Ein = erase_links_output.getEin();
		Eout = erase_links_output.getEout();
		// ------------------------------------------------------------------------------------------------

		
		
		
		
		try {
			print_network(Ein, Eout, member_list, member_matrix, num_seq);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}



}
