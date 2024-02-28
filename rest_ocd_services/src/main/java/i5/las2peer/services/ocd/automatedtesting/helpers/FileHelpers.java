package i5.las2peer.services.ocd.automatedtesting.helpers;

import i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants.*;

public class FileHelpers {

    /**
     * Moves a file from a specified source path to a specified destination path.
     * If the source file does not exist, the method does nothing.
     * If the destination file exists, it can be optionally replaced based on the given flag.
     *
     * @param from The source file path.
     * @param to The destination file path.
     * @param replaceExisting If true, replaces the file at the destination if it exists.
     */
    public static void moveFile(String from, String to, boolean replaceExisting) {
        Path sourcePath = Paths.get(from);

        // Check if the source file exists
        if (!Files.exists(sourcePath)) {
            System.out.println("Source file does not exist. No action taken.");
            return;
        }

        Path destinationPath = Paths.get(to);

        try {
            if (replaceExisting) {
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(sourcePath, destinationPath);
            }
            System.out.println("File moved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to move the file.");
        }
    }

    /**
     * Constructs the absolute file path from a relative file path.
     *
     * @param relativeFilePath The relative path to the file.
     * @return The absolute file path.
     */
    private static String constructFilePath(String relativeFilePath) {
        String contentRoot = System.getProperty("user.dir");
        return contentRoot + File.separator + relativeFilePath;
    }

    /**
     * Cleans a file path by removing consecutive duplicate directory names.
     * This method helps in rectifying file paths where a directory name might have
     * been mistakenly repeated. For example, if the original path is
     * "folder\\folder\\subfolder\\file.txt", it will be cleaned to "folder\\subfolder\\file.txt".
     *
     * @param filePath The original file path that may contain duplicate directory names.
     * @return A cleaned file path with consecutive duplicate directory names removed.
     */
    public static String cleanDuplicateDirectories(String filePath) {
        // Normalize file separators to system-dependent separator
        String normalizedPath = filePath.replace("/", File.separator).replace("\\", File.separator);
        String[] parts = normalizedPath.split(File.separator.equals("\\") ? "\\\\" : File.separator);
        StringBuilder cleanedPath = new StringBuilder(parts.length > 0 ? parts[0] : "");

        // Iterate through path parts and append if not a duplicate of the previous
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].equals(parts[i - 1])) {
                cleanedPath.append(File.separator).append(parts[i]);
            }
        }

        return cleanedPath.toString();
    }




    /**
     * Returns the file path to the main OCD algorithm code.
     * This method constructs the absolute path to the specified OCDA class file within the project's
     * directory structure.
     *
     * @param ocdaFileName The name of the OCDA class file.
     * @return A string representing the absolute path to the OCDA class file.
     */
    public static String getOCDAPath(String ocdaFileName){
        String relativeFilePath = OCDA_LOCATION_IN_MAIN_SOURCE_SET + ocdaFileName;
        return cleanDuplicateDirectories(constructFilePath(relativeFilePath));
    }



    /**
     * Returns the file path to the main OCD algorithm test class.
     * This method constructs the absolute path to the specified OCD algorithm
     * test class file within the project's test directory.
     *
     * @param ocdaTestFileName The name of the OCDA test class file.
     * @return A string representing the absolute path to the OCDA test class file.
     */
    public static String getOCDATestPath(String ocdaTestFileName){
        String relativeFilePath = OCDA_LOCATION_IN_TEST_SOURCE_SET + ocdaTestFileName;
        return cleanDuplicateDirectories(constructFilePath(relativeFilePath));
    }


    /**
     * Returns the file path to the auto-generated test class code.
     * The tests from this class are used for merging into the main OCD test class. This method constructs the
     * absolute path to the auto-generated OCDA test class within the project's test automation directory.
     *
     * @param ocdaName The name of the auto-generated OCD algorithm test class.
     * @return A string representing the absolute path to the auto-generated OCDA test class file.
     */
    public static String getAutoGeneratedTestPath(String ocdaName) {
        String relativeFilePath = PARSED_GPT_GENERATED_CODE_LOCATION + ocdaName;
        return constructFilePath(relativeFilePath);
    }

    /**
     * Returns the file path to the auto-generated unparsed test class code. This code should be parsed
     * and if parsing is successful, it should be moved to the test source to be tested for execution.
     *
     * @param generatedOCDATestClassFileName The name of the auto-generated, unparsed OCD algorithm test class.
     * @return A string representing the absolute path to the auto-generated OCDA test class file.
     */
    public static String getAutoGeneratedUnparsedTestPath(String generatedOCDATestClassFileName){
        return GPT_GENERATED_TEST_CLASS_CODE_LOCATION + generatedOCDATestClassFileName;

    }

    /**
     * Returns the file path to the file that contains unprocessed unit tests generated by GPT. Since these
     * are not yet processed, apart from unit tests, other content might be present, which should be removed
     * by processing.
     *
     * @param fileWithUnprocessedUnitTestsName The name of the unprocessed file that holds generated unit tests
     * @return A string representing the absolute path to the unprocessed unit test file.
     */
    public static String getAutoGeneratedUnprocessedUnitTestPath(String fileWithUnprocessedUnitTestsName){
        return GPT_GENERATED_UNPROCESSED_UNIT_TESTS_LOCATION + fileWithUnprocessedUnitTestsName;

    }

    /**
     * Retrieves the file object for a specified OCD algorithm.
     * The method constructs the file path by appending the algorithm name
     * to the base directory defined in OCDA_LOCATION_IN_MAIN_SOURCE_SET.
     *
     * @param ocdaName The name of the OCD algorithm.
     * @return A File object representing the algorithm's source file.
     */
    public static File getAlgorithmFile(String ocdaName) {
        // Construct the file path
        String filePath = OCDA_LOCATION_IN_MAIN_SOURCE_SET + ocdaName + ".java";

        // Create and return the File object
        return new File(filePath);
    }



    /**
     * Reads a file and returns its content as a string.
     *
     * @param filePath The path of the file to be read.
     * @return The content of the file as a string.
     */
    public static String readFileAsString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return new String(Files.readAllBytes(path));
        }catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     * Reads a file and returns its content as a string.
     *
     * @param file The file to be read.
     * @return The content of the file as a string.
     */
    public static String readFileAsString(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

}
