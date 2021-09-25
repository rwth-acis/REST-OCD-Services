package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * This class holds read and write methods for different types used throughout
 * the LFR/Signed LFR algorithms. While these are not part of the C++ code which
 * the Algorithms are based on, these methods can simplify debugging and
 * checking of how different method outputs look, in case it's necessary, or if
 * one wants to start some method with parameters that have some specific
 * values, e.g. for debugging
 */
public class ReadWrite {

	/**
	 * Print out ArrayList of ArrayLists of Doubles
	 * @param arar     ArrayList of ArrayLists of Doubles to print out
	 * @param name     Name of the file where results will be printed
	 */
	public static void arar(ArrayList<ArrayList<Double>> arar, String name, boolean round) {

		try {
			// delete file if it exists, before writing to it
			Files.deleteIfExists(Paths.get(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintStream networkStream = null;
		try {
			networkStream = new PrintStream(name);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < arar.size(); i++) {

			networkStream.append(Integer.toString((i + 1)));
			networkStream.append("   ");
			for (int j = 0; j < arar.get(i).size(); j++) {
				if (!round) {
					networkStream.append(Double.toString((arar.get(i).get(j))));
				} else {
					networkStream.append(Double.toString(Math.round(arar.get(i).get(j)))); 

				}
				networkStream.append(' ');
			}
			networkStream.println();

		}
		networkStream.close();

	}

	/**
	 * Print out ArrayList of ArrayList of Integers
	 * @param arar     ArrayList of ArrayLists of Integers to print out
	 * @param name     Name of the file where results will be printed
	 */
	public static void arar_int(ArrayList<ArrayList<Integer>> member_matrix, String name) {

		try {
			// delete file if it exists, before writing to it
			Files.deleteIfExists(Paths.get(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintStream networkStream = null;
		try {
			networkStream = new PrintStream(name);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		for (int i = 0; i < member_matrix.size(); i++) {

			networkStream.append(Integer.toString((i)));
			networkStream.append("   ");
			for (int j = 0; j < member_matrix.get(i).size(); j++) {
				networkStream.append(Integer.toString((member_matrix.get(i).get(j))));
				networkStream.append(' ');
			}
			networkStream.println();

		}
		networkStream.close();
	}

	/**
	 * Print out ArrayList of TreeMap
	 * @param armap     ArrayList of TreeMap to print out
	 * @param name      Name of the file where results will be printed
	 */
	public static void armap(ArrayList<TreeMap<Integer, Double>> armap, String name) {

		try {
			// delete file if it exists, before writing to it
			Files.deleteIfExists(Paths.get(name));
		} catch (IOException e) {

			e.printStackTrace();
		}
		PrintStream networkStream = null;
		try {
			networkStream = new PrintStream(name);

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		for (int i = 0; i < armap.size(); i++) {

			networkStream.append(Integer.toString((i)));
			networkStream.append("   ");
			Set<Map.Entry<Integer, Double>> entrySet_in = armap.get(i).entrySet(); // added variable needed for
																					// iterating over TreeMap entries
			for (Iterator<Entry<Integer, Double>> itm = entrySet_in.iterator(); itm.hasNext();) {
				Map.Entry<Integer, Double> itm_current = itm.next();

				networkStream.append(Integer.toString(itm_current.getKey()));
				networkStream.append(":");
				networkStream.append(Double.toString(itm_current.getValue())); 
				networkStream.append("   ");

			}
			networkStream.println();

		}
		networkStream.close();

	}

	/**
	 * Print out ArrayList of TreeSet
	 * @param arset     ArrayList of TreeSet to print out
	 * @param name      Name of the file where results will be printed
	 */
	public static void arset(ArrayList<TreeSet<Integer>> arset, String name) {
		try {
			// delete file if it exists, before writing to it
			Files.deleteIfExists(Paths.get(name));
		} catch (IOException e) {

			e.printStackTrace();
		}
		PrintStream networkStream = null;
		try {
			networkStream = new PrintStream(name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int u = 0; u < arset.size(); u++) {

			networkStream.append(Integer.toString((u)));
			networkStream.append("   "); // instead of "\t" in C++ code
			for (Iterator<Integer> itb = arset.get(u).iterator(); itb.hasNext();) {
				Integer itb_current = itb.next(); // added line to avoid calling 'itb.next()' multiple times

				networkStream.append(Integer.toString((itb_current)));
				networkStream.append(" ");

			}
			networkStream.println(); // new line
		}
		networkStream.close();
	}

	/**
	 * Print out ArrayList of Pairs
	 * @param arpair ArrayList of Pairs to print out
	 * @param name   Name of the file where results will be printed
	 */
	public static int ar_pair(ArrayList<Pair<Integer>> arpair, String name) {
		try {
			// delete file if it exists, before writing to it
			Files.deleteIfExists(Paths.get(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintStream networkStream = null;
		try {
			networkStream = new PrintStream(name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int u = arpair.size() - 1; u >= 0; u--) {

			networkStream.append(arpair.get(u).getFirst().toString());
			networkStream.append(" : ");
			networkStream.append(arpair.get(u).getSecond().toString());
			networkStream.println(); // new line
		}
		networkStream.close();

		return 0;
	}

	/**
	 * Print out CustomMultiMap
	 * @param multimap CustomMultiMap of Pairs to print out
	 * @param name     Name of the file where results will be printed
	 */
	public static int printCustomMultiMap(CustomMultiMap<Integer> multimap, String name) {

		CustomMultiMapIterator<Integer> itit = multimap.iterator();

		try {
			// delete file if it exists, before writing to it
			Files.deleteIfExists(Paths.get(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintStream networkStream = null;
		try {
			networkStream = new PrintStream(name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (itit.hasNext()) {

			Pair<Integer> curr = itit.next();

			networkStream.append(curr.getFirst().toString());
			networkStream.append(" : ");
			networkStream.append(curr.getSecond().toString());
			networkStream.println(); // new line
		}
		networkStream.close();

		return 0;
	}

	/**
	 * Read in a CustomMultiMap
	 * @param path Location of C++ MultiMap to read
	 * @return CustomMultiMap<Integer> equivalent to C++ MultiMap
	 */
	public static CustomMultiMap<Integer> readMultimap(String path) {
		File file = new File(path);
		CustomMultiMap<Integer> map = new CustomMultiMap<Integer>();
		CustomMultiMap<Integer> output = new CustomMultiMap<Integer>();

		try {
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine() && sc.hasNextInt()) {
				String next = sc.nextLine();

				String[] pair_string = next.split(" : ");

				map.put(new Pair<Integer>(Integer.parseInt(pair_string[0]), Integer.parseInt(pair_string[1])));

			}

			sc.close();

			for (int i = map.size() - 1; i >= 0; i--) {
				output.put(map.get(i));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * Read in deque<int> as ArrayList<Integer>
	 * @param path Location of C++ deque<int> to read
	 * @return ArrayList<Integer> equivalent to C++ deque<int>
	 */
	public static ArrayList<Integer> readArrayList(String path) {
		File file = new File(path);

		ArrayList<Integer> output = new ArrayList<Integer>();

		try {
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine() && sc.hasNextInt()) {
				String next = sc.nextLine();
				output.add(Integer.parseInt(next));
			}

			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * Read in deque<double> as ArrayList<Double>
	 * @param path Location of C++ deque<double> to read
	 * @return ArrayList<Double> equivalent to C++ deque<double>
	 */
	public static ArrayList<Double> readArrayList_double(String path) {
		File file = new File(path);

		ArrayList<Double> output = new ArrayList<Double>();

		try {
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine() && sc.hasNextDouble()) {
				String next = sc.nextLine();
				output.add(Double.parseDouble(next));
			}

			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * Read in deque<deque<int>> as ArrayList<ArrayList<Integer>>
	 * @param path Location of C++ deque<deque<int> to read
	 * @return ArrayList<ArrayList<Integer> equivalent to C++ deque<deque<int>>
	 */
	public static ArrayList<ArrayList<Integer>> readArrayListArrayList(String path) {
		File file = new File(path);

		ArrayList<ArrayList<Integer>> output = new ArrayList<ArrayList<Integer>>();

		try {
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine() && sc.hasNextInt()) {
				String next = sc.nextLine();

				String[] array_string = next.split("\\s+");

				ArrayList<Integer> arr = new ArrayList<Integer>();
				for (int z = 1; z < array_string.length; z++) {
					arr.add(Integer.parseInt(array_string[z]));
				}

				output.add(arr);
			}

			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * Read in deque of pairs as ArrayList<Pair<Integer>>
	 * @param path Location of C++ deque<pair<int, int>> to read
	 * @return ArrayList<Pair<Integer> equivalent to C++ deque<deque<int,int>>
	 */
	public static ArrayList<Pair<Integer>> readArrayListPair(String path) {
		File file = new File(path);
		ArrayList<Pair<Integer>> arr = new ArrayList<Pair<Integer>>();
		ArrayList<Pair<Integer>> output = new ArrayList<Pair<Integer>>();

		try {
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine() && sc.hasNextInt()) {
				String next = sc.nextLine();
				String[] pair_string = next.split(" : ");

				arr.add(new Pair<Integer>(Integer.parseInt(pair_string[0]), Integer.parseInt(pair_string[1])));

			}

			sc.close();

			for (int i = arr.size() - 1; i >= 0; i--) {
				output.add(arr.get(i));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	/**
	 * Read in deque<set<int>> as ArrayList<TreeSet<Integer>>
	 * @param path Location of C++ deque<set<int>> to read
	 * @return ArrayList<TreeSet<Integer> equivalent to C++ deque<set<int>>
	 */
	public static ArrayList<TreeSet<Integer>> readArrTreeSet(String path) {
		File file = new File("stuff/" + path);
		ArrayList<TreeSet<Integer>> arr = new ArrayList<TreeSet<Integer>>();
		ArrayList<TreeSet<Integer>> output = new ArrayList<TreeSet<Integer>>();

		try {
			Scanner sc = new Scanner(file);

			while (sc.hasNextLine() && sc.hasNextInt()) {
				String next = sc.nextLine();

				String[] set_string = next.split("\\s+");

				TreeSet<Integer> tree = new TreeSet<Integer>();

				for (int i = 1; i < set_string.length; i++) {
					tree.add(Integer.parseInt(set_string[i]));
				}
				output.add(tree);
			}

			for (int i = arr.size() - 1; i >= 0; i--) {
				output.add(arr.get(i));
			}

			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

}
