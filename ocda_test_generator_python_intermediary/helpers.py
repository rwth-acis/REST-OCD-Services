import re
import os
import json

# --------------------------------------------------------------
# File reading and writing methods
# --------------------------------------------------------------
def read_file_to_string(file_path):
    """
    Reads the content of a file and returns it as a string.

    Args:
        file_path (str): The path to the file to be read.

    Returns:
        str: The content of the file as a string, or None if the file is not found.
    """
    try:
        with open(file_path, 'r') as file:
            file_content = file.read()
        return file_content
    except FileNotFoundError:
        return None


# write content to a specified path either using mode that overwrites ('w') or appends ('a')
def write_to_file(full_path, content, mode='w'):
    """
    Writes content to a specified file path, either overwriting the existing content or appending to it.

    Args:
        full_path (str): The full path to the file to be written.
        content (str): The content to be written to the file.
        mode (str, optional): The mode to open the file in. 'w' for overwrite (default), 'a' for append.

    Returns:
        None
    """
    try:
        # Extract directory and filename from the full path
        directory = os.path.dirname(full_path)
        filename = os.path.basename(full_path)

        # Ensure the directory exists
        if not os.path.exists(directory):
            os.makedirs(directory)

        # Open the file with the specified mode ('w' for overwrite, 'a' for append)
        with open(full_path, mode) as file:
            # Write the content to the file
            file.write(content)
        print(f"Content written to {full_path} successfully.")
    except Exception as e:
        print(f"An error occurred: {str(e)}")


def extract_unit_tests(text):
    """
    Extracts Java unit tests from a given text and returns them as a list.

    Args:
        text (str): The text containing Java unit tests.

    Returns:
        list: A list of Java unit tests.
    """
    # Pattern to match a unit test with or without its Javadoc comment
    # The Javadoc comment part is made optional by using '(?:...)?'
    pattern = r'(?:/\*\*.*?\*/\s*)?@Test.*?public void.*?\{.*?\n\}'

    # Using re.DOTALL to make '.' match newline characters as well
    matches = re.findall(pattern, text, re.DOTALL)

    return matches

def modify_java_constants_in_file(file_path):
    """
     Modifies Java constants for setting OCD algorithm parameters in a
     file to ensure they are used correctly (i.e. constant variables are
     not used as string literals).

     Args:
         file_path (str): The path to the file containing Java constants.

     Returns:
         None
     """

    pattern = r'parameters\.put\("\s*([A-Z_]+)\s*",'

    modified_lines = []

    with open(file_path, 'r') as file:
        lines = file.readlines()

        for line in lines:
            # Replace pattern by removing quotes around the constant
            if 'parameters.put("' in line:
                modified_line = re.sub(pattern, r'parameters.put(\1,', line)
                modified_lines.append(modified_line)
                # print("Original:", line.strip())
                # print("Modified:", modified_line.strip())
                # print("---------")
            else:
                modified_lines.append(line)

    with open(file_path, 'w') as file:
        file.writelines(modified_lines)

def process_auto_generated_unit_tests(ocdaName, llm_output):
    """
    Processes auto-generated unit tests from GPT output and writes them to a file.

    Args:
        ocdaName (str): The name of the OCDA.
        llm_output (str): The output from GPT, potentially containing auto-generated unit tests.

    Returns:
        None
    """
    # extract unit tests from GPT output
    extracted_tests = extract_unit_tests(llm_output)

    # write all unit tests to a file
    tests_to_add = ""
    for extracted_test in extracted_tests:
        tests_to_add += extracted_test + "\n\n"

    graph_type_test_path = "files/gpt_responses/graph_type_tests/" + ocdaName + "_graph_type_tests.txt"

    # write auto-generated tests to a file
    write_to_file(full_path=graph_type_test_path,
                          mode='w',
                          content=tests_to_add)

    # ensure that OCDA parameter names, defined as constants are correctly used
    modify_java_constants_in_file(graph_type_test_path)


def create_json_from_file(file_path):
    """
    Creates a JSON string from the content of a file.

    Args:
        file_path (str): The path to the file.

    Returns:
        str: A JSON string representing the file content.
    """
    try:
        # Open and read the file
        with open(file_path, 'r') as file:
            file_content = file.read()

        # Create a dictionary with the file content as the value
        data = {
            "gptResponse": file_content
        }

        # Convert the dictionary to a JSON string
        json_string = json.dumps(data, indent=4)

        return json_string

    except FileNotFoundError:
        return f"File not found: {file_path}"
    except Exception as e:
        return f"An error occurred: {str(e)}"



if __name__ == "__main__":
    #Example usage
    llm_output = read_file_to_string("response.txt")  # Replace this with your actual LLM output
    #write_to_file("response_modified.txt", llm_output)
    extracted_tests = extract_unit_tests(llm_output)
    tests_to_add = ""
    for extracted_test in extracted_tests:
        tests_to_add += extracted_test + "\n\n"
    print(len(extracted_tests))


    #modify_java_constants_in_file("response_modified.txt")


