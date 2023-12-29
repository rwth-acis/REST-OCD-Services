package i5.las2peer.services.ocd.automatedtesting.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OCDWriter {

    /**
     * Generates or modifies a file at the specified path and writes the provided content to it.
     * If the file exists, this method will either append to or overwrite it based on the value of the append parameter.
     * The method uses a BufferedWriter to write the content.
     * In case of any IOExceptions, the exception is caught and its stack trace is printed.
     *
     * @param path    The full path of the file to be created or modified, relative to the repository root.
     * @param content The content to be written into the file.
     * @param append  If true, the content will be appended to the file; if false, the file will be overwritten.
     */
    public static void generateAndWriteFile(String path, String content, boolean append) {
        try {
            // Create a File object with the full path
            File file = new File(path);

            // Ensure the parent directories exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Create a new file if it doesn't exist
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }

            // Write content to file, append if append is true, overwrite otherwise
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
                writer.write(content);
                System.out.println("File has been " + (append ? "appended" : "written") + " at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Logs communication with GPT to a file with a timestamp and optionally prints to the console.
     *
     * @param textToLog     The text to be logged.
     * @param printToConsole True to print the logged text to the console, false otherwise.
     */
    public static void logGptCommunication(String textToLog, Boolean printToConsole){
        // Get the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = dateFormat.format(new Date());


        // Define the file path where the logged communication is written
        String filePath = "gpt/logs/gpt-communication.txt";

        // Write the current date and generatedPrompt to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            // Write the current date
            writer.write(currentDate);
            writer.newLine(); // Move to the next line

            // Write the generatedPrompt
            writer.write(textToLog);
            writer.newLine(); // Move to the next line

            writer.newLine(); // Add an empty line between log entries
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
