package i5.las2peer.services.ocd.automatedtesting.ocdaexecutor;

import i5.las2peer.services.ocd.automatedtesting.helpers.PathResolver;

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
     * Executes a Gradle task using the specified command arguments.
     * This method dynamically determines the correct path to the Gradle wrapper script
     * based on the operating system and the current working directory and its parent.
     * This is used to deal with a project hierarchy.
     * It first checks if the appropriate gradle wrapper exists in the current working directory;
     * if not, it checks the parent directory. It then executes the Gradle task with the provided
     * command arguments and prints the output and exit status of the task.
     *
     * This approach allows the method to be flexible and work correctly regardless of whether it is
     * called from code or executed manually, accommodating environments where the
     * project structure includes child projects.
     *
     * @param command A list of strings representing the command and its arguments to execute the Gradle task.
     *                The first element should be the gradlew command, followed by the task name
     *                and any necessary flags or parameters.
     * @throws FileNotFoundException if the gradle wrapper file cannot be found in either the current or parent directory.
     * @throws IOException          if an I/O error occurs when executing the gradle command.
     * @throws InterruptedException if the current thread is interrupted while waiting for the process to finish.
     */
    private static void executeGradleTask(List<String> command) {
        try {
            // Determine the current directory and parent directory
            File currentDir = new File(System.getProperty("user.dir"));
            File parentDir = currentDir.getParentFile();

            // Determine the correct Gradle wrapper command for the operating system
            String gradlewCommand = getGradlewCommand();

            // Check if the Gradle wrapper script exists in the current directory, otherwise look in the parent directory
            File gradlewFile = new File(currentDir, gradlewCommand);
            if (!gradlewFile.exists()) {
                gradlewFile = new File(parentDir, gradlewCommand);
                if (!gradlewFile.exists()) {
                    throw new FileNotFoundException(gradlewCommand + " not found in current or parent directory");
                }
            }

            // Update the command to use the absolute path to the Gradle wrapper script
            List<String> updatedCommand = new ArrayList<>(command);
            updatedCommand.set(0, gradlewFile.getAbsolutePath()); // Replace the gradlew command with the full path

            // Set the working directory based on the location of the Gradle wrapper script
            File workingDirectory = gradlewFile.getParentFile();

            ProcessBuilder builder = new ProcessBuilder(updatedCommand);
            builder.redirectErrorStream(true);
            builder.directory(workingDirectory); // Set the working directory to where the Gradle wrapper script is found

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
     * Executes the 'pmdTest' Gradle task.
     * This method is used to programmatically run a Gradle task that executes PMD analysis on test sources.
     */
    public static void runPmdTest() {
        String gradlew = getGradlewCommand();
        executeGradleTask(Arrays.asList(gradlew, "pmdTest"));
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
        File currentDirFile = new File(System.getProperty("user.dir"), "testResults.txt");
        File parentDirFile = new File(new File(System.getProperty("user.dir")).getParent(), "testResults.txt");
        File resultFile = currentDirFile.exists() ? currentDirFile : (parentDirFile.exists() ? parentDirFile : null);

        if (!(resultFile == null)) {
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
     * Executes 'runGenerateAndWriteOCDAAccuracyTest' Gradle task for a given OCD algorithm name.
     * This method is used to programmatically run a gradle task that will generate OCD accuracy
     * test for a specified algorithm. The test is then merged into the main test class of the
     * algorithm. Before generating OCD accuracy tests, parameter values should be added to
     * ocda_parameters.json. The values should be created using GPT or a similar tool.
     * @param ocdaName      The name of the OCDA algorithm for which the test is generated.
     */
    public static void runGenerateAndWriteOCDAAccuracyTest(String ocdaName){

        String gradlew = getGradlewCommand();
        executeGradleTask(Arrays.asList(gradlew, "runGenerateAndWriteOCDAAccuracyTest", "-PocdaName=" + ocdaName));
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
