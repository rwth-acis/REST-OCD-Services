package i5.las2peer.services.ocd.test_interfaces.ocda;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;


import java.io.FileNotFoundException;

/**
 * This interface holds parts of the OCDA tests that are shared and
 * are independent of the graph type the OCDA is compatible with.
 */
public interface BaseGraphTestReq {

    OcdAlgorithm getAlgorithm();

}
