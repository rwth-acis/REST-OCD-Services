package i5.las2peer.services.ocd.automatedtesting.helpers;

public class FormattingHelpers {


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


}
