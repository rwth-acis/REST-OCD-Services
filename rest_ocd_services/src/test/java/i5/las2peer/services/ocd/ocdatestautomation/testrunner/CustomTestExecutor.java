package i5.las2peer.services.ocd.ocdatestautomation.testrunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Executes a specified test class and writes the results to a file.
 * This class is used to run JUnit tests programmatically and capture any failures or errors.
 */
public class CustomTestExecutor {
    /**
     * The main method for executing tests and writing results to a file.
     * It expects the fully qualified name of a test class to be provided as a system property.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            String testClassName = System.getProperty("testClassName");
            if (testClassName != null) {
                Class<?> testClass = Class.forName(testClassName);
                CustomTestRunner runner = new CustomTestRunner();
                List<String> failureDetails = runner.runTests(testClass);


                // Define the path to the root directory of the Gradle project. Due to execution context differences
                // below line is necessary to ensure CustomTestExecutor and GradleTaskExecutor will write/read from
                // the same file
                File projectRoot = new File(System.getProperty("user.dir")).getParentFile();
                File resultFile = new File(projectRoot, "testResults.txt");

                // Write the failure details to a file in the project's root directory;
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
                    for (String detail : failureDetails) {
                        writer.write(detail);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No test class name provided.");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
