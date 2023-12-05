package i5.las2peer.services.ocd.automatedtesting.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OCDWriter {

    /**
     * Generates a file at the specified relative path within the specified directory and writes the provided content to it.
     * If a file at the given path already exists, it will be overwritten. The method
     * creates a new file and uses a BufferedWriter to write the content. In case of any
     * IOExceptions, the exception is caught and its stack trace is printed.
     *
     *
     * @param dirName      The name of the directory where the content should be written
     * @param relativePath The relative path (within the dirname directory) where the file will be created or overwritten.
     * @param content      The content to be written into the file.
     */
    public static void generateAndWriteFile(String dirName, String relativePath, String content) {
        try {
            // Define the base directory
            File baseDirectory = new File(dirName);

            // Create the base directory if it does not exist
            if (!baseDirectory.exists()){
                baseDirectory.mkdir();
            }

            // Create a File object with the full path
            File file = new File(baseDirectory, relativePath);

            // Ensure the parent directories exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Create a new file (or overwrite an existing one)
            if (!file.createNewFile() && !file.exists()) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }

            // Write content to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
                System.out.println("File '" + relativePath + "' has been generated and written in '" + baseDirectory.getAbsolutePath() + "'.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
