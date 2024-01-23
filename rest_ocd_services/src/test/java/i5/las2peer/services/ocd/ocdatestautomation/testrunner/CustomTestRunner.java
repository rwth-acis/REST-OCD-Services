package i5.las2peer.services.ocd.ocdatestautomation.testrunner;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom runner for executing JUnit tests programmatically.
 * It provides a method to run tests from a given class and collect failure details.
 */
public class CustomTestRunner {

    /**
     * Runs the tests in the specified class and collects details of any failures.
     *
     * @param testClass The class containing JUnit tests to be executed.
     * @return A list of strings where each string contains details of a test failure.
     */
    public List<String> runTests(Class<?> testClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();

        // Process the summary to extract failure details, including stack traces
        List<String> failureDetails = summary.getFailures().stream()
                .map(failure -> {
                    TestIdentifier testIdentifier = failure.getTestIdentifier();
                    Throwable exception = failure.getException();
                    // Generate error message about the failure
                    String errorMessage = testIdentifier.getDisplayName() + " failed: ";
                    // If exception string has a message use it, otherwise use stacktrace
                    List<String> exceptionString = Arrays.asList(exception.getMessage().split("\\s+"));
                    // Check if error string is null. Also take care of the case where graph on which the algorithm is executed is null, in which case the message also ends with null
                    if (!exceptionString.get(exceptionString.size() - 1).equals("null") || exceptionString.stream().anyMatch(s -> s.contains(CustomGraph.class.getSimpleName()))) {
                         errorMessage += exception.getMessage();
                    } else {
                        // If there is no message, get the first line of the stack trace
                        StackTraceElement[] stackTrace = exception.getStackTrace();
                        if (stackTrace.length > 0) {
                            errorMessage += stackTrace[0].toString();
                        } else {
                            errorMessage += "unknown reason!"; // No stack trace available
                        }
                    }

                    return errorMessage;
                })
                .collect(Collectors.toList());

        return failureDetails;
    }

    /**
     * Constructs a stack trace string for a given Throwable.
     *
     * @param throwable The Throwable from which to extract the stack trace.
     * @return A string representation of the stack trace.
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
