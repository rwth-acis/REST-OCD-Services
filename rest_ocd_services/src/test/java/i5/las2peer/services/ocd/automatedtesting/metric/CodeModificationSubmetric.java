package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.PromptGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeModificationSubmetric {

    /**
     * Ratio of lines within test methods that were not supposed to be modified and were not modified
     */
    public static double correctlyUnmodifiedMethodLineRatio = 1;

    /**
     * Ratio autogenerated/complete unit test comments that are correctly annotated as autogenerated
     */
    public static double autogeneratedTestAnnotationRatio = 1;

    /**
     * List that holds instructions that will be used to generate the prompt for improving ChatGPT code
     */
    public static ArrayList<String> promptImprovementRemarks = new ArrayList<>();




    public static void main(String[] args) {
        File gptInput = new File(OCDAParser.getOCDATestPath("SskAlgorithmTest1.java"));
        //File gptOutput = new File(OCDAParser.getOCDATestPath("SskAlgorithmTest.java"));
        File gptOutput = new File("someFile.java");

        evaluateLineModificationInTests(gptOutput,gptInput);
        evaluateMethodCommentModification(gptOutput,gptInput);

        System.out.println("\n\nPrompt improvement remarks:");
        for (String remark : promptImprovementRemarks){
            System.out.println("\t"+remark);
        }

        System.out.println("\n\n" + "correctlyUnmodifiedMethodLineRatio = "+correctlyUnmodifiedMethodLineRatio + " | autogeneratedTestAnnotationRatio = "+autogeneratedTestAnnotationRatio);


    }


    /**
     * Compares Javadoc comments in unit tests between two Java files to check if comments marked as "Don't modify comment"
     * in the input file were modified in the output file.
     *
     * @param gptOutput The output Java test class file to be compared.
     * @param gptInput  The input Java test class file used as a reference.
     * @return          The number of comments that were marked as "Don't modify comment" in the input file
     *                  but were found to be modified in the output file.
     */
    public static int evaluateMethodCommentModification(File gptOutput, File gptInput) {

        // Number of unit test comments annotated as auto-generated
        int annotatedCommentCount = 0;
        // Number of auto-generated/completed unit tests where comment indicating autogeneration was removed/altered
        int incorrectlyModifiedCommentsCount = 0;


        /* Method names for input and output test class files */
        List<String> inputTestClassFileMethods = OCDAParser.extractMethods(gptInput);
        List<String> outputTestClassFileMethods = OCDAParser.extractMethods(gptOutput);

        // Extract method names and their Javadoc comments from both files
        Map<String, String> inputComments = new HashMap<>();
        Map<String, String> outputComments = new HashMap<>();
        for (String methodName : inputTestClassFileMethods) {

            // Map each method to its corresponding comment in the input file
            inputComments.put(methodName, normalizeComment(OCDAParser.getMethodComment(gptInput,methodName)));

            // If the method from the input file is contained in the output file, then map method to its comment,
            // otherwise map method to an empty comment
            if (outputTestClassFileMethods.contains(methodName)){
                outputComments.put(methodName, normalizeComment(OCDAParser.getMethodComment(gptOutput,methodName)));
            } else {
                outputComments.put(methodName,"");
            }

        }

        // Iterate through each method in the input file
        for (Map.Entry<String, String> entry : inputComments.entrySet()) {
            String methodName = entry.getKey();
            String inputComment = entry.getValue();

            // Check if the method comment contains comment indication that the unit test is auto generated/completed
            if (inputComment.contains(PromptGenerator.AUTO_GENERATED_COMMENT_STRING)) {

                // If unit test has a comment marking it for auto-generation/completion increment corresponding counter
                annotatedCommentCount += 1;

                // Comment of the unit test from input file in the output file (if present)
                String outputComment = outputComments.getOrDefault(methodName, "");

                // Check if the part of the comment referring to autogeneration was modified in the output file
                if (!outputComment.contains(PromptGenerator.AUTO_GENERATED_COMMENT_STRING)) {

                    // When a comment marking autocompleted tests is removed/altered increase corresponding counter
                    incorrectlyModifiedCommentsCount++;

                    // Add prompt instruction for code improvement that will be used for future prompts
                    String promptInstruction = "Javadoc comment of unit test '" + methodName + "' must include text '"
                            + PromptGenerator.AUTO_GENERATED_COMMENT_STRING + "'. However, this text was removed. " +
                            "Add it back.";
                    promptImprovementRemarks.add(promptInstruction);
                }
            }
        }

        // Calculate the ratio of autogenerated tests which are correctly annotated as such.
        if (annotatedCommentCount > 0) {
            autogeneratedTestAnnotationRatio = 1 - ((double) incorrectlyModifiedCommentsCount) / annotatedCommentCount;
        } else {
            autogeneratedTestAnnotationRatio = 1;
        }

        return incorrectlyModifiedCommentsCount;
    }

    /**
     * Normalizes a comment string by removing Java comment symbols and reducing all whitespace to a single space.
     * This method strips away comment delimiters and asterisks commonly used in Java doc comments and block comments.
     * It replaces any sequence of whitespace characters (including new lines) with a single space and trims leading
     * and trailing spaces from the comment. This normalization is useful for text comparison purposes where formatting
     * differences such as whitespace and comment syntax are irrelevant.
     *
     * @param comment The comment string to be normalized.
     * @return        A normalized version of the comment string, free from comment-specific syntax and with uniform spacing.
     */
    private static String normalizeComment(String comment) {
        return comment.replaceAll("/\\*\\*|\\*/|\\*", "") // Remove comment symbols
                .replaceAll("\\s+", " ")            // Replace all whitespace (including newlines) with a single space
                .trim();                            // Trim leading and trailing spaces
    }


    /**
     * Evaluates the ratio of lines that were incorrectly modified in the output file compared to the input file.
     * This method scans through methods in both input and output Java class files, focusing on lines annotated
     * with a specific comment (e.g., "Don't modify"). It calculates the ratio of lines that were not supposed
     * to be modified but were changed in the output file. The ratio is calculated as the number of incorrectly
     * modified lines divided by the total number of lines that were not supposed to be modified. A ratio of 1
     * indicates that no line was modified incorrectly.
     *
     * @param gptOutput The output Java file to be evaluated.
     * @param gptInput  The input Java file used as the reference.
     * @return          The ratio of correctly unmodified lines. A value of 1 indicates perfect adherence
     *                  to non-modification.
     */
    public static double evaluateLineModificationInTests(File gptOutput, File gptInput){
        // Method names for input and output test class files
        List<String> inputTestClassFileMethods = OCDAParser.extractMethods(gptInput);
        List<String> outputTestClassFileMethods = OCDAParser.extractMethods(gptOutput);

        // Total lines that was not supposed to be modified in the output file compared to the input file
        int totalLinesNotToModify = 0;
        // Total lines that were modified in the output file even though they were not supposed to be modified
        int incorrectlyModifiedLines = 0;

        // Check if method lines from input file that were not supposed to be modified were modified in the output file
        // when this is the case, add a corresponding instruction to the list of instructions for prompt improvement
        for (String methodName : inputTestClassFileMethods){

            if (outputTestClassFileMethods.contains(methodName)){

                // Get lines from a specified method in both input and output test class files annotated with a comment
                // indicating the line should not be modified
                List<String> inputClassMethodAnnotatedLines = OCDAParser.listAnnotatedLinesInMethod(gptInput,methodName,
                        PromptGenerator.DONT_MODIFY_COMMENT_STRING,false);
                List<String> outputClassMethodAnnotatedLines = OCDAParser.listAnnotatedLinesInMethod(gptOutput, methodName,
                        PromptGenerator.DONT_MODIFY_COMMENT_STRING,false);

                // Lines from input file with comment "Don't modify" increase line number that shouldn't be modified
                totalLinesNotToModify += inputClassMethodAnnotatedLines.size();

                // Normalize lines in input and output since differing number of spaces should be ignored
                List<String> normalizedInputFileMethodLines = inputClassMethodAnnotatedLines.stream()
                        .map(line -> line.replaceAll("\\s+", " ").trim())
                        .collect(Collectors.toList());

                List<String> normalizedOutputFileMethodLines = outputClassMethodAnnotatedLines.stream()
                        .map(line -> line.replaceAll("\\s+", " ").trim())
                        .collect(Collectors.toList());

                // Each line in the specified method of input test class file that was annotated with "don't modify"
                // should be present in the output test class file */
                for (String inputFileMethodLine : normalizedInputFileMethodLines) {
                    // If the line that was not supposed to be modified was modified, add remark for prompt improvement
                    if (!normalizedOutputFileMethodLines.contains(inputFileMethodLine)){

                        // When an incorrectly modified line is detected, increase the corresponding counter
                        incorrectlyModifiedLines += 1;

                        // Add prompt improvement instruction to be used in subsequent prompts to ChatGPT
                        String promptImprovementInstruction = "Code line '" + inputFileMethodLine + "' in method '"
                                + methodName + "' was modified, but it must be unmodified since it is annotated with '" + PromptGenerator.DONT_MODIFY_COMMENT_STRING +"' comment.";
                        promptImprovementRemarks.add(promptImprovementInstruction);
                    }
                }
            }

        }

        // Calculate the ratio of unmodified lines among all lines that were not supposed to be modified

        if (totalLinesNotToModify > 0) {
            correctlyUnmodifiedMethodLineRatio = 1 - ((double) incorrectlyModifiedLines) / totalLinesNotToModify;
        } else {
            correctlyUnmodifiedMethodLineRatio = 1;
        }

        return correctlyUnmodifiedMethodLineRatio;

    }

    /**
     * Resets variables of code modification submetric to be reused
     */
    public static void resetSubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        autogeneratedTestAnnotationRatio = 0;
        correctlyUnmodifiedMethodLineRatio = 0;

    }


}
