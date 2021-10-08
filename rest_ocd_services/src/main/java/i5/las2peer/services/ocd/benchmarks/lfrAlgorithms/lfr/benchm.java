package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.lfr;


import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import java.util.TreeMap;
import java.util.TreeSet;

import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.BuildSubgraphsContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.EinEoutContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.InternalDegreeAndMembershipContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.PropagateContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.PropagateOneTwoContainer;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses.WeightsContainerClass;

import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.HelperMethods;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.util.Combinatorics;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.util.Random;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Edge;
import y.base.Node;

import java.util.Map.Entry;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.Set;

/**
 * This class is Java implementation of weighted, directed networks which is equivalent to LFR algorithm 
 * and it is based on the C++ version authored by Andrea Lancichinetti
 * Original C++ code can be found on https://sites.google.com/site/andrealancichinetti/files 
 */
public class benchm {

	
	public static int print_network(ArrayList<TreeSet<Integer>> Ein, ArrayList<TreeSet<Integer>> Eout, ArrayList<ArrayList<Integer>> member_list,
			ArrayList<ArrayList<Integer>> member_matrix, ArrayList<Integer> num_seq, ArrayList<TreeMap<Integer, Double>> neigh_weigh_in,
			ArrayList<TreeMap<Integer, Double>> neigh_weigh_out, double beta, double mu, double mu0) throws IOException {


		int edges = 0;

		int num_nodes = member_list.size();

		ArrayList<Double> double_mixing_in = new ArrayList<Double>();
		for (int i = 0; i < Ein.size(); i++) {
			if (Ein.get(i).size() != 0) {

				double one_minus_mu = ((double) (dir_benchm.internal_kin(Ein, member_list, i))) / Ein.get(i).size();

				double_mixing_in.add(Math.abs(1.0 - one_minus_mu));
				edges += Ein.get(i).size();
			}
		}

		ArrayList<Double> double_mixing_out = new ArrayList<Double>();
		for (int i = 0; i < Eout.size(); i++) {
			if (Eout.get(i).size() != 0) {

				double one_minus_mu = ((double) (dir_benchm.internal_kin(Eout, member_list, i))) / Eout.get(i).size();

				double_mixing_out.add(Math.abs(1.0 - one_minus_mu));
			}
		}


		double density = 0;
		double sparsity = 0;

		for (int i = 0; i < member_matrix.size(); i++) {

			double media_int = 0;
			double media_est = 0;

			for (int j = 0; j < member_matrix.get(i).size(); j++) {

				double kinj = (double) (dir_benchm.internal_kin_only_one(Ein.get(member_matrix.get(i).get(j)), member_matrix.get(i)));
				media_int += kinj;
				media_est += Ein.get(member_matrix.get(i).get(j)).size() - (double) dir_benchm.internal_kin_only_one(Ein.get(member_matrix.get(i).get(j)), member_matrix.get(i));
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


		Files.deleteIfExists(Paths.get("ocd/lfr/network.dat")); // deletes network.dat if it already existed, before writing to it
		PrintStream networkStream = new PrintStream("ocd/lfr/network.dat"); // PrintStream to write to 'network.dat'
		for (int u = 0; u < Eout.size(); u++) {

			for (Iterator<Integer> itb = Eout.get(u).iterator(); itb.hasNext();) {
				Integer itb_current = itb.next(); // added line to avoid calling 'itb.next()' multiple times

				networkStream.append(Integer.toString((u + 1)));
				networkStream.append(' '); // instead of "\t" in C++ code
				networkStream.append(Integer.toString((itb_current + 1)));
				networkStream.append(' ');
				networkStream.append((neigh_weigh_out.get(u).get(itb_current)).toString());
				networkStream.println(); // new line
			}
		}
		networkStream.close();



		

		Files.deleteIfExists(Paths.get("ocd/lfr/community.dat")); // deletes community.dat if it already existed, before writing to it
		PrintStream communityStream = new PrintStream("ocd/lfr/community.dat"); // PrintStream where community.dat will be written
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

	// this method doesn't seem to be used in C++ code
//	public static int check_weights(ArrayList<TreeMap<Integer, Double>> neigh_weigh_in, ArrayList<TreeMap<Integer, Double>> neigh_weigh_out,
//			ArrayList<ArrayList<Integer>> member_list, ArrayList<ArrayList<Double>> wished, ArrayList<ArrayList<Double>> factual, double tot_var, double[] strs) {
//
//		double d1t = 0;
//		double d2t = 0;
//		double d3t = 0;
//
//		double var_check = 0;
//
//		for (int k = 0; k < neigh_weigh_in.size(); k++) {
//
//			double in_s = 0;
//			double out_s = 0;
//
//			Set<Map.Entry<Integer, Double>> entrySet_in = neigh_weigh_in.get(k).entrySet(); // added variable needed for
//																							// iterating over TreeMap
//																							// entries
//			for (Iterator<Entry<Integer, Double>> itm = entrySet_in.iterator(); itm.hasNext();) {
//
//				Map.Entry<Integer, Double> itm_current = itm.next(); // added variable to avoid calling '.next()'
//																		// multiple times
//
//				if (itm_current.getValue() < 0) { // C++ code 'itm->second' corresponds to Java code
//													// 'itm.next().getValue()'
//					
//					System.out.println("the check failed because of 333");
//					return 0;
//				}
//				if (Math.abs(itm_current.getValue() - neigh_weigh_out.get(itm_current.getKey()).get(k)) > 1e-7) { // C++ code 'itm->first' corresponds to Java code 'itm.next().getKey()'
//																
//
//					System.out.println("the check failed because of 111");
//					
//					return 0;
//				}
//
//				if (dir_benchm.they_are_mate(k, itm_current.getKey(), member_list)) {
//
//					in_s += itm_current.getValue();
//
//				} else {
//					out_s += itm_current.getValue();
//				}
//			}
//
//			Set<Map.Entry<Integer, Double>> entrySet_out = neigh_weigh_out.get(k).entrySet(); // added variable needed for iterating over TreeMap entries
//																								
//																								
//			for (Iterator<Entry<Integer, Double>> itm = entrySet_out.iterator(); itm.hasNext();) {
//
//				Map.Entry<Integer, Double> itm_current = itm.next(); // added variable to avoid calling '.next()' multiple times
//																		
//
//				if (itm_current.getValue() < 0) {
//
//					
//					System.out.println("the check failed because of 333");
//					return 0;
//
//				}
//				if (Math.abs(itm_current.getValue() - neigh_weigh_in.get(itm_current.getKey()).get(k)) > 1e-7) {
//
//					System.out.println("the check failed because of 111");
//				
//					return 0;
//
//				}
//				if (dir_benchm.they_are_mate(k, itm_current.getKey(), member_list)) {
//
//					in_s += itm_current.getValue();
//
//				} else {
//
//					out_s += itm_current.getValue();
//				}
//			}
//
//			if (Math.abs(in_s - factual.get(k).get(0)) > 1e-7) {
//				System.out.println("the check failed because of " + (in_s - factual.get(k).get(0)));
//			}
//
//			if (Math.abs(out_s - factual.get(k).get(1)) > 1e-7) {
//				System.out.println("the check failed because of " + (out_s - factual.get(k).get(1)));
//			}
//
//			// changed strs to array to fit this line (it was address to double in C++)
//			if (Math.abs(in_s + out_s + factual.get(k).get(2) - strs[k]) > 1e-7) {
//				System.out.println("the check failed because of " + (in_s + out_s + factual.get(k).get(2) - strs[k]));
//			}
//
//			double d1 = (in_s - wished.get(k).get(0));
//			double d2 = (out_s - wished.get(k).get(1));
//			double d3 = (strs[k] - in_s - out_s);
//			var_check += d1 * d1 + d2 * d2 + d3 * d3;
//
//			d1t += d1 * d1;
//			d2t += d2 * d2;
//			d3t += d3 * d3;
//
//		}
//
//		System.out.println("tot_var" + tot_var);
//		System.out.println("\tdit " + d1t);
//		System.out.println("\td2t " + d2t);
//		System.out.println("\td3t" + d3t);
//		System.out.println("\td3t" + d3t);
//		if (Math.abs(var_check - tot_var) > 1e-5) {
//			System.out.println("found this difference in check " + (Math.abs(var_check - tot_var)));
//			return 0; 
//		} else {
//			System.out.println("ok: check passed");
//		}
//	
//		return 0;
//	}


	public static PropagateOneTwoContainer propagate_one(ArrayList<TreeMap<Integer, Double>> neighbors_weights, ArrayList<Integer> VE, ArrayList<ArrayList<Integer>> member_list,
			ArrayList<ArrayList<Double>> wished, ArrayList<ArrayList<Double>> factual, int i, double tot_var, double[] strs, ArrayList<Integer> internal_kin_top,
			ArrayList<TreeMap<Integer, Double>> others) {



		double change = factual.get(i).get(2) / VE.get(i);


		double oldpartvar = 0;
		Set<Map.Entry<Integer, Double>> entrySet_neighbors_weights = neighbors_weights.get(i).entrySet(); // added variable for iterator creation
		for (Iterator<Map.Entry<Integer, Double>> itm = entrySet_neighbors_weights.iterator(); itm.hasNext();) {

			
			Map.Entry<Integer, Double> itm_current = itm.next(); // added variable to avoid calling '.next()' multiple times
			if (itm_current.getValue() + change > 0) { // itm_current.getKey() and itm_current.getValue() correspond to itm->first and itm->second in original C++ code
				for (int bw = 0; bw < 3; bw++) {
					
					
					
					oldpartvar += (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw))
							* (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw));
	

				}
			}
		}

		for (int bw = 0; bw < 3; bw++) {
			oldpartvar += (factual.get(i).get(bw) - wished.get(i).get(bw)) * (factual.get(i).get(bw) - wished.get(i).get(bw));
		}



		double newpartvar = 0;
		
			

		for (Iterator<Map.Entry<Integer, Double>> itm = entrySet_neighbors_weights.iterator(); itm.hasNext();) {

			Map.Entry<Integer, Double> itm_current = itm.next(); // added variable to avoid calling '.next()' multiple times
			if (itm_current.getValue() + change > 0) {

				if (dir_benchm.they_are_mate(i, itm_current.getKey(), member_list)) {



					factual.get(itm_current.getKey()).set(0, (factual.get(itm_current.getKey()).get(0) + change));
					factual.get(itm_current.getKey()).set(2, (factual.get(itm_current.getKey()).get(2) - change));

					factual.get(i).set(0, (factual.get(i).get(0) + change));
					factual.get(i).set(2, (factual.get(i).get(2) - change));






				} else {

					factual.get(itm_current.getKey()).set(1, (factual.get(itm_current.getKey()).get(1) + change));
					factual.get(itm_current.getKey()).set(2, (factual.get(itm_current.getKey()).get(2) - change));

					factual.get(i).set(1, (factual.get(i).get(1) + change));
					factual.get(i).set(2, (factual.get(i).get(2) - change));



				}




				for (int bw = 0; bw < 3; bw++) {
					newpartvar += (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw))
							* (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw));
				}

				
				itm_current.setValue(itm_current.getValue() + change); 
				others.get(itm_current.getKey()).replace(i, (others.get(itm_current.getKey()).get(i) + change)); 

			}

		}

		


