# Guide for Using Custom GPT in Test Generation

This document serves as a comprehensive guide for utilizing custom GPT models for the automatic generation of tests within the framework. If any questions arise, please reach out via the Rocket.Chat channel of DBIS.

Custom GPT used for WebOCD's OCDA test generation can be accessed using the following link: https://chat.openai.com/g/g-JchAUbvgn-ocd-test-generator

## Graph Type Related Tests

**Step 1: Initialize Test Class**
- **Command**: `gradle runInitializeGraphTypeTestRelatedFiles -PocdaName=<AlgorithmName> --no-build-cache`
- **Description**: This task generates a test class for the specified algorithm and integrates interface implementations based on the compatible graph types. The new test class will be located at `rest_ocd_services/src/test/java/i5/las2peer/services/ocd/algorithms`. If the class already exists, it will be overwritten only if it is empty. GPT-generated tests that pass validation are added to this class. Additionally, copies of this class are made in `gpt/classfiles` to be used during the GPT-based test generation process and to serve as a reference for evaluating the generated code.

**Step 2: Construct GPT Prompt**
- **Command**: `gradle runGenerateAndWriteGraphTypeRelatedPromptByName -PocdaName=<AlgorithmName> --no-build-cache`
- **Description**: This command creates a prompt file in the `gpt/prompts` directory, which can be directly used as input for GPT. For optimal results, use a custom GPT model tailored for graph analysis frameworks. A supplementary file is also created in `gpt/unit_tests_unprocessed` for storing the unprocessed GPT response (GPT response should be copied here directly).

**Step 3: Evaluate GPT Response and Merge or Construct Issue Fixing Prompt**
- **Command**: `gradle runProcessGraphTypeRelatedGPTTestCode -PocdaName=<AlgorithmName> --no-build-cache`
- **Description**: This step involves extracting tests from the GPT response, evaluating them, and then either merging them into the main test class or creating a prompt to fix identified issues. If issues are found, a new prompt is generated in `gpt/prompts` for corrections. Repeat this step as needed until the response meets the required standards.

## OCDA Accuracy Tests

**Step 1: Generate OCDA Parameter Prompt**
- **Command**: `gradle runGenerateAndWriteOCDAParameterGenerationPromptByName -PocdaName=<AlgorithmName> --no-build-cache`
- **Description**: Generates a prompt for diverse OCDA parameter generation, located in `gpt/prompts`, and creates an empty file named `<AlgorithmName>_gpt_generated_ocda_parameters.txt` in `gpt/unit_tests_unprocessed` for the GPT's response.

**Step 2: Create OCDA Accuracy Test**
- **Command**: `gradle runGenerateAndWriteOCDAAccuracyTest -PocdaName=<AlgorithmName> --no-build-cache`
- **Description**: Utilizes the GPT response with parameters to create the OCDA accuracy test.

## Specific OCDA Method Tests

**Step 1: Initialize Test Files**
- **Command**: `gradle runInitializeOCDAMethodTestFiles -PocdaName=<AlgorithmName> -PmethodNames=method1,method2 --no-build-cache`
- **Description**: Initializes required test files by specifying the algorithm name and method. This creates a test class in `gpt/classfiles` which will be used as input for GPT. The class is overwritten only if it is empty.

**Step 2: Construct GPT Prompt for OCDA Method Tests**
- **Command**: `gradle runGenerateAndWriteOCDAMethodTestPrompt -PocdaName=<AlgorithmName> -PmethodNames=getMaxDifference --no-build-cache`
- **Description**: Generates a prompt file in the `gpt/prompts` directory for custom GPT and A supplementary file is also created in `gpt/unit_tests_unprocessed` for storing the unprocessed GPT response (GPT response should be copied here directly).

**Step 3: Evaluate GPT Response for OCDA Method Tests**
- **Command**: `gradle runProcessOCDAMethodRelatedGPTTestCode -PocdaName=<AlgorithmName> -PmethodNames=getMaxDifference --no-build-cache`
- **Description**: Analyzes the GPT-generated tests based on the specified algorithm and method, determining whether to merge them into the main test class or to create a new prompt for issue resolution.

**Note**: After each code evaluation step, a detailed report will be generated in `gpt/reports`, outlining the findings based on established quality metrics. 

---
# Guide for Using Assistant GPT in Test Generation
In case of assistant GPT usage, users need to execute a python script while WebOCD is running and wait for the automatic integration of validated test results into the main test class. Before the scripts are executed, API key from Open AI should be added to `ocda_test_generator_python_intermediary/.env` as follows: OPEN_AI_API_KEY=your_key 

**Graph Type Related Tests**:
- **Command**: `python ocda_test_generator.py generate_graph_type_related_tests <AlgorithmName>`

**OCDA Accuracy Tests**:
- **Command**: `python ocda_test_generator.py generate_diverse_ocda_parameters <AlgorithmName>`

**Specific OCDA Method Tests**:
- **Command**:  `python ocda_test_generator.py generate_specific_ocda_method_tests <AlgorithmName> --methodNames "method1,method2"`


The process with Assistant GPT does not require user interaction beyond the initial execution of the script. A markdown log file detailing the interaction will be generated in `gpt/logs` for user review.
