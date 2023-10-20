package i5.las2peer.services.ocd.viewer.painters;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.graphstream.graph.Edge;

public class EdgePainter {
     


        /**
	 * This method assigns every edge a color, corresponding on the layer it is on.
	 * @param cover The cover, whose graph should be painted
	 */
    public void doEdgePaint(Cover cover) {
        List<Color> colors;
        if (cover.getGraph().getNumberOfLayers() > 20) {
            colors = getColorCollection(cover.getGraph().getNumberOfLayers());
        } else {
            colors = getPredefinedColors();
        }
        List<String> layers = cover.getGraph().Layers();
        Map<String, Color> layerColors = new HashMap<>();

        for (int i = 0; i < layers.size(); i++) {
            layerColors.put(layers.get(i), colors.get(i));
        }

        Iterator<Edge> edges = cover.getGraph().edges().iterator();
        while (edges.hasNext()) {
            Edge edge = edges.next();

            Color color = layerColors.get(cover.getGraph().getEdgeLayerId(edge));
            int red = color.getRed();
            int blue = color.getBlue();
            int green = color.getGreen();
            edge.setAttribute("color", "rgb(" + red + "," + green + "," + blue + ")");
        }

    }
      /**
	 * This method assigns every edge a color, corresponding on the layer it is on.
	 * @param graph The graph, whose edges should be painted
	 */
    public void doEdgePaint(CustomGraph graph) {
        List<Color> colors;
        if (graph.getNumberOfLayers() > 20) {
            colors = getColorCollection(graph.getNumberOfLayers());
        } else {
            colors = getPredefinedColors();
        }
        List<String> layers = graph.Layers();
        Map<String, Color> layerColors = new HashMap<>();

        for (int i = 0; i < layers.size(); i++) {
            layerColors.put(layers.get(i), colors.get(i));
        }

        Iterator<Edge> edges = graph.edges().iterator();
        while (edges.hasNext()) {
            Edge edge = edges.next();

            Color color = layerColors.get(graph.getEdgeLayerId(edge));
            int red = color.getRed();
            int blue = color.getBlue();
            int green = color.getGreen();
            edge.setAttribute("color", "rgb(" + red + "," + green + "," + blue + ")");
        }

    }

    /**
     * Returns a collection of random colors.
     * 
     * @param amount The amount of colors required.
     * @return The color collection.
     */
    protected List<Color> getColorCollection(int amount) {
        List<Color> colors = new ArrayList<Color>();
        for (int i = 0; i < amount; i++) {
            Random random = new Random();
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);
            /*
             * Mix random color with light gray to lighten up.
             */
            /*
             * if (colors != null) {
             * red = (red + 192) / 2;
             * green = (green + 192) / 2;
             * blue = (blue + 192) / 2;
             * }
             */
            colors.add(new Color(red, green, blue));
        }
        return colors;
    }



     /**
     * Returns a collection of predefined colors.
     * @return The color collection.
     */
    public List<Color> getPredefinedColors() {
        List<Color> colors = new ArrayList<>();
        colors.add(new Color(139, 0, 0));          
        colors.add(new Color(0, 100, 0));          
        colors.add(new Color(0, 0, 139));         
        colors.add(new Color(128, 128, 0));        
        colors.add(new Color(128, 0, 128));        
        colors.add(new Color(0, 128, 128));        
        colors.add(new Color(255, 69, 0));         
        colors.add(new Color(128, 0, 0));         
        colors.add(new Color(0, 0, 128));          
        colors.add(new Color(139, 0, 139));        
        colors.add(new Color(0, 128, 0));          
        colors.add(new Color(0, 128, 128));        
        colors.add(new Color(139, 69, 19));        
        colors.add(new Color(47, 79, 79));         
        colors.add(new Color(0, 0, 205));          
        colors.add(new Color(128, 0, 0));          
        colors.add(new Color(205, 92, 92));        
        colors.add(new Color(0, 100, 0));          
        colors.add(new Color(139, 0, 0));          
        colors.add(new Color(0, 0, 128)); 
        return colors;
    }

}
