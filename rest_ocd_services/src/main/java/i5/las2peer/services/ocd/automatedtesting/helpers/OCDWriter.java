package i5.las2peer.services.ocd.automatedtesting.helpers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

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
     * @param ocdaName       Name of the OCD algorithm for which the communication is logged.
     */
    public static void logGptCommunication(String textToLog, Boolean printToConsole, String ocdaName){
        // Get the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = dateFormat.format(new Date());


        // Define the file path where the logged communication is written
        String filePath = "gpt/logs/" + ocdaName + "-gpt-communication.txt";

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



    /**
     * Writes or updates the parameter data for a specific OCD algorithm in a JSON file. If the file exists, the method
     * updates the existing JSON object with the new data for the specified algorithm. If the file does not exist,
     * it creates a new JSON object and writes it to the file.
     *
     * @param filePath The file path where the JSON data is stored.
     * @param algorithmName The name of the algorithm for which data is being written or updated.
     * @param finalParametersJsonString The JSON string representing the data to be written for the algorithm.
     */
    public static void writeAlgorithmDataToFile(String filePath, String algorithmName, String finalParametersJsonString) {
        try {
            JSONArray algorithmData = (JSONArray) new JSONParser().parse(finalParametersJsonString);
            JSONObject rootObject = new JSONObject();

            // Check if the file exists and is not empty
            Path path = Paths.get(filePath);
            if (Files.exists(path) && Files.size(path) > 0) {
                rootObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
            }

            // Update the algorithm data
            rootObject.put(algorithmName, algorithmData);

            // Write to the file
            try (FileWriter file = new FileWriter(filePath)) {
                file.write(rootObject.toJSONString());
            }
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing or writing JSON: " + e.getMessage());
        }
    }


    /**
     * Reads and returns the data for a specific algorithm from a JSON file. This method parses the JSON file and
     * retrieves the data associated with the specified algorithm.
     *
     * @param filePath      The file path where the JSON data is stored.
     * @param algorithmName The name of the algorithm for which data is being retrieved.
     * @return A Map containing the algorithm data if found, or null if an error occurs, if the file is empty,
     *         incomplete, or if the algorithm data does not exist.
     */
    public static Map<String, List<String>> readAlgorithmDataFromFile(String filePath, String algorithmName) {
        try {
            // Check if the file exists and is not empty
            if (!Files.exists(Paths.get(filePath)) || Files.size(Paths.get(filePath)) == 0) {
                System.out.println("File is empty or does not exist.");
                return null;
            }

            JSONParser parser = new JSONParser();
            JSONObject rootObject = (JSONObject) parser.parse(new FileReader(filePath));

            if (rootObject == null || !rootObject.containsKey(algorithmName)) {
                System.out.println("Algorithm data not found or JSON is incomplete.");
                return null;
            }

            JSONArray algorithmDataArray = (JSONArray) rootObject.get(algorithmName);
            Map<String, List<String>> algorithmData = new HashMap<>();

            for (Object item : algorithmDataArray) {
                JSONObject paramObject = (JSONObject) item;
                for (Object key : paramObject.keySet()) {
                    String paramName = (String) key;
                    String paramValue = (String) paramObject.get(key);

                    algorithmData.computeIfAbsent(paramName, k -> new ArrayList<>()).add(paramValue);
                }
            }
            return algorithmData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
