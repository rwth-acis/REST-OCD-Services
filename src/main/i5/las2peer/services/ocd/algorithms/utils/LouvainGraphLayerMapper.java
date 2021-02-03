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

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.LouvainGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Given a list of graphs where each node in graph n + 1 is a community in
 * graph n, this class maps the partitionings of all graphs (except the first) as if they
 * were partitionings of the first graph.
 */
public class LouvainGraphLayerMapper {
  private final List<LouvainGraph> graphs = new ArrayList<>();
  // maps between communities on L and nodes on L + 1:
  private final List<HashMap<Integer,Integer>> layerMaps = new ArrayList<>();
  private int layer = 0;

  /**
   * Maps nodes from a community to the corresponding node on the layer above
   * @param g A community in the form of a Louvain graph
   * @return The mapping
   * @throws OcdAlgorithmException
   */
  public HashMap<Integer,Integer> createLayerMap(LouvainGraph g) 
		  throws OcdAlgorithmException {
    int count = 0;
    layer++;
    final boolean[] isFound = new boolean[g.order()];
    final HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
    // Arrays.sort(communities);

    for (int node = 0; node < g.order(); node++) {
      final int comm = g.partitioning().community(node);
      if (!isFound[comm]) {
        map.put(comm, count);
        isFound[comm] = true;
        count++;
      }
    }
    if (map.size() != g.partitioning().numComms()) {
      throw new OcdAlgorithmException("Map creation failed: " +
          g.partitioning().numComms() + " != " +
          map.size());
    }
    layerMaps.add(map);
    graphs.add(g);
    return map;
  }

  /**
   * Uses the layer maps to assign a community from each layer to the base layer graph.
   * @return A list of community belongings per layer
   */
  public List<int[]> run() {
    final List<int[]> rawComms = new ArrayList<>();
    final List<int[]> communities = new ArrayList<>();
    communities.add(graphs.get(0).partitioning().communities());

    for (int i = 0; i < layer; i++) {
      rawComms.add(graphs.get(i).partitioning().communities());
    }

    for (int i = 0; i < layer - 1; i++) {
      communities.add(mapToBaseLayer(i, rawComms));
    }

    return communities;
  }

  /**
   * Maps layers to each other until the specified layer has been mapped to the base layer
   * @param layer A specified layer
   * @param rawComms The basic communities per layer
   * @return The mapping from nodes of the specified layer to the base layer
   */
  private int[] mapToBaseLayer(int layer, List<int[]> rawComms) {
    int[] a = mapToNextLayer(graphs.get(layer), layerMaps.get(layer),
        rawComms.get(layer + 1));
    layer--;

    while (layer >= 0) {
      a = mapToNextLayer(graphs.get(layer), layerMaps.get(layer), a);
      layer--;
    }

    return a;
  }

  /**
   * Maps each node in a layer to its community on the layer above it
   * @param g A louvain graph with the specified community
   * @param map A mapping "function"
   * @param commsL2 The community in a layer above the first
   * @return The mapping
   */
  private int[] mapToNextLayer(LouvainGraph g, HashMap<Integer,Integer> map, int[] commsL2) {
    final int[] commsL1 = g.partitioning().communities();
    final int[] NL1toCL2 = new int[g.order()];

    for (int nodeL1 = 0; nodeL1 < g.order(); nodeL1++) {
      final int commL1 = commsL1[nodeL1];
      final int nodeL2 = map.get(commL1);
      final int commL2 = commsL2[nodeL2];
      NL1toCL2[nodeL1] = commL2;
    }

    return NL1toCL2;
  }
}