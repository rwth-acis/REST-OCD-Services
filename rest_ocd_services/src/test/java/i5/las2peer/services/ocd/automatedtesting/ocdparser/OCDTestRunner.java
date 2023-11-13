package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;

/**
 * The OCDTestRunner class provides functionality to run JUnit 5 tests
 * programmatically. It is used to check if the tests in a given OCD
 * test class can't be compiled due to some exception. The purpose
 * of this class is to be used in the calculation of the quality
 * metric for the auto-generated OCDA tests.
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
     * Runs JUnit 5 tests in a compiled test class.
     * @param compilationUnitTest The CompilationUnit of the test class.
     * @param testFile The file representing the test class.
     */
    public static void runCompiledTestClassWithJUnit5(CompilationUnit compilationUnitTest, File testFile) {

        String className = deriveFullClassName(compilationUnitTest, testFile);
        try {

            // Create a request to discover tests in the specified class
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectClass(Class.forName(className)))
                    .build();

            // Create a Launcher and a SummaryGeneratingListener
            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener listener = new SummaryGeneratingListener();

            // Execute the request with the listener
            launcher.execute(request, listener);

            // Get the test execution summary from the listener
            TestExecutionSummary summary = listener.getSummary();
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                Throwable exception = failure.getException();
                System.out.println("Exception in " + failure.getTestIdentifier().getDisplayName() + ": " + exception.getMessage());
                exception.printStackTrace(); // This will print the full stack trace
            }
            System.out.println("Test successful: " + summary.getTestsSucceededCount() + " out of " + summary.getTestsFoundCount());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Test class not found: " + className, e);
        }
    }
}
