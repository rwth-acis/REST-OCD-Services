package i5.las2peer.services.ocd.automatedtesting.helpers;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattingHelpers {


    private static final Pattern VARIABLE_DECLARATION_PATTERN = Pattern.compile(
            "(?:public|protected|private|static|final|transient|volatile)?\\s+" + // Modifiers
                    "([\\w<>\\[\\],\\s]+)\\s+" + // Type (group 1)
                    "(\\w+)\\s*" + // Name (group 2)
                    "(?:=\\s*(.+))?;"); // Value (group 3, optional)

    /**
     * Capitalizes the first letter of a given string.
     *
     * This method converts the first character of a string to uppercase while leaving the rest of the string unchanged.
     * If the string is null or empty, it returns the string as is. This method is useful for formatting strings
     * where the first letter needs to be capitalized, such as in class names or certain types of identifiers.
     *
     * @param str The string whose first letter is to be capitalized.
     * @return The string with its first letter capitalized, or the original string if it is null or empty.
     */
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    /**
     * Converts an underscore-separated string to camelCase format.
     * This method is particularly useful for transforming constant-style
     * (uppercase with underscores) variable names into camelCase variable names.
     * For example, an input string "ZERO_WEIGHTS" will be converted to "zeroWeights".
     *
     * @param s The underscore-separated string to be converted.
     * @return The camelCase version of the input string. If the input is empty
     *         or only contains underscores, an empty string is returned.
     */
    public static String toCamelCaseFromUnderscore(String s) {
        String[] parts = s.trim().split("_"); // Split on underscores
        if (parts.length == 0) return "";

        StringBuilder camelCaseString = new StringBuilder(parts[0].toLowerCase()); // First word stays as it is

        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
        }

        return camelCaseString.toString();
    }

    /**
     * Converts a camelCase string to an uppercase string with underscores between words,
     * and appends "_NAME" at the end.
     *
     * @param camelCaseStr The camelCase string to be converted.
     * @return A converted string in uppercase with underscores and "_NAME" appended.
     */
    public static String convertCamelCaseToUpperCaseWithUnderscores(String camelCaseStr) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";

        // Replace the camelCase format with underscores and convert to uppercase
        String upperCaseWithUnderscores = camelCaseStr.replaceAll(regex, replacement).toUpperCase();

        // Append "_NAME" at the end
        return upperCaseWithUnderscores + "_NAME";
    }

    /**
     * Converts a string in uppercase with underscores and "_NAME" appended
     * to a camelCase string.
     *
     * @param upperCaseStr The uppercase string with underscores and "_NAME" to be converted.
     * @return A converted string in camelCase format.
     */
    public static String convertUpperCaseWithUnderscoresToCamelCase(String upperCaseStr) {
        // Remove the "_NAME" part
        String noNameStr = upperCaseStr.replaceFirst("_NAME$", "");

        // Split the string on underscores
        String[] words = noNameStr.split("_");

        // Convert the first word to lowercase
        StringBuilder camelCaseStr = new StringBuilder(words[0].toLowerCase());

        // Process the remaining words
        for (int i = 1; i < words.length; i++) {
            camelCaseStr.append(words[i].substring(0, 1).toUpperCase())
                    .append(words[i].substring(1).toLowerCase());
        }

        return camelCaseStr.toString();
    }



    /**
     * Extracts the name of a variable from its declaration.
     *
     * @param variableDeclaration The string representing the variable declaration.
     * @return The extracted name of the variable.
     * @throws IllegalArgumentException If the provided string does not match the expected format.
     */
    public static String extractVariableName(String variableDeclaration) {
        Matcher matcher = getVariableDeclarationMatcher(variableDeclaration);
        if (matcher.matches()) {
            return matcher.group(2); // The variable name is in the second capturing group
        } else {
            throw new IllegalArgumentException("Invalid variable declaration format.");
        }
    }

    /**
     * Extracts the value of a variable from its declaration.
     *
     * @param variableDeclaration The string representing the variable declaration.
     * @return The extracted value of the variable, or an empty string if no value is assigned.
     * @throws IllegalArgumentException If the provided string does not match the expected format.
     */
    public static String extractVariableValue(String variableDeclaration) {
        Matcher matcher = getVariableDeclarationMatcher(variableDeclaration);
        if (matcher.matches()) {
            return matcher.group(3) != null ? matcher.group(3).trim() : ""; // The variable value is in the third capturing group
        } else {
            throw new IllegalArgumentException("Invalid variable declaration format.");
        }
    }

    /**
     * Creates a Matcher for variable declaration patterns.
     *
     * @param variableDeclaration The string representing the variable declaration.
     * @return A Matcher object for the variable declaration pattern.
     */
    private static Matcher getVariableDeclarationMatcher(String variableDeclaration) {
        return VARIABLE_DECLARATION_PATTERN.matcher(variableDeclaration);
    }


    /**
     * Creates a variable name for a graph instantiation to be used in unit tests based on the provided graph type string.
     * This method processes a graph type string (expected to be in a format like "GraphType.DIRECTED") and converts the
     * portion after the dot into camelCase, appending "Graph" at the end. For instance, "GraphType.DIRECTED" would be
     * converted to "directedGraph". This is particularly useful for generating descriptive and consistent variable names
     * in unit test code.
     *
     * @param compatibleGraphTypeString A string representing the graph type, in a format like "GraphType.SOMETYPE".
     * @return A string that represents the camelCase variable name for the graph, suitable for use in unit test code.
     */
    public static String generateGraphVariableName(String compatibleGraphTypeString){
        return toCamelCaseFromUnderscore(compatibleGraphTypeString.split("\\.")[1]) + "Graph";
    }

    /**
     * Replaces all occurrences of a constant name with a variable name in a given Java class implementation string.
     * The value of the variable name is also surrounded with quotes. This method preserves the original formatting
     * of the Java class implementation. This is used for OCD algorithm parameters in prompt generation.
     *
     * @param parameterVariableNameConstant The constant name to be replaced.
     * @param parameterVariableName The variable name that replaces the constant name.
     * @param setParametersImplementation The Java class implementation string where replacements are to be made.
     * @return The modified Java class implementation with replacements made.
     */
    public static String replaceConstantWithVariable(String parameterVariableNameConstant, String parameterVariableName, String setParametersImplementation) {
        // Replace the constant name with the variable name, and surround the variable name with quotes
        String replacedString = setParametersImplementation.replace(parameterVariableNameConstant, "\"" + parameterVariableName + "\"");

        return replacedString;
    }


    /**
     * Extracts the last part of a string separated by dots, or returns the input string as is
     * if there are no dots.
     *
     * @param input The input string to extract the last part from.
     * @return The last part of the input string or the input string itself if there are no dots.
     * @throws IllegalArgumentException If the input string is null or empty.
     */
    public static String extractLastPart(String input) {
        // Check if the input string is null or empty
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }

        // Split the string by dots
        String[] parts = input.split("\\.");

        // Extract the last part or leave it as is if there are no dots
        String lastPart = parts.length > 0 ? parts[parts.length - 1] : input;

        return lastPart;
    }


    /**
     * Converts the values in a map from Object to String. This method is typically used for converting
     * parsed JSON values to their string representations.
     *
     * @param parsedMap The original map with Object values.
     * @return A map with the same keys as the input, but where each value is a list of string representations
     *         of the original values.
     */
    public static Map<String, List<String>> convertValuesToStrings(Map<String, Object> parsedMap) {
        Map<String, List<String>> stringifiedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : parsedMap.entrySet()) {
            String key = entry.getKey(); // Assume key is already a string

            List<String> stringifiedValues = new ArrayList<>();
            List<?> values = (List<?>) entry.getValue();
            for (Object value : values) {
                stringifiedValues.add(value.toString()); // Convert each value to String
            }

            stringifiedMap.put(key, stringifiedValues);
        }

        return stringifiedMap;
    }

    /**
     * Extracts a JSON string from the input text and converts it to a map. The method searches for
     * the first occurrence of a JSON-like object within the input string and attempts to parse it.
     *
     * @param input The string potentially containing a JSON object.
     * @return A map representation of the JSON object.
     * @throws IllegalArgumentException if no valid JSON object is found in the input string.
     */
    public static Map<String, Object> extractJsonAndConvertToMap(String input) {
        // Regular expression to find the outermost curly braces
        String jsonRegex = "\\{[^{}]*\\}";
        Pattern pattern = Pattern.compile(jsonRegex);
        Matcher matcher = pattern.matcher(input);

        // Find the first occurrence that looks like a JSON object
        while (matcher.find()) {
            String potentialJson = matcher.group();

            try {
                // Try to parse the potential JSON
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(potentialJson);

                // Convert to Map and return if successful
                return (Map<String, Object>) jsonObject;
            } catch (ParseException e) {
                // If parsing fails, continue to the next match
            }
        }

        throw new IllegalArgumentException("No valid JSON found in the input string.");
    }

    /**
     * Converts a list of maps into a JSON string. Each map in the list represents a set of key-value pairs,
     * which is converted into a JSON object. The entire list is then represented as a JSON array of these objects.
     *
     * @param list The list of maps to be converted to JSON. Each map in the list should represent a set of key-value pairs.
     * @return A JSON string representing the list of maps as a JSON array.
     */
    public static String convertListToJSONString(ArrayList<Map<String, String>> list) {
        JSONArray jsonArray = new JSONArray();

        for (Map<String, String> map : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(map);
            jsonArray.add(jsonObject);
        }

        return jsonArray.toJSONString();
    }



}
