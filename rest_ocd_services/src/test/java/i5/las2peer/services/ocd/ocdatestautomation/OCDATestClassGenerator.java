package i5.las2peer.services.ocd.ocdatestautomation;

import i5.las2peer.services.ocd.automatedtesting.GPTCodeProcessor;
import i5.las2peer.services.ocd.automatedtesting.PromptGenerator;
import i5.las2peer.services.ocd.automatedtesting.TestClassMerger;
import i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers;
import i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter;
import i5.las2peer.services.ocd.automatedtesting.helpers.PathResolver;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.*;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
     * @param isReferenceTestClass  Boolean determining if the generated class is used as a main test class which will
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
    public static void generateAndWriteOCDTestClass(File ocdaCode, boolean isReferenceTestClass){

        // Extract OCD algorithm name from the class file
        String ocdaName = OCDAParser.getClassName(ocdaCode);

        // Name of the generated file can be annotated with string Generated, for readability. This can e.g. be used
        // for temporarily generated test classes the contents of which should be merged into the
        // main test class
        String ocdaTestClassName = ocdaName;
        if (!isReferenceTestClass) {
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
        if (!isReferenceTestClass) {
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


        // Write test class to a file for GPT processing
        generateAndWriteFile(PathResolver.resolvePath("gpt/classfiles/"+ocdaTestClassName+"Test.java"), stringBuilder.toString(),false);

        // Create a test class for OCDA if it doesn't exist and populate it with base test interfaces and skeleton
        // code that should be enriched by GPT
        initializeTestClass(ocdaName,stringBuilder.toString());

    }


    /**
     * Initialize a test class for specified OCDA with a specified content
     * @param ocdaName      Name of the OCDA for which the test class should be created
     * @param content       Content of the test class
     */
    public static void initializeTestClass(String ocdaName, String content){
        String ocdaTestClassPath = FileHelpers.getOCDATestPath(ocdaName + "Test.java");
        File ocdaTestClass = new File(ocdaTestClassPath);
        // Check if the file exists
        if (!ocdaTestClass.exists() || ocdaTestClass.length() == 0) {
            try {
                // Try creating the file. If the directories leading up to the file do not exist,
                // they will need to be created first (this shouldn't generally happen since there
                // are other OCDA).
                File parentDir = ocdaTestClass.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs(); // Make the directory (and any parent directories as needed)
                }
                ocdaTestClass.createNewFile(); // Create the new file

                // Initialize test class with basic method and interface implementations
                generateAndWriteFile(ocdaTestClassPath, content,false);


                System.out.println("File created: " + ocdaTestClass.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("An error occurred while creating the file: " + e.getMessage());
            }
        } else {
            System.out.println("File already exists: " + ocdaTestClass.getAbsolutePath());
        }
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


    /**
     * Generates a JUnit test method string for assessing the accuracy of overlapping community detection
     * for a given algorithm. The test leverages OCDA parameter values generated by GPT and stored in a
     * JSON file. The test succeeds if any of the parameter sets lead to the correct detection of community
     * count within the specified graph. If all parameter sets fail to yield the correct community count,
     * the test fails, indicating that the OCDA class implementation or parameter values may need review.
     *
     * @param ocdaName The name of the Overlapping Community Detection Algorithm (OCDA) for which the test is generated.
     * @return A string representing the JUnit test method for the OCDA accuracy test.
     */
    public static String generateOCDAAccuracyTest(String ocdaName){

        return "/**\n" +
                " * This is an auto-generated OCD accuracy test, that checks if the OCD algorithm detects a reasonable number of communities\n" +
                " * on a graph with a known community structure. If this test fails, the issue is likely either with the used parameter values\n" +
                " * or the algorithm implementation. In this case, these areas should be reviewed.\n" +
                " */\n" +
                "    @Test\n" +
                "    public void ocdaAccuracyTest(){\n" +
                "        // Number of different parameter values for which the placeholders should be created\n" +
                "        int parameterCount = Integer.parseInt(" + "\"" + OCDA_PARAMETER_GENERATION_COUNT_FOR_OCD_ACCURACY_TESTS + "\"" + ");\n" +
                "\n" +
                "        // Get graph on which the OCDA should be executed\n" +
                "        " + CustomGraph.class.getSimpleName() + " graph = " + OcdTestGraphFactory.class.getSimpleName() + ".getUndirectedKarateGraph();\n" +
                "\n" +
                "        // Read auto-generated OCDA parameter values\n" +
                "        Map<String, List<String>> dataForAlgorithm = " + OCDWriter.class.getSimpleName() + ".readAlgorithmDataFromFile(\"gpt/json/ocda_parameters.json\",\"" + ocdaName + "\");\n" +
                "\n" +
                "        if (dataForAlgorithm != null) {\n" +
                "            // Create a list to store community count when OCDA is executed with each auto-generated parameter set\n" +
                "            ArrayList<Integer> detectedCommunityCounts = new ArrayList<>();\n" +
                "\n" +
                "            // Add code to execute OCDA with each parameter set and store resulting community count\n" +
                "            for (int i = 0; i < parameterCount; i++) {\n" +
                "\n" +
                "                // Initialize map to hold OCDA parameters\n" +
                "                Map<String, String> parameters = new HashMap<>();\n" +
                "\n" +
                "                // Fill the parameter map with the auto-generated parameter values\n" +
                "                for (String parameterName : dataForAlgorithm.keySet()){\n" +
                "                    parameters.put(parameterName, String.valueOf(dataForAlgorithm.get(parameterName).get(i)));\n" +
                "                }\n" +
                "                getAlgorithm().setParameters(parameters);\n" +
                "\n" +
                "                try {\n" +
                "                    // Execute OCDA and store community count found with each used parameter set\n" +
                "                    detectedCommunityCounts.add(getAlgorithm().detectOverlappingCommunities(graph).getCommunities().size());\n" +
                "\n" +
                "                } catch (Exception e) {\n" +
                "                    e.printStackTrace();\n" +
                "                }\n" +
                "            }\n" +
                "            System.out.println(\"detected communities \" + detectedCommunityCounts); \n "+ //TODO:DELETE this line
                "            // Check if at least one parameter set leads to correct community count\n" +
                "            boolean hasValueInRange = detectedCommunityCounts.stream().anyMatch(value -> value >= 2 && value <= 5);\n" +
                "            assertTrue(hasValueInRange, \"No parameter set resulted in correct community count detection. Please review the test. Detected community counts were: \" + detectedCommunityCounts);\n" +
                "\n" +
                "        }\n" +
                "    }";



    }

    /**
     * Generates and writes an overlapping community detection (OCD) accuracy test for a specified OCD algorithm.
     * This generated test is then added to the main test class of the specified algorithm. The test checks the
     * accuracy of the algorithm in detecting communities within a given graph structure using algorithm parameters
     * stored in a JSON file (that were generated by GPT). The test is added to the existing test class file for
     * the specified OCD algorithm.
     *
     * @param ocdaName The name of the Overlapping Community Detection Algorithm (OCDA) for which the test is being generated.
     */
    public static void generateAndWriteOCDAAccuracyTest(String ocdaName) {

        // Extract valid OCDA params from GPT response and write them to a JSON file used for test creation
        System.out.println("Extracting Valid Auto-Generated OCDA Parameters from GPT Response and Writing them to JSON...");
        GPTCodeProcessor.extractOCDAParamsFromGPTResponseAndWriteToJSON(ocdaName);
        System.out.println("OCDA Parameters Successfully Extracted and Written. Moving On to Creating OCDA Accuracy Test...");

        // Generate ocd accuracy test for the specified OCD algorithm
        System.out.println("Generating OCDA Accuracy Test...");
        String unitTestString = generateOCDAAccuracyTest(ocdaName);

        // Identify main test class for the specified OCD algorithm to which the unit test should be added
        File ocdaTestClass = getOcdaFile(ocdaName + "Test.java", "/src/test/java/i5/las2peer/services/ocd/algorithms/");

        // Add OCD accuracy test to the main test class of the algorithm
        System.out.println("Generation Successful. Merging OCDA Accuracy Test into: " + ocdaTestClass.getAbsolutePath());
        TestClassMerger.mergeUnitTestIntoClass(ocdaTestClass, unitTestString);
        System.out.println("OCDA Accuracy Test Successfully Added.");
    }



    /**
     * Initializes and creates two identical files that hold partially completed unit tests related to graph types
     * for a specific Overlapping Community Detection Algorithm (OCDA). These files include placeholders for sections
     * to be auto-completed. One file is intended for auto-completion by GPT, while the other serves as a reference
     * for evaluation and comparison of the auto-completed test class.
     *
     * @param ocdaName The name of the OCDA for which the test files are being created.
     */
    public void initializeGraphTypeTestRelatedFiles(String ocdaName){

        // Get OCDA code
        File ocdaCode = getOcdaFile(ocdaName + ".java", "/src/main/java/i5/las2peer/services/ocd/algorithms/");

        // generate a test class with partially completed unit tests to which the GPT-generated tests  should be compared to
        generateAndWriteOCDTestClass(ocdaCode, true);

        // Generate OCD algorithm test class with partially completed unit tests that will act as a reference for comparison
        generateAndWriteOCDTestClass(ocdaCode, false);
    }


    /**
     * Initializes and creates two identical files containing partially completed unit tests related to specific
     * methods of an Overlapping Community Detection Algorithm (OCDA). These files include placeholders for sections
     * to be auto-completed by GPT. One file is used for GPT auto-completion, and the other serves as a reference
     * for the evaluation and comparison of the auto-completed test class.
     *
     * @param ocdaName    The name of the OCDA for which the test files are being created.
     * @param methodNames A list of OCDA method names for which the test files are generated.
     */
    public void initializeOCDAMethodTestFiles(String ocdaName, List<String> methodNames){

        // Get OCDA code
        File ocdaCode = getOcdaFile(ocdaName + ".java", "/src/main/java/i5/las2peer/services/ocd/algorithms/");

        /* Generate and write test classes that hold partially completed unit tests related to specific OCDA methods */
        //methodNames = Arrays.asList("Item1", "calculateTransitiveLinkWeight", "calculateMemberships");
        // generate a test class with partially completed unit tests to which the GPT-generated tests  should be compared to
        generateAndWriteOCDAMethodsTestClass(ocdaCode, methodNames,true);

        // Generate OCD algorithm test class with partially completed unit tests  that will act as a reference for comparison
        generateAndWriteOCDAMethodsTestClass(ocdaCode, methodNames, false);
    }


    /**
     * Retrieves the file object for an Overlapping Community Detection Algorithm (OCDA) based on the provided name
     * and relative path. This helper method constructs the full path to the OCDA file and returns a File object
     * representing it.
     *
     * @param ocdaName     The name of the OCDA file, including its extension (e.g., ".java").
     * @param relativePath The relative path within the project structure where the OCDA file is located.
     * @return A File object representing the OCDA file.
     */
    private static File getOcdaFile(String ocdaName, String relativePath) {
        String contentRoot = System.getProperty("user.dir");
        String ocdaCodePath = contentRoot + File.separator + relativePath + ocdaName;
        return new File(ocdaCodePath);
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
                case "generateAndWriteOCDAAccuracyTest":
                    if (args.length > 1) {
                        String ocdaName = args[1];
                        new OCDATestClassGenerator().generateAndWriteOCDAAccuracyTest(ocdaName);
                    } else {
                        System.out.println("OCDA name not provided for OCD Accuracy Test.");
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
