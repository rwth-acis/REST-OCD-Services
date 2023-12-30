package i5.las2peer.services.ocd.automatedtesting.ocdaexecutor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Common method to execute a Gradle task with the specified arguments.
     */
    private static void executeGradleTask(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.directory(new File(System.getProperty("user.dir")));

            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Gradle task exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

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
        String gradlew = getGradlewCommand();
        executeGradleTask(Arrays.asList(gradlew, taskName, "-PtestClass=" + testClassName));

        // Read test results from the file
        File resultFile = new File(System.getProperty("user.dir"), "testResults.txt");
        if (resultFile.exists()) {
            try (BufferedReader fileReader = new BufferedReader(new FileReader(resultFile))) {
                String resultLine;
                while ((resultLine = fileReader.readLine()) != null) {
                    testResults.add(resultLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            resultFile.delete(); // Optionally, delete the file after reading
        }
        return testResults;
    }

    /**
     * Executes the 'runInitializeGraphTypeTestRelatedFiles' Gradle task for a given OCD algorithm name.
     * This method is used to programmatically run a Gradle task that initializes test files related to
     * specific graph types compatible with the given OCDA algorithm. This includes creating a partially
     * competed test class file that should be compelted by GPT and also a reference test class file which
     * is used in the evaluation of auto-generated code
     *
     * @param ocdaName The name of the OCDA algorithm for which the test files are to be initialized.
     *                 This name is passed as a property to the Gradle task.
     */
    public static void runInitializeGraphTypeTestRelatedFiles(String ocdaName) {
        String gradlew = getGradlewCommand();
        executeGradleTask(Arrays.asList(gradlew, "runInitializeGraphTypeTestRelatedFiles", "-PocdaName=" + ocdaName));
    }

    /**
     * Executes the 'runInitializeOCDAMethodTestFiles' Gradle task for a given OCD algorithm name
     * and a list of method names. This method is used to programmatically run a Gradle task that
     * initializes test files related to specific methods of the OCDA algorithm. This includes
     * creating a partially competed test class file for testing specified methods from the OCDA
     * (if they exist) that should be completed by GPT and also a reference test class file
     * which is used in auto-generated code evaluation.
     * It constructs and executes a Gradle command with the necessary task name and properties.
     *
     * @param ocdaName     The name of the OCDA algorithm for which the test files are to be initialized.
     * @param methodNames  A list of method names within the OCDA algorithm to be tested.
     */
    public static void runInitializeOCDAMethodTestFiles(String ocdaName, List<String> methodNames) {
        String gradlew = getGradlewCommand();
        String methodNamesString = String.join(",", methodNames);
        executeGradleTask(Arrays.asList(gradlew, "runInitializeOCDAMethodTestFiles", "-PocdaName=" + ocdaName, "-PmethodNames=" + methodNamesString));
    }

    /**
     * Helper method to get the Gradle wrapper command based on the OS.
     *
     * @return The Gradle wrapper command.
     */
    private static String getGradlewCommand() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows") ? "gradlew.bat" : "./gradlew";
    }

    public static void main(String[] args) {
//        /* Execute a test class */
//
//        String generatedClassLocation = "i5.las2peer.services.ocd.ocdatestautomation.testrunner.generatedclasses.";
//        String testClassName = "GeneratedSskAlgorithmTest";
//
//        String fullTestClassPath = generatedClassLocation + testClassName;
//
//        List<String> results = runGradleTask("runCustomTests", fullTestClassPath);
//        System.out.println("printing results");
//        results.forEach(System.out::println); // Print the results


//        /* Generate partially completed test class files for graph type related tests */
//        //GradleTaskExecutor.runInitializeGraphTypeTestRelatedFiles("SskAlgorithm");

        /* Generate partially completed test class files for OCDA method related tests*/
//        List<String> methods = Arrays.asList("method1", "calculateMemberships", "getMaxDifference");
//        GradleTaskExecutor.runInitializeOCDAMethodTestFiles("SskAlgorithm", methods);
    }
}
