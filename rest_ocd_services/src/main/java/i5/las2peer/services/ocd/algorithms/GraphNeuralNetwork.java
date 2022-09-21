package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import okhttp3.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphNeuralNetwork implements OcdAlgorithm{

    @Override
    /**
     * the function detectOverlappingCommunity is calling a web server where the python code is stored in it under the path
     * http://127.0.0.1:5000/training
     */
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("http://127.0.0.1:5000/training")
                .method("GET", body)
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
        /**
         * this method should return a cover and the cover that is returned can easily be built using teh cover class constructor
         */
    }

    @Override
    public CoverCreationType getAlgorithmType() {

        return CoverCreationType.GRAPH_NEURAL_NETWORK_ALGORITHM;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        compatibilities.add(GraphType.WEIGHTED);
        compatibilities.add(GraphType.DIRECTED);
        return compatibilities;
    }

    @Override
    public void setParameters(Map<String, String> parameters) {

    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }
}
