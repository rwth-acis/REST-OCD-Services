package i5.las2peer.services.ocd.graphs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.MultiNode;

import java.util.*;

public class DynamicGraph extends CustomGraph{
    /**
     * Dynamic graph extension
     * Extends CustomGraph's by a list of interactions
     */
    private List<DynamicInteraction> dynamicInteractions = new ArrayList<>();

    public DynamicGraph() {
    }

    public DynamicGraph(CustomGraph graph, List<DynamicInteraction> dynamicInteractions) {
        super(graph);
        this.dynamicInteractions = dynamicInteractions;
    }

    public DynamicGraph(String key) {
        super(key);
    }

    public DynamicGraph(AbstractGraph graph) {
        super(graph);
    }

    public DynamicGraph(DynamicGraph graph) {
        super(graph);
        this.dynamicInteractions = graph.dynamicInteractions;
    }

    /**
     * The getter for the list of dynamic interactions
     * @return list
     */
    public List<DynamicInteraction> getDynamicInteractions() {
        return dynamicInteractions;
    }

    /**
     * The setter for the list of dynamic interactions
     * @param dynamicInteractions
     */
    public void setDynamicInteractions(List<DynamicInteraction> dynamicInteractions) {
        this.dynamicInteractions = dynamicInteractions;
    }

    /**
     * @return true if the graph is dynamic
     */
    public boolean isDynamic() {
        return isOfType(GraphType.DYNAMIC);
    }

    public void addDynamicInteraction(DynamicInteraction dynamicInteraction) {
        this.dynamicInteractions.add(dynamicInteraction);
    }

    /*@Override
    protected void copyMappings(Map<Integer, CustomNode> customNodes, Map<Integer, CustomEdge> customEdges, Map<MultiNode, Integer> nodeIds, Map<Edge, Integer> edgeIds) {
       copyDynamicMappings(customNodes, customEdges, nodeIds, edgeIds);
    }*/

    /*@Override
    protected void addCustomEdge(Edge edge) {
        DynamicInteraction dynamicInteraction = new DynamicInteraction();
        this.addDynamicInteraction(edge, dynamicInteraction);
    }*/

    /**
     * Getter for the edge date of a certain edge.
     *
     * @param edge
     *            The edge.
     * @return The edge date.
     *//*
    public String getEdgeDate(Edge edge) {
        if(getCustomEdge(edge) instanceof DynamicInteraction) {
            DynamicInteraction result = (DynamicInteraction) getCustomEdge(edge);
            return result.getDate();
        }
       return "no date";
    }

    *//**
     * Setter for the edge date of a certain edge.
     *
     * @param edge
     *            The edge.
     * @param date
     *            The edge date.
     *//*
    public void setEdgeDate(Edge edge, String date) {
        if(getCustomEdge(edge) instanceof DynamicInteraction) {
            DynamicInteraction result = (DynamicInteraction) getCustomEdge(edge);
            result.setDate(date);
        }
    }

    *//**
     * Getter for the edge action of a certain edge.
     *
     * @param edge
     *            The edge.
     * @return The edge action.
     *//*
    public String getEdgeAction(Edge edge) {
        if(getCustomEdge(edge) instanceof DynamicInteraction) {
            DynamicInteraction result = (DynamicInteraction) getCustomEdge(edge);
            return result.getAction();
        }
        return "no action";
    }

    *//**
     * Setter for the edge action of a certain edge.
     *
     * @param edge
     *            The edge.
     * @param action
     *            The edge action.
     *//*
    public void setEdgeAction(Edge edge, String action) {
        if(getCustomEdge(edge) instanceof DynamicInteraction) {
            ((DynamicInteraction) getCustomEdge(edge)).setAction(action);
        }
    }*/
}
