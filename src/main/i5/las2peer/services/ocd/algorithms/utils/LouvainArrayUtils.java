/* MIT License
Copyright (c) 2018 Neil Justice
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */

package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Random;

/**
 * This class contains some helper methods for Array handling in the Louvain algorithm
 */
public final class LouvainArrayUtils {

  private LouvainArrayUtils() {
    // Disable instantiation of static helper class
  }

  /**
   * Gives the last occuring index of a value in an array
   * @param a An array
   * @param n A value
   * @return The last occuring index of a value in an array
   */
  public static int lastIndexOf(double[] a, double n) {
    if (a == null) {
      return -1;
    }
    int lastIndex = -1;
    for (int i = 0; i < a.length; i++) {
      if (a[i] == n) {
        lastIndex = i;
      }
    }
    return lastIndex;
  }

  /**
   * Shuffles the values in an integer array randomly
   * @param a An array
   */
  public static void shuffle(int[] a) {
    final Random rnd = new Random();
    final int count = a.length;
    for (int i = count; i > 1; i--) {
      final int r = rnd.nextInt(i);
      swap(a, i - 1, r);
    }
  }

  /**
   * Fills an integer array with random values
   * @param a An array
   */
  public static void fillRandomly(int[] a) {
    final int count = a.length;

    for (int i = 0; i < count; i++) {
      a[i] = i;
    }

    shuffle(a);
  }

  /**
   * Swaps to values in an integer array
   * @param a An array
   * @param i A first value
   * @param j A second value
   */
  private static void swap(int[] a, int i, int j) {
    final int temp = a[i];
    a[i] = a[j];
    a[j] = temp;
  }
}