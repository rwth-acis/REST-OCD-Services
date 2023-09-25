package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import i5.las2peer.services.ocd.graphs.CustomNode;

public class MultiplexClique{

    private Set<Integer> nodes = new HashSet<>();
    private Set<String> layers = new HashSet<>();
    private int index;

    public MultiplexClique(Set<Integer> nodes, Set<String> layers, int index) {
        this.nodes = nodes;
        this.layers = layers;
        this.index = index;
    }

    public MultiplexClique() {
        
    }


    public boolean equals(MultiplexClique comp) {
        if (nodes.size() != comp.nodes.size() || layers.size() != comp.layers.size()) {
            return false;
        }

        if (!nodes.equals(comp)) {
            return false;
        }

        if (!layers.equals(layers)) {
            return false;
        }
        return true;
    }

    public boolean notEquals(MultiplexClique comp) {
        return !equals(comp);
    }

    public boolean lessThan(MultiplexClique comp) {
        if (nodes.size() != comp.nodes.size()) {
            return nodes.size() < comp.nodes.size();
        }

        if (layers.size() != comp.layers.size()) {
            return layers.size() < comp.layers.size();
        }

        Iterator<Integer> it1 = nodes.iterator();
        Iterator<Integer> it2 = comp.nodes.iterator();

        while (it1.hasNext()) {
            Integer numb1 = it1.next();
            Integer numb2 = it2.next();

            if (numb1.compareTo(numb2) < 0) {
                return true;
            }

            if (numb1.compareTo(numb2) > 0) {
                return false;
            }
        }

        Iterator<String> layerIt1 = layers.iterator();
        Iterator<String> layerIt2 = comp.layers.iterator();

        while (layerIt1.hasNext()) {
            String layer1 = layerIt1.next();
            String layer2 = layerIt2.next();

            if (layer1.compareTo(layer2) < 0) {
                return true;
            }

            if (layer1.compareTo(layer2) > 0) {
                return false;
            }
        }

        return false;
    }

    public boolean greaterThan(MultiplexClique comp) {
        return !lessThan(comp) && !equals(comp);
    }

    // Method to add a node to the 'nodes' set
    public void addNode(Integer node) {
        nodes.add(node);
    }

    // Method to remove a node from the 'nodes' set
    public void removeNode(Integer node) {
        nodes.remove(node);
    }

    // Method to add a layer to the 'layers' set
    public void addLayer(String layer) {
        layers.add(layer);
    }

    // Method to remove a layer from the 'layers' set
    public void removeLayer(String layer) {
        layers.remove(layer);
    }

    // Setter for nodes
    public void setNodes(Set<Integer> nodes) {
        this.nodes = nodes;
    }

    // Getter for nodes
    public Set<Integer> getNodes() {
        return nodes;
    }

    // Setter for layers
    public void setLayers(Set<String> layers) {
        this.layers = layers;
    }

    // Getter for layers
    public Set<String> getLayers() {
        return layers;
    }

     // Setter for index
    public void setIndex(int index) {
        this.index = index;
    }

    // Getter for index
    public int getIndex() {
        return index;
    }
}
