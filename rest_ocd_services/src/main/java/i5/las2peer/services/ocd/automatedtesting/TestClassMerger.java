package i5.las2peer.services.ocd.automatedtesting;


import com.github.javaparser.StaticJavaParser;
import i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers;
import i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestClassMerger {

    /**
     * Merges unit tests from a source test class into a target test class.
     * The method will replace tests in the target class with those from the source class if they have the same name.
     *
     * @param targetClass File path to the target test class where tests will be added.
     * @param sourceClass File path to the source test class from which tests are extracted.
     */
    public static void mergeTestClasses(File targetClass, File sourceClass) {
        try {
            // Parse the target and source Java files into CompilationUnit objects.
            // These objects represent the abstract syntax tree of the parsed files.
            CompilationUnit targetCu = OCDAParser.parseJavaFile(targetClass);
            CompilationUnit sourceCu = OCDAParser.parseJavaFile(sourceClass);

            // Retrieve the primary class or interface declaration from the target test class.
            // This is where the methods from the source class will be added or replaced.
            ClassOrInterfaceDeclaration targetClassDecl = targetCu.findFirst(ClassOrInterfaceDeclaration.class)
                    .orElseThrow(() -> new IllegalStateException("Target test class does not contain a class or interface declaration"));

            // A set to store the names of all methods in the target class.
            // This helps to quickly check if a method with the same name already exists.
            Set<String> targetMethodNames = new HashSet<>();
            targetClassDecl.getMethods().forEach(method -> targetMethodNames.add(method.getNameAsString()));

            // Iterate over all method declarations in the source class.
            // Filter to include only those methods that are annotated with @Test annotation.
            sourceCu.findAll(MethodDeclaration.class).stream()
                    .filter(method -> method.isAnnotationPresent("Test"))
                    .forEach(method -> {
                        // Clone each test method from the source class.
                        // This is to avoid modifying the original source class.
                        MethodDeclaration clonedMethod = method.clone();
                        String methodName = clonedMethod.getNameAsString();

                        // Check if a method with the same name already exists in the target class.
                        // If it does, remove the existing method(s) before adding the new one.
                        if (targetMethodNames.contains(methodName)) {
                            targetClassDecl.getMethodsByName(methodName).forEach(m -> targetClassDecl.remove(m));
                        }
                        // Add the cloned method to the target class.
                        targetClassDecl.addMember(clonedMethod);
                    });

            // Write the modified target CompilationUnit back to the file.
            // This effectively updates the target test class with the merged test methods.
            try (FileWriter fileWriter = new FileWriter(targetClass)) {
                fileWriter.write(targetCu.toString());
            }
        } catch (IOException e) {
            // Handle any IO exceptions that occur during file parsing or writing.
            e.printStackTrace();
            System.out.println("Error during merging test classes: " + e.getMessage());
        }
    }

    /**
     * Merges a list of unit tests (as strings) into a target test class file.
     * It adds the tests to the class, replacing any existing tests with the same name.
     *
     * @param targetClassFile File object representing the target test class where tests will be added.
     * @param unitTests List of unit test method strings to be added.
     */
    public static void mergeUnitTestsIntoClass(File targetClassFile, List<String> unitTests) {
        try {
            CompilationUnit targetCu = OCDAParser.parseJavaFile(targetClassFile);

            ClassOrInterfaceDeclaration targetClassDecl = targetCu.findFirst(ClassOrInterfaceDeclaration.class)
                    .orElseThrow(() -> new IllegalStateException("Target test class does not contain a class or interface declaration"));

            Set<String> targetMethodNames = new HashSet<>();
            targetClassDecl.getMethods().forEach(method -> targetMethodNames.add(method.getNameAsString()));

            for (String testMethodStr : unitTests) {
                MethodDeclaration testMethod = StaticJavaParser.parseMethodDeclaration(testMethodStr);
                String methodName = testMethod.getNameAsString();

                if (targetMethodNames.contains(methodName)) {
                    targetClassDecl.getMethodsByName(methodName).forEach(targetClassDecl::remove);
                }
                targetClassDecl.addMember(testMethod);
            }

            try (FileWriter fileWriter = new FileWriter(targetClassFile)) {
                fileWriter.write(targetCu.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during merging unit tests into class: " + e.getMessage());
        }
    }


    /**
     * Merges a single unit test (as string) into a target test class file.
     * It adds the tests to the class, replacing any existing tests with the same name.
     *
     * @param targetClassFile File object representing the target test class where tests will be added.
     * @param unitTestString Unit test method strings to be added.
     */
    public static void mergeUnitTestIntoClass(File targetClassFile, String unitTestString) {
        List<String> oneElementList = Arrays.asList(unitTestString);
        mergeUnitTestsIntoClass(targetClassFile, oneElementList);
    }

    /**
     * This method modifies a Java file by moving the "// Don't modify" comments from being standalone lines
     * to inline comments at the end of the following line. This helps maintain the readability of the code
     * while preserving the intent of the comment.
     *
     * @param file The Java file to be modified.
     */
    public static void processComments(File file)  {
        // StringBuilder to accumulate the modified content of the file.
        StringBuilder modifiedContent = new StringBuilder();
        // System-dependent line separator to maintain file's original line breaks.
        String lineSeparator = System.lineSeparator();
        // To store the indentation (whitespace) of the line containing the "// Don't modify" comment.
        String commentIndentation = "";
        // Flag to indicate whether the comment should be appended to the next line.
        boolean appendComment = false;
        // The comment string to be identified and moved.
        String dontModifyComment = "// "+ OCDATestAutomationConstants.DONT_MODIFY_COMMENT_STRING;

        // Pattern to match the leading whitespace (indentation) of a line.
        Pattern indentationPattern = Pattern.compile("^(\\s*)");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (appendComment) {
                    // If the flag is set, append the comment to this line with the stored indentation.
                    line = "\n"+ commentIndentation + line.trim() + " " + dontModifyComment +"\n";
                    appendComment = false; // Reset the flag.
                }
                // Find and store the indentation of the current line.
                Matcher matcher = indentationPattern.matcher(line);
                if (matcher.find()) {
                    commentIndentation = matcher.group(1);
                }
                // Check if the line contains only the "// Don't modify" comment.
                if (line.trim().equals(dontModifyComment)) {
                    appendComment = true; // Set flag to append the comment to the next line.
                    continue; // Skip adding this line to the modified content.
                }
                // Add the current line to the modified content.
                modifiedContent.append(line).append(lineSeparator);
            }
        }  catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Write the modified content back to the file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(modifiedContent.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts unit tests from a provided text.
     * This method uses a regular expression to identify Java unit test methods,
     * optionally including their Javadoc comments, and returns them as a list of strings.
     * The method aims to match test methods annotated with @Test and their preceding JavaDoc comments.
     *
     * @param text The text from which unit tests are to be extracted. This text can be a mix of code and non-code.
     *             For example, a response from a GPT.
     * @return A list of strings, each representing a unit test method along with its Javadoc comment (if any).
     */
    public static List<String> extractUnitTestsFromText(String text) {
        List<String> unitTests = new ArrayList<>();

        String patternString = "(?s)(/\\*\\*.*?\\*/\\s*)?@Test.*?public void.*?\\(.*?\\).*?\\{";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String methodStart = matcher.group();
            int methodEnd = findMethodEnd(text, matcher.end());
            unitTests.add(text.substring(matcher.start(), methodEnd));
        }

        return unitTests;
    }

    /**
     * Finds the end index of a method in a text starting from a given index.
     * This method counts the number of opening and closing braces to find the end of the method.
     * It assumes that the method starts with an opening brace and counts until the braces balance out.
     *
     * @param text The text containing the method.
     * @param start The index in the text where the method starts.
     * @return The index in the text where the method ends. If the method is unbalanced (missing closing brace),
     *         it returns the start index.
     */
    private static int findMethodEnd(String text, int start) {
        int openBraces = 1;
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) == '{') {
                openBraces++;
            } else if (text.charAt(i) == '}') {
                openBraces--;
                if (openBraces == 0) {
                    return i + 1;
                }
            }
        }
        return start; // In case of an unbalanced method, return the start of the method
    }

    /**
     * Extracts unit tests from a file.
     * This method reads the content of the file specified by the file path,
     * and then extracts the unit tests using the extractUnitTestsFromText method.
     *
     * @param filePath The path of the file from which unit tests are to be extracted.
     * @return A list of strings, each representing a unit test method.
     * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    public static List<String> extractUnitTestsFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return extractUnitTestsFromText(content);
    }


    /**
     * Extracts unit tests from a specified file and adds them to the target class file.
     * If a unit test with the same name already exists in the target class file, it is replaced.
     * This method first extracts unit tests from the specified unit test file, then merges
     * these tests into the target class file.
     *
     * @param targetClassFilePath The file path of the target class file where the unit tests will be merged.
     * @param unitTestFilePath The file path of the class file from which unit tests are extracted.
     */
    public static void extractAndMergeUnitTests(String targetClassFilePath, String unitTestFilePath) {

        try {
            // Extract unit tests from a file
            List<String> extractedTestsFromFile = extractUnitTestsFromFile(unitTestFilePath);
            //extractedTestsFromFile.forEach(System.out::println);

            // Merge extracted unit tests into a test class
            File targetClassFile =  new File(targetClassFilePath);
            mergeUnitTestsIntoClass(targetClassFile, extractedTestsFromFile);

            // Improve inline comment placement in the code
            processComments(targetClassFile);

        }  catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Extracts unit tests from a specified file and writes them into a new temporary Java class file.
     * This temporary java class file is needed to use java parser for parsing the unit tests.
     * This method reads unit tests from the given unit test file path, formats them into a temporary Java class
     * structure, and writes the result to the target file path. If the target and unit test file paths are the same,
     * it overwrites the original file. The method includes package and class declarations in the output file.
     *
     * @param targetFilePath      The file path where the Java class with the unit tests will be written.
     * @param unitTestFilePath    The file path from which to extract the unit tests.
     */
    public static void extractAndWriteUnitTests(String targetFilePath, String unitTestFilePath) {
        try {
            // Extract unit tests from a file
            List<String> extractedTestsFromFile = extractUnitTestsFromFile(unitTestFilePath);
            //System.out.println("number of extracted tests " + extractedTestsFromFile.size());
            //extractedTestsFromFile.forEach(extractedTest -> System.out.println(extractedTest ));

            // Build the content for the Java class
            StringBuilder classContent = new StringBuilder();
            classContent.append("package example.org;\n\n");
            classContent.append("public class TemporaryClassForParsingTesting {\n\n");

            // Append each extracted unit test to the class content
            for (String unitTest : extractedTestsFromFile) {
                classContent.append(unitTest).append("\n\n");
            }

            classContent.append("}\n");

            // if the source and target files for writing are the same, delete content of the file before writing
            if (targetFilePath.equals(unitTestFilePath)) {
                new File(unitTestFilePath).delete();
            }

            // Write the complete class content to the file
            OCDWriter.generateAndWriteFile(targetFilePath, classContent.toString(), false);

            File targetFile = new File(targetFilePath);

            // Improve inline comment placement in the unit test code (if needed)
            processComments(targetFile);

            // Ensure OCD algorithm parameter constants are correctly used, fix when not.
            modifyJavaConstantsInFile(targetFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Modifies Java constants in a file by ensuring that constant variables are not used as string literals
     * in a specific pattern and appends '_NAME' to constants that don't end with '_NAME'.
     * This method searches for lines containing 'parameters.put(' followed by a
     * capitalized constant as a string literal, removes the quotes around the constant,
     * and ensures the constant ends with '_NAME'.
     *
     * @param file The File object to be modified.
     * @throws IOException If an I/O error occurs.
     */
    public static void modifyJavaConstantsInFile(File file) throws IOException {
        Path path = file.toPath();
        List<String> lines = Files.readAllLines(path);
        List<String> modifiedLines = new ArrayList<>();

        // First pass: Remove quotes around constants
        Pattern pattern = Pattern.compile("parameters\\.put\\(\"\\s*([A-Z_]+)\\s*\",");
        Matcher matcher;

        for (String line : lines) {
            matcher = pattern.matcher(line);
            if (matcher.find()) {
                String modifiedLine = matcher.replaceAll("parameters.put($1,");
                modifiedLines.add(modifiedLine);
                // Uncomment for debugging:
                // System.out.println("Original: " + line);
                // System.out.println("Modified: " + modifiedLine);
                // System.out.println("---------");
            } else {
                modifiedLines.add(line);
            }
        }

        // Second pass: Ensure all constants end with '_NAME'
        List<String> finalLines = new ArrayList<>();
        Pattern appendPattern = Pattern.compile("parameters\\.put\\(\\s*([A-Z_]+)(?<!_NAME)\\s*,");

        for (String line : modifiedLines) {
            matcher = appendPattern.matcher(line);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String constantName = matcher.group(1) + "_NAME";
                matcher.appendReplacement(sb, "parameters.put(" + constantName + ",");
                System.out.println("Incorrect constant " + matcher.group(1) +" identified. Replacing with " + constantName);
            }
            matcher.appendTail(sb);
            String finalLine = sb.toString();

            finalLines.add(finalLine);


        }

        Files.write(path, finalLines);
    }


    // Example usage
    public static void main(String[] args) {
        String ocdaName = "SskAlgorithm";

        //  Extract unit test list from a unitTestFilePath and merge into test class at targetClassFilePath

        String unitTestFilePath = FileHelpers.getAutoGeneratedUnprocessedUnitTestPath(ocdaName + OCDATestAutomationConstants.GPT_GENERATED_TEST_FILE_NAME_SUFFIX);

        String targetClassFilePath = FileHelpers.getAutoGeneratedUnparsedTestPath("Generated" + ocdaName + "Test.java");
        extractAndMergeUnitTests(targetClassFilePath, unitTestFilePath);
    }

}