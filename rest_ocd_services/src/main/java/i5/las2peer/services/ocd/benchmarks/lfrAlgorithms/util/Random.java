package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
/**
 * This class holds Java version of the methods from random.cpp authored by Andrea Lancichinetti. 
 * Original C++ code can be found on https://sites.google.com/site/andrealancichinetti/files.
 */
public class Random {

	public static final int R2_IM1 = 2147483563;
	public static final int R2_IM2 = 2147483399;
	public static final double R2_AM = (1.0 / R2_IM1);
	public static final double R2_IMM1 = (R2_IM1 - 1);
	public static final int R2_IA1 = 40014;
	public static final int R2_IA2 = 40692;
	public static final int R2_IQ1 = 53668;
	public static final int R2_IQ2 = 52774;
	public static final int R2_IR1 = 12211;
	public static final int R2_IR2 = 3791;
	public static final int R2_NTAB = 32;
	public static final double R2_NDIV = (1 + R2_IMM1 / R2_NTAB);
	public static final double R2_EPS = 1.2e-7;
	public static final double R2_RNMX = (1.0 - R2_EPS);


	static long idum2 = 123456789;
	static long iy = 0;
	static long[] iv = new long[R2_NTAB];


	public static long seed_ = 1;

	public static double ran2(long idum) {
		int j;
		long k;
//		long idum2 = 123456789;
//		long iy = 0;
//		long[] iv = new long[R2_NTAB]; 
		double temp;

		if (idum <= 0 || iy == 0) {

			if (-idum < 1) {
				idum = 1 * idum;
			} else {
				idum = -idum;
			}
			idum2 = idum;

			for (j = R2_NTAB + 7; j >= 0; j--) {
				k = (idum) / R2_IQ1;
				idum = R2_IA1 * (idum - k * R2_IQ1) - k * R2_IR1;

				if (idum < 0) {
					idum += R2_IM1;
				}
				if (j < R2_NTAB) {
					iv[j] = idum;
				}
			}
			iy = iv[0];
		}
		k = idum / R2_IQ1;
		idum = R2_IA1 * (idum - k * R2_IQ1) - k * R2_IR1;
		if (idum < 0) {
			idum += R2_IM2;
		}
		k = (idum2) / R2_IQ2;
		idum2 = R2_IA2 * (idum2 - k * R2_IQ2) - k * R2_IR2;
		if (idum2 < 0) {
			idum2 += R2_IM2;
		}
		j = (int) (iy / R2_NDIV); // added casting to int, since java doesnt do it automatically
		iy = iv[j] - idum2;
		iv[j] = idum;
		if (iy < 1) {
			iy += R2_IMM1;
		}
		temp = R2_AM * iy;
		seed_ = idum; // added line to imitate C++ code, which has address of seed_ as input to this
						// method
		if (temp > R2_RNMX) {

			return R2_RNMX;

		} else {

			return temp;
		}
	}

	public static double ran4(Boolean t, long s) {

		double r = 0;

		// long seed_ = 1; seed_ is defined outside of this method

		if (t) {
			r = ran2(seed_);

		} else {
			seed_ = s;
		}

		return r;
	}

	public static double ran4() {
		return ran4(true, 0);
	}

	public static void srand4() {

		long s = (long) new Date().getTime();
		ran4(false, s);

	}

	public static void srand5(int rank) {

		long s = (long) rank;
		ran4(false, s);
	}

	public static int irand(int n) {

		int res = ((int) (ran4() * (n + 1)));

		return res;
	}

	public static void srand_file() throws NumberFormatException, IOException {

		File f = new File("time_seed.dat");

		if (!f.exists()) { // check if time_seed.dat already exists
			PrintStream seedStream = new PrintStream("time_seed.dat"); // if time_seed.dat doesn't exist, create it
			seedStream.append(Integer.toString(21111983)); // put initial seed value in time_seed.dat
			seedStream.close();
		}

		BufferedReader seed_reader = new BufferedReader(new FileReader("time_seed.dat"));
		int seed = Integer.parseInt(seed_reader.readLine()); // read in the seed value
		srand5(seed);

		PrintStream seedStream = new PrintStream("time_seed.dat");
		seedStream.append(Integer.valueOf(seed + 1).toString()); // increase seed value by 1
		seedStream.close();

	}

}
