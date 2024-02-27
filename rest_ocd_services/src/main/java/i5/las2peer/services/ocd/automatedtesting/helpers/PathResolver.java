package i5.las2peer.services.ocd.automatedtesting.helpers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolver {

    /**
     * Finds the root directory of the project by traversing up from the current directory.
     * The root is identified by a specific marker, such as the presence of a '.git' directory.
     * This method is useful for determining the project root both in a standalone and a Gradle context.
     *
     * @return The absolute path to the root directory of the project as a String.
     *         Returns an empty string if the root directory cannot be determined.
     */
    private static String findRootDirectory() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null && !isRootDirectory(current)) {
            current = current.getParent();
        }
        return current != null ? current.toString() : "";
    }

    /**
     * Checks if the given path is the root directory of the project.
     * The root directory is identified by the presence of a specific marker, such as a '.git' directory.
     *
     * @param path The path to check.
     * @return true if the path contains the marker indicating it is the root directory, false otherwise.
     */
    private static boolean isRootDirectory(Path path) {
        // Check for a unique marker in the root directory
        // For example, looking for a '.git' directory or a specific file
        File marker = path.resolve(".git").toFile();
        return marker.exists();
    }

    /**
     * Resolves a relative path to an absolute path based on the root directory of the project.
     * The root directory is dynamically determined based on the presence of a specific marker,
     * such as a '.git' directory. This method is useful for generating consistent file paths
     * regardless of whether the code is executed via Gradle or standalone.
     *
     * @param relativePath The relative path to be resolved.
     * @return The absolute path as a String, formed by appending the relative path to the project's root directory.
     */
    public static String resolvePath(String relativePath) {
        String rootDirectory = findRootDirectory();
        return Paths.get(rootDirectory, relativePath).toString();
    }

    /**
     * Prepend project root path if the system's projectRoot variable is set. This is used for certain gradle
     * tasks to avoid issues due to hierarchical structure of the WebOCD Gradle project.
     * @param initialPath
     * @return
     */
    public static String addProjectRootPathIfSet(String initialPath){
        String pathWithProjectRoot = System.getProperty("projectRoot") != null ? System.getProperty("projectRoot") + File.separator : "";
        return pathWithProjectRoot + initialPath;

    }

    // Example usage
    public static void main(String[] args) {
        String filePath = resolvePath("gpt/classfiles/GeneratedSskAlgorithmTest.java");
        System.out.println(filePath);
        // Now use this filePath to write your file
    }
}
