package i5.las2peer.services.ocd.ocdatestautomation;

import i5.las2peer.services.ocd.automatedtesting.PromptGenerator;
import i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers;
import i5.las2peer.services.ocd.automatedtesting.helpers.PathResolver;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants.*;
import static i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter.generateAndWriteFile;
import static i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser.*;
import static i5.las2peer.services.ocd.automatedtesting.PromptGenerator.*;

public class OCDATestClassGenerator {


    /**
     * Generates and writes a test class file for a specified OCD algorithm. This method dynamically constructs a test
     * class based on the characteristics and compatible graph types of the given OCD algorithm. The generated test
     * class implements appropriate test interfaces depending on the graph types compatible with the OCD algorithm.
     * For instance, OCD algorithms compatible with directed graphs will implement the DirectedGraphTestReq interface.
     * The created unit tests within the test class are partially complete and are designed to be finalized using a GPT.
     *
     * @param ocdaCode The file containing the OCD algorithm code. This file is parsed to determine the algorithm's
     *                  characteristics and compatible graph types.
     * @param isMainTestClass  Boolean determining if the generated class is used as a main test class which will
     *                         be filled with unit tests, or if it is not a main test class and instead the unit
     *                         tests from this class will be merged into the main class (after using GPT to
     *                         generate unit tests).
     *
     *
     * @implNote The method performs several key functions:
     * 1. Extracts the OCD algorithm name from the class file.
     * 2. Identifies compatible graph types for the algorithm.
     * 3. Determines the interfaces to be implemented by the test class based on the algorithm's compatibilities.
     * 4. Constructs the test class by appending necessary import statements, interface implementations, and partially completed unit tests.
     * 5. Writes the constructed test class to a Java file.
     *
     * The generated test class includes standard setup for unit tests, such as a setup method annotated with @BeforeEach.
     * The generated code section is marked with "ChatGPT Code" to indicate where the GPT should complete the unit tests.
     */
    public static void generateAndWriteOCDTestClass(File ocdaCode, boolean isMainTestClass){

        // Extract OCD algorithm name from the class file
        String ocdaName = OCDAParser.getClassName(ocdaCode);

        // Name of the generated file can be annotated with string Generated, for readability. This can e.g. be used
        // for temporarily generated test classes the contents of which should be merged into the
        // main test class
        String ocdaTestClassName = ocdaName;
        if (!isMainTestClass) {
            ocdaTestClassName = "Generated" + ocdaName;
        }


        // Extract compatible graph types for the given algorithm
        List<String> compatibilities = OCDAParser.extractCompatibilities(ocdaCode);

        // String that will hold interface names that an OCDA test class should implement
        String baseInterfaceNamesToImplement = "";

        // All OCDA test classes implement test interface for algorithm parameters and undirected graph type
        baseInterfaceNamesToImplement += OCDAParameterTestReq.class.getSimpleName()
                + ", " + UndirectedGraphTestReq.class.getSimpleName();


        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();


        // Iterate through the list of compatible graph type strings and add corresponding interfaces
        // to the interfaces that the test class implements
        for (String graphType : compatibilities) {
            if (graphType.equals("GraphType.DIRECTED")){
                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + DirectedGraphTestReq.class.getSimpleName();
            }
            if (graphType.equals("GraphType.WEIGHTED")){
                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + WeightedGraphTestReq.class.getSimpleName();

            }
            if (graphType.equals("GraphType.ZERO_WEIGHTS")){
                // test interface for zero weight graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + ZeroWeightsGraphTestReq.class.getSimpleName();

            }
            if (graphType.equals("GraphType.NEGATIVE_WEIGHTS")){
                // test interface for negative weights graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + NegativeWeightsGraphTestReq.class.getSimpleName();

            }
            if (graphType.equals("GraphType.SELF_LOOPS")){
                // test interface for self loops graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + SelfLoopsGraphTestReq.class.getSimpleName();

            }

        }

        // Merge import statements from the OCD algorithm class and its test class, since the algorithm class
        // imports might be needed for the test class if the user wants to test OCD algorithm methods.
        List<String> mergedImports = mergeAndSortLists(extractSortedImports(ocdaCode),
                generateTestClassImports(ocdaCode));



        /* Generate partially completed test class */

        // package depends on where the auto-generated class should be placed.
        String testClassPackage = TEST_CLASS_PACKAGE_STRING;
        if (!isMainTestClass) {
            testClassPackage = AUTO_GENERATED_TEST_CLASS_PACKAGE;
        }

        // Append package to the test class
        stringBuilder.append("package " + testClassPackage + ";").append("\n\n");

        // Append import statements to the test class
        mergedImports.forEach(importStatement -> stringBuilder.append(importStatement).append("\n"));

        // Add interface implementation and partially completed unit tests to the test class,
        // based on compatible graph types.
        stringBuilder.append("\n\n" +
                "public class " +  ocdaTestClassName +"Test implements "
                + baseInterfaceNamesToImplement + " {\n" +
                "\n" +
                "\t" + ocdaName + " algo; // Variable to access " + ocdaName +"\n" +
                "\t" + CustomGraph.class.getSimpleName() + " defaultCustomGraphToUse; // Variable to access CustomGraph used in tests (unless test defines a different graph) \n" +
                "\n" +
                "\t@BeforeEach\n" +
                "\tpublic void setup() {\n" +
                "\t\talgo = new "+ ocdaName +"();\n" +
                "\t\tdefaultCustomGraphToUse = new " + CustomGraph.class.getSimpleName() + "();\n" +
                "\t}\n" +
                "\n" +
                "\t@Override\n" +
                "\tpublic OcdAlgorithm getAlgorithm() {\n" +
                "\t\treturn algo;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                generatePartiallyCompletedGraphTypeTests(ocdaCode)+ "\n" +
                "}\n"
        );


        // Write test class to a file
        generateAndWriteFile(PathResolver.resolvePath("gpt/classfiles/"+ocdaTestClassName+"Test.java"), stringBuilder.toString(),false);


    }


