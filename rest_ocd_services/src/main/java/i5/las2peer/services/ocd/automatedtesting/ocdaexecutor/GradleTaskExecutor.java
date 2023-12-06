package i5.las2peer.services.ocd.automatedtesting.ocdaexecutor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes Gradle tasks programmatically and captures the results.
 * This class is designed to run specific Gradle tasks, especially those
 * related to testing, and to collect any test results or relevant output.
 * For example, auto-generated OCDA algorithm code can be tested for
 * successful execution at runtime.
 */
public class GradleTaskExecutor {

    /**
     * Runs a specified Gradle task for a given test class and captures the test results.
     *
     * @param taskName     The name of the Gradle task to run.
     * @param testClassName The fully qualified name of the test class to be executed.
     * @return A list of strings representing the captured results from the task execution.
     *         Each string in the list corresponds to a line of output, which may include test failure details.
     */
    public static List<String> runGradleTask(String taskName, String testClassName) {
        List<String> testResults = new ArrayList<>();
        try {
            String gradlew = System.getProperty("os.name").toLowerCase().startsWith("windows") ? "gradlew.bat" : "./gradlew";
            ProcessBuilder builder = new ProcessBuilder(gradlew, taskName, "-PtestClass=" + testClassName);
            builder.redirectErrorStream(true); // Redirect error stream to the output stream
            builder.directory(new File(System.getProperty("user.dir"))); // Assuming the current directory is the project root

            Process process = builder.start();

            // Read and print the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Gradle task exited with code: " + exitCode);

            // Read test results from the file
            File resultFile = new File(System.getProperty("user.dir"), "testResults.txt");
            if (resultFile.exists()) {
                try (BufferedReader fileReader = new BufferedReader(new FileReader(resultFile))) {
                    String resultLine;
                    while ((resultLine = fileReader.readLine()) != null) {
                        testResults.add(resultLine);
                    }
                }
                resultFile.delete(); // Optionally, delete the file after reading
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return testResults;
    }

    public static void main(String[] args) {
        // Example: Run 'runCustomTests' task for 'org.example.CalculatorTest' test class

        String generatedClassLocation = "i5.las2peer.services.ocd.ocdatestautomation.testrunner.generatedclasses.";
        String testClassName = "GeneratedSskAlgorithmTest";

        String fullTestClassPath = generatedClassLocation + testClassName;

        List<String> results = runGradleTask("runCustomTests", fullTestClassPath);
        System.out.println("printing results");
        results.forEach(System.out::println); // Print the results
    }
}
