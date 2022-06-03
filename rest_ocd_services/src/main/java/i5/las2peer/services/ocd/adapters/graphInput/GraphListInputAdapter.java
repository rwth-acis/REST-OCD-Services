package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphListInputAdapter implements GraphInputAdapter {

    private LinkedList<Integer> graphIdList = null;

    @Override
    public CustomGraph readGraph() throws AdapterException {
        CustomGraph graph = new CustomGraph();
        for(int graphId:graphIdList){
            graph.addGraphIntoGraphSeries(getCustomGraph(graphId));//id=3
        }
        return graph;
    }

    @Override
    public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {
        String[] graphListStr=null;
        if(param.containsKey("graphList")){
            graphListStr = param.get("graphList").split("_");//3_4_5_
            for(String graphIdStr:graphListStr){
                graphIdList.add(Integer.parseInt(graphIdStr));
            }//[3,4,5]
        }
    }

    @Override
    public Reader getReader() {
        return null;
    }

    @Override
    public void setReader(Reader reader) {

    }
}
