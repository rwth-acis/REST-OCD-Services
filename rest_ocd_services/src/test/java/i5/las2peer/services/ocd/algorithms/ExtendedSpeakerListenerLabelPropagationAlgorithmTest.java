package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedSpeakerListenerLabelPropagationAlgorithmTest implements DirectedGraphTestReq {

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