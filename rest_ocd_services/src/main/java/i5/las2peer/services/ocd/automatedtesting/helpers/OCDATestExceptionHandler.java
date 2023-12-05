package i5.las2peer.services.ocd.automatedtesting.helpers;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.metrics.OcdMetricException;

public class OCDATestExceptionHandler {

    /**
     * Handles and rethrows exceptions to ensure they are captured by the test runner.
     *
     * This method plays a critical role in the context of running JUnit 5 tests programmatically using
     * the OCDTestRunner. When tests are executed via the OCDTestRunner, exceptions might only be
     * outputted to the console and not properly captured by the runner. This method ensures that all
     * exceptions, whether they are specific expected types (like OcdAlgorithmException, InterruptedException,
     * or OcdMetricException) or other unexpected types, are both logged and rethrown. By rethrowing the
     * exceptions, they are properly captured by the OCDTestRunner, allowing it to accurately report
     * on the executability of the auto-generated tests. The method is essential for the correct functioning
     * of the test evaluation process in the OCDTestRunner.
     *
     * @param e The exception encountered during the execution of a test.
     * @throws Exception The rethrown exception for external handling by the OCDTestRunner.
     */
    public static void handleException(Exception e) throws Exception {
        if (e instanceof OcdAlgorithmException || e instanceof InterruptedException || e instanceof OcdMetricException) {
            // Log and rethrow the expected exceptions
            e.printStackTrace();
            throw e;
        } else {
            // Unexpected exceptions will be thrown as is
            throw e;
        }
    }
}