    /**
     * Generates and writes a test class for specified methods of an OCD algorithm. This method can create both
     * a test class that should be completed (e.g., by GPT) and a reference class. The reference class is used to
     * compare whether the auto-completion altered unintended parts of the code.
     *
     * @param ocdaCode The file containing the OCD algorithm code.
     * @param methodsToTest A list of method names from the OCD algorithm that should be tested.
     * @param isReferenceClass A boolean flag to indicate if the generated class is a reference test class.
     *                         If true, generates a reference class; otherwise, generates a class for completion.
     */
    public static void generateAndWriteOCDAMethodsTestClass(File ocdaCode, List<String> methodsToTest, boolean isReferenceClass){

        // Extract OCD algorithm name from the class file
        String ocdaName = OCDAParser.getClassName(ocdaCode);

        // Differentiate between a test class that should be completed (by GPT) and a reference test class which is
        // used to compare if the changes made by GPT make sense (e.g. was something deleted that wasn't supposed to?)
        String ocdaTestClassName = ocdaName;;
        if (!isReferenceClass) {
            ocdaTestClassName = "Generated" + ocdaName;
        }


        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();


        // Merge import statements from the OCD algorithm class and its test class, since the algorithm class
        // imports might be needed for the test class if the user wants to test OCD algorithm methods.
        List<String> mergedImports = mergeAndSortLists(extractSortedImports(ocdaCode),
                generateTestClassImports(ocdaCode));


        /* Generate partially completed test class */

        // package depends on where the auto-generated class should be placed.
        String testClassPackage = TEST_CLASS_PACKAGE_STRING;
        if (!isReferenceClass) {
            testClassPackage = AUTO_GENERATED_TEST_CLASS_PACKAGE;
        }

        // Append package to the test class
        stringBuilder.append("package " + testClassPackage + ";").append("\n\n");

        // Append import statements to the test class
        mergedImports.forEach(importStatement -> stringBuilder.append(importStatement).append("\n"));

        // Generate unit partially completed unit tests for each method that should be tested
        StringBuilder partiallyCompletedOCDAMethodTests = new StringBuilder();
        if (methodsToTest != null) {
            methodsToTest.forEach(methodToTest -> {

                // If the specified method exists in the specified OCD class, then create a partially completed unit test
                Boolean ocdaMethodExists = !OCDAParser.getMethodSignature(ocdaCode, methodToTest).equals("");
                if (ocdaMethodExists) {
                    partiallyCompletedOCDAMethodTests.append(PromptGenerator.generateOCDAMethodUnitTestString(methodToTest)).append("\n\n");
                }

            });
        }


        // Add partially completed unit tests to the test class
        stringBuilder.append("\n\n" +
                "public class " +  ocdaTestClassName +"Test {\n" +
                "\n" +
                "\t" + ocdaName + " algo; // Variable to access " + ocdaName +"\n" +
                "\t" + CustomGraph.class.getSimpleName() + " defaultCustomGraphToUse; // Variable to access CustomGraph used in tests (unless test defines a different graph) \n" +
                "\n" +
                "\t@BeforeEach\n" +
                "\tpublic void setup() {\n" +
                "\t\talgo = new "+ ocdaName +"();\n" +
                "\t\tdefaultCustomGraphToUse = new " + CustomGraph.class.getSimpleName() + "();\n" +
                "\t}\n" +
                "\n" +
                partiallyCompletedOCDAMethodTests.toString()+ "\n" +
                "}\n"
        );

        // Write test class to a file
        generateAndWriteFile(PathResolver.resolvePath("gpt/classfiles/"+ocdaTestClassName+"Test.java"), stringBuilder.toString(),false);


    }



