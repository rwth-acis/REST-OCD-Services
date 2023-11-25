package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.github.javaparser.JavaParser;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import com.github.javaparser.ast.CompilationUnit;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.io.File;

/**
 * The OCDTestRunner class provides functionality to run JUnit 5 tests
 * programmatically. It is used to check if the tests in a given OCD
 * test class can't be compiled due to some exception. The purpose
 * of this class is to be used in the calculation of the code
 * validity submetric for the auto-generated OCDA tests.
 */
public class OCDTestRunner {

    /**
     * Derives the full class name from a CompilationUnit and a file.
     * @param cu The CompilationUnit of the file.
     * @param file The file from which the class name is derived.
     * @return The full class name including the package.
     */
    private static String deriveFullClassName(CompilationUnit cu, File file) {
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getName().toString())
                .orElse("");
        String baseName = removeFileExtension(file.getName());
        return packageName.isEmpty() ? baseName : packageName + "." + baseName;
    }

    /**
     * Removes the file extension from a file name.
     * @param fileName The name of the file.
     * @return The file name without the extension.
     */
    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }


    /**
     * Runs JUnit 5 tests in a compiled test class and returns a list of exception messages. this method should only be executed on a class located on the correct path e.g. among other ocd algorithms to avoid class not found exception
     * @param testFile The file representing the test class.
     * @return List of strings representing exception messages.
     */

    /**
     * Executes JUnit 5 tests from a specified test class file and returns a list of formatted exception messages.
     *
     * This method dynamically loads a test class from the provided file, executes its JUnit 5 test cases, and
     * collects any exceptions thrown during the test execution. Each exception is formatted to provide key details
     * about the failure. It is crucial that the test class file is located in the correct classpath (e.g. placed
     * with other OCD algorithm test classes) to avoid a ClassNotFoundException. This method is particularly useful
     * for running tests programmatically in environments where traditional test runners may not be applicable.
     *
     * @param testFile The file representing the compiled test class. This file should exist on the correct test class path.
     * @return A List of strings, each representing a formatted message for an exception thrown during the test execution.
     * @throws Exception If any exception occurs during the test class loading or execution process.
     */
    public static List<String> runCompiledTestClassWithJUnit5(File testFile) throws Exception {

        // Compilation unit of the parsed OCDA test class
        CompilationUnit compilationUnit =  OCDAParser.parseJavaFile(testFile);

        // Load the test class
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{testFile.getParentFile().toURI().toURL()});
        String className = deriveFullClassName(compilationUnit, testFile);
        Class<?> testClass = Class.forName(className, true, classLoader);

        // Create a LauncherDiscoveryRequest
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build();

        // Create a Launcher
        Launcher launcher = LauncherFactory.create();

        // Register a listener to collect test execution details
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        // Execute the tests
        launcher.execute(request);

        // Create a list to hold exception messages
        List<String> exceptionMessages = new ArrayList<>();

        // Collect errors
        TestExecutionSummary summary = listener.getSummary();
        summary.getFailures().forEach(failure -> {
            TestIdentifier testIdentifier = failure.getTestIdentifier();
            Throwable exception = failure.getException();
            String errorMessage = formatExceptionMessage(testIdentifier, exception);
            exceptionMessages.add(errorMessage);
        });

        return exceptionMessages;
    }

    /**
     * Formats the exception message for a failed test.
     *
     * This method takes a TestIdentifier and a Throwable as inputs and
     * generates a formatted string that includes the test name, the exception
     * type, and the exception message. This string is intended to provide a
     * concise summary of why the test failed, making it easier to identify
     * issues quickly.
     *
     * @param testIdentifier The identifier of the test that failed.
     * @param throwable The exception that was thrown during the test execution.
     * @return A string that contains a formatted message with the test name,
     *         exception type, and exception message.
     */
    private static String formatExceptionMessage(TestIdentifier testIdentifier, Throwable throwable) {
        // Get the exception type and message
        String exceptionType = throwable.getClass().getName();
        String exceptionMessage = throwable.getMessage();

        return "Test '" + testIdentifier.getDisplayName() + "' failed throwing an " + "exception: '"
                + exceptionType + "' with the message: \"" +exceptionMessage + "\"";
    }

    /**
     * Converts the stack trace of a throwable into a string.
     *
     * This method is useful for logging or displaying the stack trace of an exception
     * in a human-readable format. It utilizes StringWriter and PrintWriter to capture
     * the stack trace of the provided Throwable and convert it into a String.
     *
     * @param throwable The throwable object whose stack trace is to be converted.
     * @return A string representation of the throwable's stack trace.
     */
    private static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
