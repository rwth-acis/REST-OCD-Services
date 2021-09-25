package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class holds Java version of lower_bound methods similar to those in C++
 */
public class HelperMethods {

	/**
	 * This method returns index of the first occurrence of the Double value at
	 * least as high as the threshold. It imitates C++ method with the same name
	 * 
	 * @param arr       ArrayList<Double> to which the lower bound should be found
	 * @param first     index from which the search should occur
	 * @param last      index to which the search should occur
	 * @param threshold value the found element should be at least as large as
	 * @return index of the first occurence of the Double value found
	 */
	public static int lower_bound(ArrayList<Double> arr, int first, int last, double threshold) {

		Collections.sort(arr); // sort the array before binary search

		ArrayList<Double> arr_in_range = new ArrayList<Double>(); // array list for specified range
		arr_in_range.addAll(arr.subList(first, last));

		int searched_value_index = arr_in_range.size(); // if desired value can't be found, one past last element index
														// will be returned, to imitate C++ lower_bound

		for (int i = 0; i < arr_in_range.size(); i++) {

			if (arr_in_range.get(i) >= threshold) {

				searched_value_index = i;
				break; // first occurance of searched value is desired, to imitate C++ lower_bound

			}
		}

		return first + searched_value_index; // we have to add 'first' to get index of the whole array, not subarray

	}

	/**
	 * This method returns index of the first occurrence of the Integer value at
	 * least as high as the threshold. It imitates C++ method with the same name
	 * 
	 * @param arr       ArrayList<Integer> to which the lower bound should be found
	 * @param first     index from which the search should occur
	 * @param last      index to which the search should occur
	 * @param threshold value the found element should be at least as large as
	 * @return index of the first occurrence of the Integer value found
	 */
	public static int lower_bound_int(ArrayList<Integer> arr, int first, int last, int threshold) {

		Collections.sort(arr); // sort the array before binary search

		ArrayList<Integer> arr_in_range = new ArrayList<Integer>(); // array list for specified range
		arr_in_range.addAll(arr.subList(first, last));

		int searched_value_index = arr_in_range.size(); // if desired value can't be found, one past last element index
														// will be returned, to imitate C++ lower_bound

		for (int i = 0; i < arr_in_range.size(); i++) {

			if (arr_in_range.get(i) >= threshold) {

				searched_value_index = i;
				break; // first occurrence of searched value is desired, to imitate C++ lower_bound

			}
		}

		return first + searched_value_index; // we have to add 'first' to get index of the whole array, not subarray
	}

}