    public void initializeGraphTypeTestRelatedFiles(String ocdaName){
        String contentRoot = System.getProperty("user.dir");
        String relativePath = "/src/main/java/i5/las2peer/services/ocd/algorithms/";
        String ocdaCodePath =  contentRoot + File.separator + relativePath + ocdaName + ".java";
        File ocdaCode = new File(ocdaCodePath);
        // generate a test class with partially completed unit tests to which the GPT-generated tests  should be compared to
        generateAndWriteOCDTestClass(ocdaCode, true);
        // Generate OCD algorithm test class with partially completed unit tests that will act as a reference for comparison
        generateAndWriteOCDTestClass(ocdaCode, false);
    }

    public void initializeOCDAMethodTestFiles(String ocdaName, List<String> methodNames){
        String contentRoot = System.getProperty("user.dir");
        String relativePath = "/src/main/java/i5/las2peer/services/ocd/algorithms/";
        String ocdaCodePath =  contentRoot + File.separator + relativePath + ocdaName + ".java";
        File ocdaCode = new File(ocdaCodePath);

        /* Generate and write test classes that hold partially completed unit tests related to specific OCDA methods */
        //methodNames = Arrays.asList("Item1", "calculateTransitiveLinkWeight", "calculateMemberships");
        // generate a test class with partially completed unit tests to which the GPT-generated tests  should be compared to
        generateAndWriteOCDAMethodsTestClass(ocdaCode, methodNames,true);
        // Generate OCD algorithm test class with partially completed unit tests  that will act as a reference for comparison
        generateAndWriteOCDAMethodsTestClass(ocdaCode, methodNames, false);
    }




    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "initializeGraphTypeTestRelatedFiles":
                    if (args.length > 1) {
                        String ocdaName = args[1];
                        new OCDATestClassGenerator().initializeGraphTypeTestRelatedFiles(ocdaName);
                    } else {
                        System.out.println("OCDA name not provided for Graph Type Tests.");
                    }
                    break;
                case "initializeOCDAMethodTestFiles":
                    if (args.length > 2) {
                        String ocdaName = args[1];
                        List<String> methodNames = Arrays.asList(args).subList(2, args.length);
                        new OCDATestClassGenerator().initializeOCDAMethodTestFiles(ocdaName, methodNames);
                    } else {
                        System.out.println("OCDA name or method names not provided for OCDA Method Tests.");
                    }
                    break;
                default:
                    System.out.println("Invalid arguments.");
                    break;
            }
        } else {
            System.out.println("No arguments provided.");
        }
    }




//    public static void main(String[] args) {
//        File ocdaCode = new File(FileHelpers.getOCDAPath("SskAlgorithm.java"));
//
//        /* Generate and write test classes that hold partially completed unit tests related to graph types */
//
//        // generate a test class with partially completed unit tests to which the GPT-generated tests  should be compared to
////        generateAndWriteOCDTestClass(ocdaCode, true);
////        // Generate OCD algorithm test class with partially completed unit tests that will act as a reference for comparison
////        generateAndWriteOCDTestClass(ocdaCode, false);
//
////        /* Generate and write test classes that hold partially completed unit tests related to specific OCDA methods */
////        List<String> stringList = Arrays.asList("Item1", "calculateTransitiveLinkWeight", "calculateMemberships");
////        // generate a test class with partially completed unit tests to which the GPT-generated tests  should be compared to
////        generateAndWriteOCDAMethodsTestClass(ocdaCode, stringList,true);
////        // Generate OCD algorithm test class with partially completed unit tests  that will act as a reference for comparison
////        generateAndWriteOCDAMethodsTestClass(ocdaCode, stringList, false);
//    }
}