		for (int bw = 0; bw < 3; bw++) {
			newpartvar += (factual.get(i).get(bw) - wished.get(i).get(bw)) * (factual.get(i).get(bw) - wished.get(i).get(bw));

		}

		
		tot_var += newpartvar - oldpartvar;


		return new PropagateOneTwoContainer(neighbors_weights, wished, factual, tot_var, others);
	}


	public static PropagateOneTwoContainer propagate_two(ArrayList<TreeMap<Integer, Double>> neighbors_weights, ArrayList<Integer> VE, ArrayList<ArrayList<Integer>> member_list,
			ArrayList<ArrayList<Double>> wished, ArrayList<ArrayList<Double>> factual, int i, double tot_var, double[] strs, ArrayList<Integer> internal_kin_top,
			ArrayList<TreeMap<Integer, Double>> others) {
	
		

		int internal_neigh = internal_kin_top.get(i);

		if (internal_neigh != 0) { // in this case I rewire the difference strength

			
			
			double changenn = (factual.get(i).get(0) - wished.get(i).get(0));

			double oldpartvar = 0;

			Set<Map.Entry<Integer, Double>> entrySet_neighbors_weights = neighbors_weights.get(i).entrySet(); // added variable for iterator creation
			for (Iterator<Map.Entry<Integer, Double>> itm = entrySet_neighbors_weights.iterator(); itm.hasNext();) {


		
				
				
				Map.Entry<Integer, Double> itm_current = itm.next(); // added variable not to avoid reusing 'next()'
				if (dir_benchm.they_are_mate(i, itm_current.getKey(), member_list)) {

					double change = changenn / internal_neigh;

					if ((itm_current.getValue() - change) > 0) {

						for (int bw = 0; bw < 3; bw++) {
							oldpartvar += (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw))
									* (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw));
						}
					}

				} else {

					double change = changenn / (VE.get(i) - internal_neigh);

					if ((itm_current.getValue() + change) > 0) {
						for (int bw = 0; bw < 3; bw++) {
							oldpartvar += (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw))
									* (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw));
						}

					}
				}

			}

			for (int bw = 0; bw < 3; bw++) {
				oldpartvar += (factual.get(i).get(bw) - wished.get(i).get(bw)) * (factual.get(i).get(bw) - wished.get(i).get(bw));
			}

			double newpartvar = 0;

			for (Iterator<Entry<Integer, Double>> itm = entrySet_neighbors_weights.iterator(); itm.hasNext();) {

				Map.Entry<Integer, Double> itm_current = itm.next();
				if (dir_benchm.they_are_mate(i, itm_current.getKey(), member_list)) {

					double change = changenn / internal_neigh;




					if ((itm_current.getValue() - change) > 0) {

						factual.get(itm_current.getKey()).set(0, (factual.get(itm_current.getKey()).get(0) - change));
						factual.get(itm_current.getKey()).set(2, (factual.get(itm_current.getKey()).get(2) + change));

						factual.get(i).set(0, (factual.get(i).get(0) - change));
						factual.get(i).set(2, (factual.get(i).get(2) + change));

						for (int bw = 0; bw < 3; bw++) {
							newpartvar += (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw))
									* (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw));
						}




						itm_current.setValue(itm_current.getValue() - change);
						others.get(itm_current.getKey()).replace(i, (others.get(itm_current.getKey()).get(i) - change));




					}
				} else {

					double change = changenn / (VE.get(i) - internal_neigh);

					if ((itm_current.getValue() + change) > 0) {

						factual.get(itm_current.getKey()).set(1, (factual.get(itm_current.getKey()).get(1) + change));
						factual.get(itm_current.getKey()).set(2, (factual.get(itm_current.getKey()).get(2) - change));

						factual.get(i).set(1, (factual.get(i).get(1) + change));
						factual.get(i).set(2, (factual.get(i).get(2) - change));

						for (int bw = 0; bw < 3; bw++) {
							newpartvar += (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw))
									* (factual.get(itm_current.getKey()).get(bw) - wished.get(itm_current.getKey()).get(bw));
						}


						
							itm_current.setValue(itm_current.getValue() + change); 
							others.get(itm_current.getKey()).replace(i, (others.get(itm_current.getKey()).get(i) + change)); 

					}

				}
			}

			for (int bw = 0; bw < 3; bw++) {

				newpartvar += (factual.get(i).get(bw) - wished.get(i).get(bw)) * (factual.get(i).get(bw) - wished.get(i).get(bw));

			}

			tot_var += newpartvar - oldpartvar;

		}





		return new PropagateOneTwoContainer(neighbors_weights, wished, factual, tot_var, others);
	}


	public static PropagateContainer propagate(ArrayList<TreeMap<Integer, Double>> neigh_weigh_in, ArrayList<TreeMap<Integer, Double>> neigh_weigh_out, ArrayList<Integer> VE,
			ArrayList<ArrayList<Integer>> member_list, ArrayList<ArrayList<Double>> wished, ArrayList<ArrayList<Double>> factual, int i, double tot_var, double[] strs,
			ArrayList<Integer> internal_kin_top) {

		

		PropagateOneTwoContainer propagate_one_output1 = propagate_one(neigh_weigh_in, VE, member_list, wished, factual, i, tot_var, strs, internal_kin_top, neigh_weigh_out);
		// overwrite input variables with their modified versions after calling propagate_one method
		neigh_weigh_in = propagate_one_output1.getNeighbors_weights();
		wished = propagate_one_output1.getWished();
		factual = propagate_one_output1.getFactual();
		tot_var = propagate_one_output1.getTot_var();
		neigh_weigh_out = propagate_one_output1.getOthers(); // 'getOthers' returns the last input parameter of propagate_one, which is neigh_weigh_out in this case




		PropagateOneTwoContainer propagate_one_output2 = propagate_one(neigh_weigh_out, VE, member_list, wished, factual, i, tot_var, strs, internal_kin_top, neigh_weigh_in);
		// overwrite input variables with their modified versions after calling propagate_one method
		neigh_weigh_out = propagate_one_output2.getNeighbors_weights();
		wished = propagate_one_output2.getWished();
		factual = propagate_one_output2.getFactual();
		tot_var = propagate_one_output2.getTot_var();
		neigh_weigh_in = propagate_one_output2.getOthers();

		

		PropagateOneTwoContainer propagate_two_output1 = propagate_two(neigh_weigh_in, VE, member_list, wished, factual, i, tot_var, strs, internal_kin_top, neigh_weigh_out);
		// overwrite input variables with their modified versions after calling propagate_two method
		neigh_weigh_in = propagate_two_output1.getNeighbors_weights();
		wished = propagate_two_output1.getWished();
		factual = propagate_two_output1.getFactual();
		tot_var = propagate_two_output1.getTot_var();
		neigh_weigh_out = propagate_two_output1.getOthers();

		

		PropagateOneTwoContainer propagate_two_output2 = propagate_two(neigh_weigh_out, VE, member_list, wished, factual, i, tot_var, strs, internal_kin_top, neigh_weigh_in);
		// overwrite input variables with their modified versions after calling propagate_two method
		neigh_weigh_out = propagate_two_output2.getNeighbors_weights();
		wished = propagate_two_output2.getWished();
		factual = propagate_two_output2.getFactual();
		tot_var = propagate_two_output2.getTot_var();
		neigh_weigh_in = propagate_two_output2.getOthers();
		
		
		


		// return container class holding input parameters modified within this method (equivalent to non-constant method parametrs in C++ code)
		return new PropagateContainer(neigh_weigh_in, neigh_weigh_out, wished, factual, tot_var);
	}


	public static WeightsContainerClass weights(ArrayList<TreeSet<Integer>> ein, ArrayList<TreeSet<Integer>> eout, ArrayList<ArrayList<Integer>> member_list, double beta,
			double mu, ArrayList<TreeMap<Integer, Double>> neigh_weigh_in, ArrayList<TreeMap<Integer, Double>> neigh_weigh_out) {



		


		double tstrength = 0;

		ArrayList<Integer> VE = new ArrayList<Integer>(); // VE is the degree of the nodes (in + out)
		ArrayList<Integer> internal_kin_top = new ArrayList<Integer>(); // this is the internal degree of the nodes (in + out)



		for (int i = 0; i < ein.size(); i++) {


			internal_kin_top.add(dir_benchm.internal_kin(ein, member_list, i) + dir_benchm.internal_kin(eout, member_list, i));
			VE.add(ein.get(i).size() + eout.get(i).size());
			tstrength += Math.pow(VE.get(i), beta);



		}


		double[] strs = new double[VE.size()]; // strength of nodes
		// build a matrix like this: ArrayList<TreeSet<Integer, Double>> each row corresponds to link - weights (in C++ this was deque<map <int, double>>)

		
		for (int i = 0; i < VE.size(); i++) {

			// TreeMap<Integer, Double> new_map = new TreeMap<Integer, Double>(); Different to C++: instead of this line, directly initialize TreeMaps below, otherwise neigh_weigh_in and neigh_weigh_out will share TreeMaps

			neigh_weigh_in.add(new TreeMap<Integer, Double>());
			neigh_weigh_out.add(new TreeMap<Integer, Double>());

			for (Iterator<Integer> its = ein.get(i).iterator(); its.hasNext();) {
				neigh_weigh_in.get(i).put(its.next(), 0.); // in C++ this line used pairs
			}

			for (Iterator<Integer> its = eout.get(i).iterator(); its.hasNext();) {
				neigh_weigh_out.get(i).put(its.next(), 0.); // in C++ this line used pairs
			}


			strs[i] = Math.pow(((double) VE.get(i)), beta);
		}




		ArrayList<ArrayList<Double>> wished = new ArrayList<ArrayList<Double>>(); // 3 numbers for each node: internal, idle and extra strength. the sum of the three is strs[i]. wished is the theoretical, factual the factual one.
		ArrayList<ArrayList<Double>> factual = new ArrayList<ArrayList<Double>>();

		for (int i = 0; i < VE.size(); i++) {

			// moved this initialization inside the loop, otherwise same instance is assigned to both wished and factual
			{
				ArrayList<Double> s_in_out_id_row = new ArrayList<Double>(3);
				s_in_out_id_row.add(0, 0.);
				s_in_out_id_row.add(1, 0.);
				s_in_out_id_row.add(2, 0.);
				wished.add(s_in_out_id_row);
			}

			{
				ArrayList<Double> s_in_out_id_row = new ArrayList<Double>(3);
				s_in_out_id_row.add(0, 0.);
				s_in_out_id_row.add(1, 0.);
				s_in_out_id_row.add(2, 0.);
				factual.add(s_in_out_id_row);
			}
		}


		double tot_var = 0;


		for (int i = 0; i < VE.size(); i++) {


			wished.get(i).set(0, ((1.0 - mu) * strs[i]));
			
			wished.get(i).set(1, (mu * strs[i]));

			factual.get(i).set(2, strs[i]);

			tot_var += wished.get(i).get(0) * wished.get(i).get(0) + wished.get(i).get(1) * wished.get(i).get(1) + strs[i] * strs[i];

			
		}
		


		double precision = 1e-9;
		double precision2 = 1e-2;
		double not_better_than = Math.pow(tstrength, 2) * precision;
		

		int step = 0;

		while (true) {
			
			long t0 = new Date().getTime();

			double pre_var = tot_var;

			for (int i = 0; i < VE.size(); i++) {

				
				PropagateContainer propagate_output = propagate(neigh_weigh_in, neigh_weigh_out, VE, member_list, wished, factual, i, tot_var, strs, internal_kin_top);
				// following are added lines that overwrite variables that were modified by propagate method call, by the modified values (in C++ version of the code, pointers were given to the method as input)
				neigh_weigh_in = propagate_output.getNeigh_weigh_in();
				neigh_weigh_out = propagate_output.getNeigh_weigh_out();
				wished = propagate_output.getWished();
				factual = propagate_output.getFactual();
				tot_var = propagate_output.getTot_var();

				// ---------------------------------------------------



			}
			

			double relative_improvement = ((double) (pre_var - tot_var)) / pre_var;

			
			if (tot_var < not_better_than) {

				break;

			}


			if (relative_improvement < precision2) {

				break;
			}

			long t1 = new Date().getTime();
			long deltat = t1 - t0;

			step++;

		}
		
		

		// returns a container class with fields of variables that were given to this method as input and modified within this method
		return new WeightsContainerClass(neigh_weigh_in, neigh_weigh_out);

	}

	/**
	 * Method from which LFR algorithm is initiated, which is based on weighted,
	 * directed benchmark originally written in C++, authored by Andrea
	 * Lancichinetti
	 * 
	 * @param excess,            to produce a benchmark whose distribution of the
	 *                           ratio of external in-degree/total in-degree is
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
	 * @param mixing_parameter2  mixing parameter for the weights
	 * @param beta               exponent for the weight distribution
	 * @param overlapping_nodes  number of overlapping nodes
	 * @param overlap_membership number of memberships of overlapping nodes
	 * @param nmin               minimum for the community sizes
	 * @param nmax               maximum for the community sizes
	 * @param fixed_range        To have a random network: using this option will
	 *                           set muw=0, mut=0, and minc=maxc=N, i.e. there will
	 *                           be one only community.
	 * @return                   integer representing if execution status
	 */
	public static Cover weighted_directed_network_benchmark(boolean excess, boolean defect, int num_nodes, double average_k, int max_degree, double tau, double tau2, double mixing_parameter,
			double mixing_parameter2, double beta, int overlapping_nodes, int overlap_membership, int nmin, int nmax, boolean fixed_range) {

		
		
		try {
			Random.srand_file(); // this method is to ensure that different graph is generated each time benchmark method is called (it updates seed)
		} catch (NumberFormatException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		// it finds the minimum degree -----------------------------------------------------------------------

		
		
		// change signs to opposite of what input is, since C++ based implementation of
		// LFR algorithms uses positive t1, t2
		// input, while LFR algorithm from WebOCD used negative inputs
		tau = -tau;
		tau2 = -tau2;
	
		
		
		double dmin = dir_benchm.solve_dmin(max_degree, average_k, -tau);
		if (dmin == -1) {
			return null; 
		}

		
		
		int min_degree = (int) (dmin);

		double media1 = dir_benchm.integer_average(max_degree, min_degree, tau);
		double media2 = dir_benchm.integer_average(max_degree, min_degree + 1, tau);

		
		
		
		if (Math.abs(media1 - average_k) > Math.abs(media2 - average_k)) {
			min_degree++;
		}


		// range for the community sizes
		if (!fixed_range) {

			nmax = max_degree;
			nmin = Math.max((int) min_degree, 3);
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
		
		

		int inarcs = dir_benchm.deque_int_sum(degree_seq_in);
		degree_seq_out = dir_benchm.compute_internal_degree_per_node(inarcs, degree_seq_in.size(), degree_seq_out); // in C++ code, degree_seq_out address was input. In Java method outputs degree_seq_out that was given as input and this output overwrites the previous degree_seq_out value



		ArrayList<ArrayList<Integer>> member_matrix = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> num_seq = new ArrayList<Integer>();
		ArrayList<Integer> internal_degree_seq_in = new ArrayList<Integer>();
		ArrayList<Integer> internal_degree_seq_out = new ArrayList<Integer>();

		// ******************************** internal_degree and membership ***************************************************


		InternalDegreeAndMembershipContainer internal_degree_and_membership_output = dir_benchm.internal_degree_and_membership(mixing_parameter, overlapping_nodes,
				overlap_membership, num_nodes, member_matrix, excess, defect, degree_seq_in, degree_seq_out, num_seq, internal_degree_seq_in, internal_degree_seq_out, fixed_range,
				nmin, nmax, tau2); // added variable to hold output of the internal_degree_and_membership method (since Java doesn't use pointer inputs like C++ does)

		if (internal_degree_and_membership_output == null) {
			return null; 
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
		BuildSubgraphsContainer build_subgraphs_output = dir_benchm.build_subgraphs(Ein, Eout, member_matrix, member_list, link_list_in, link_list_out, internal_degree_seq_in,
				degree_seq_in, internal_degree_seq_out, degree_seq_out, excess, defect); // added variable to hold output of the build_subgraphs method (since Java doesn't use pointer inputs like C++ does)

		if (build_subgraphs_output == null) {
			return null; 
		}



		// following are added lines that overwrite variables that were modified by build_subgraphs method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		Ein = build_subgraphs_output.getEin();
		Eout = build_subgraphs_output.getEout();
		member_list = build_subgraphs_output.getMember_list();
		link_list_in = build_subgraphs_output.getLink_list_in();
		link_list_out = build_subgraphs_output.getLink_list_out();
		// ------------------------------------------------------------------------------------------------


		
		
		
		
		System.out.println("connecting communities...");
		EinEoutContainer connect_all_parts_output = dir_benchm.connect_all_the_parts(Ein, Eout, member_list, link_list_in, link_list_out);


		// following are added lines that overwrite variables that were modified by connect_all_the_parts method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		Ein = connect_all_parts_output.getEin();
		Eout = connect_all_parts_output.getEout();
		// ------------------------------------------------------------------------------------------------

	
		

		EinEoutContainer erase_links_output = dir_benchm.erase_links(Ein, Eout, member_list, excess, defect, mixing_parameter);
		if (erase_links_output == null) {
			return null;
		}

		

		// following are added lines that overwrite variables that were modified by erase_links method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		Ein = erase_links_output.getEin();
		Eout = erase_links_output.getEout();
		// ------------------------------------------------------------------------------------------------
		

		ArrayList<TreeMap<Integer, Double>> neigh_weigh_in = new ArrayList<TreeMap<Integer, Double>>();
		ArrayList<TreeMap<Integer, Double>> neigh_weigh_out = new ArrayList<TreeMap<Integer, Double>>();

		System.out.println("inserting weights...");
		WeightsContainerClass weights_output = weights(Ein, Eout, member_list, beta, mixing_parameter2, neigh_weigh_in, neigh_weigh_out);


		// following are added lines that overwrite variables that were modified by 'weights' method, by the modified values (in C++ version of the code, pointers were given to the method as input)
		neigh_weigh_in = weights_output.getNeigh_weigh_in();
		neigh_weigh_out = weights_output.getNeigh_weigh_out();
		// ------------------------------------------------------------------------------------------------


		
		try {
			System.out.println("recording network...");
			print_network(Ein, Eout, member_list, member_matrix, num_seq, neigh_weigh_in, neigh_weigh_out, beta, mixing_parameter2, mixing_parameter);
		} catch (IOException e) {
			e.printStackTrace();
		}

	///// create a cover to be returned /////
		
		CustomGraph graph = new CustomGraph();
		
		/*
		 *  create graph nodes
		 */
		for (int u = 0; u < Eout.size(); u++) {
			Node node = graph.createNode();
			graph.setNodeName(node, Integer.toString(u));
			

		}
		
		
		/*
		 *  create edges between the nodes
		 */
		Node[] nodes = graph.getNodeArray();
		for (int u = 0; u < Eout.size(); u++) {
			for (Iterator<Integer> itb = Eout.get(u).iterator(); itb.hasNext();) {

				Integer itb_current = itb.next();
				Edge edge = graph.createEdge(nodes[u], nodes[itb_current]);
				graph.setEdgeWeight(edge, neigh_weigh_out.get(u).get(itb_current));
			}
		}
		
		/*
		 *  matrix row count corresponds to node count	
		 */
		int node_count = member_list.size(); 	
		/*
		 * matrix column count correspond to highest community id. This is because
		 * communities are counted up and highest community id will correspond to number
		 * of columns in a memership matrix
		 */
		int community_count = 0; 
		for (int i = 0; i < member_list.size(); i++) {
			int curr = Collections.max(member_list.get(i));
			if (curr > community_count) {
				community_count = curr;
			}
		}	
	

		// populate membership matrix for cover creation
		Matrix memberships = new Basic2DMatrix(node_count, community_count+1);

		for (int i = 0; i < node_count; i++) {
			for (int j = 0; j < member_list.get(i).size(); j++) {
				
				/*
				 * assign memberships of nodes. Since matrix columns start with index 0,
				 * subtract 1 column coordinate
				 */
				memberships.set(i, member_list.get(i).get(j), 1); 
				
			}
		}
		
		Cover c = new Cover(graph, memberships);
		
		return c;
	}



}
