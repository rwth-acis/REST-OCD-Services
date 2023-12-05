package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.DirectedGraphTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.OCDAParameterTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.WeightedGraphTestReq;
import org.junit.jupiter.api.BeforeEach;

class ExtendedSpeakerListenerLabelPropagationAlgorithmTest implements DirectedGraphTestReq, UndirectedGraphTestReq,
        WeightedGraphTestReq, OCDAParameterTestReq {

    OcdAlgorithm algo;

    @BeforeEach
    public void setup() {
        algo = new ExtendedSpeakerListenerLabelPropagationAlgorithm();
    }

    @Override
    public OcdAlgorithm getAlgorithm() {
        return algo;
    }


}