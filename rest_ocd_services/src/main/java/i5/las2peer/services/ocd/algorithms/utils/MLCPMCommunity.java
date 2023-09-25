package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashSet;
import java.util.Set;

//Holding Community Meta Information
public class MLCPMCommunity {
    private Set<MultiplexClique> cliques = new HashSet<>();
    private Set<String> layers = new HashSet<>();

    // Getter for cliques
    public Set<MultiplexClique> getCliques() {
        return cliques;
    }

    // Setter for cliques
    public void setCliques(Set<MultiplexClique> cliques) {
        this.cliques = cliques;
    }

    // Getter for layers
    public Set<String> getLayers() {
        return layers;
    }

    // Setter for layers
    public void setLayers(Set<String> layers) {
        this.layers = layers;
    }

    // Method to add a clique
    public void addClique(MultiplexClique clique) {
        cliques.add(clique);
    }

    // Method to delete a clique
    public void deleteClique(MultiplexClique clique) {
        cliques.remove(clique);
    }

    // Method to add a layer
    public void addLayer(String layer) {
        layers.add(layer);
    }

    // Method to delete a layer
    public void deleteLayer(String layer) {
        layers.remove(layer);
    }


    
}
