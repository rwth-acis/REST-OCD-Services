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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.HashMap;


/**
 * Sparse square matrix using hashmap.
 */
public class LouvainSparseIntMatrix {
  private final HashMap<Long,Integer> map;
  private final long size;
  private boolean compressed = false;

  public LouvainSparseIntMatrix(int size) {
    this.size = size;
    map = new HashMap<Long,Integer>();
  }

  public LouvainSparseIntMatrix(LouvainSparseIntMatrix m) {
    this.size = m.size();
    map = new HashMap<Long,Integer>(m.map);
    compressed = m.compressed;
  }

  public int get(int x, int y) {
	  if(map.get((long) x * size + (long) y) != null) {
		  return map.get((long) x * size + (long) y);
	  }
	  return 0;
  }

  public void set(int x, int y, int val) {
    map.put((long) x * size + (long) y, val);
    compressed = false;
  }

  public void add(int x, int y, int val) {
    set(x, y, get(x, y) + val);
  }

  public long size() {
    return size;
  }

  public Iterator<HashMap.Entry<Long,Integer>> iterator() {
    return map.entrySet().iterator();
  }

  public boolean isSymmetric() {
    for (final Iterator<HashMap.Entry<Long,Integer>> it = iterator(); it.hasNext(); ) {
    	Entry<Long, Integer> entry = it.next();
      if (entry.getValue() != get(sparseX(entry, size), sparseY(entry, size))) {
        return false;
      }
    }
    return true;
  }
  
  public int sparseX(Entry<Long, Integer> entry, long size) {
    return (int) (entry.getKey() % size);
  }
  public int sparseY(Entry<Long, Integer> entry, long size) {
    return (int) (entry.getKey() / size);
  }
}