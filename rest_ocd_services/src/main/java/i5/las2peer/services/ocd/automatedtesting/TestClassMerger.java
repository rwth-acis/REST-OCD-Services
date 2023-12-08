package i5.las2peer.services.ocd.automatedtesting;


import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

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
     * This method modifies a Java file by moving the "// Don't modify" comments from being standalone lines
     * to inline comments at the end of the following line. This helps maintain the readability of the code
     * while preserving the intent of the comment.
     *
     * @param file The Java file to be modified.
     */
    public static void modifyComments(File file)  {
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

}