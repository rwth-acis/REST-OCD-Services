import requests
import json

baseUrl = "localhost:8080"

def check_response(response):
    """
    Check the HTTP response status code and handle accordingly.

    This function checks the status code of an HTTP response. If the status code is 200 (OK),
    it prints a success message along with the Content-Type of the response. It then returns
    the text content of the response. If the status code is not 200, it prints a failure message
    with the status code.

    Args:
        response: The response object to check, typically received from a request made using requests library.

    Returns:
        The text content of the response if the response's status code is 200. Otherwise, returns None.
    """
    # Check the response status code
    if response.status_code == 200:
        print("Success", response.headers['Content-Type'])
        # You can access the response content as XML here if needed
        return response.text
        #print(response_text)

    else:
        print(f"Request failed with status code: {response.status_code}")
        # print(response.text)

def get_graph_type_based_unit_test_completion_prompt(ocdaName):
    """
      Retrieves a prompt from a WebOCD Service instance that holds partially completed overlapping community
      detection algorithm (OCDA) tests, OCDA parameters, and the setParam method implementation related
      to graph types.

      Args:
          ocdaName (str): The name of the OCDA.

      Returns:
          str: The prompt containing partially completed OCDA tests, OCDA parameters, and the setParam method
          implementation, or None if the request fails.
      """
    graph_type_related_test_completion_prompt = f'http://{baseUrl}/ocd/prompts/graphtype?ocdaName={ocdaName}'  # partial tests, algorithm params and setParam method
    response = requests.get(graph_type_related_test_completion_prompt)

    return check_response(response)


def get_specific_ocda_method_tests_completion_prompt(ocdaName, methodNames):
    """
    Retrieves a prompt from WebOCD to generate unit tests for specified OCDA methods.

    Args:
        ocdaName (str): The name of the OCDA.
        methodNames (str): Names of the methods.

    Returns:
        str: The prompt for generating unit tests.
    """
    ocda_parameter_generation_prompt = f'http://{baseUrl}/ocd/prompts/ocdamethod?ocdaName={ocdaName}'
    response = requests.post(ocda_parameter_generation_prompt, data=methodNames,  headers={'Content-Type': 'text/plain'})

    return check_response(response)

def get_ocda_parameter_generation_prompt(ocdaName):
    """
      Retrieves a prompt from a WebOCD Service instance that holds OCDA parameters, the setParam method
      implementation, and a JSON with parameter placeholders.

      Args:
          ocdaName (str): The name of the OCDA.

      Returns:
          str: The prompt containing OCDA parameters, the setParam method implementation, and a JSON with parameter
          placeholders, or None if the request fails.
      """
    ocda_parameter_generation_prompt = f'http://{baseUrl}/ocd/prompts/ocdaparameters?ocdaName={ocdaName}'  #algorithm params and setParam method and JSON with parameter placeholders
    response = requests.get(ocda_parameter_generation_prompt)

    return check_response(response)




if __name__ == "__main__":
    ocdaName = "SskAlgorithms"
    response_content = get_graph_type_based_unit_test_completion_prompt(ocdaName)
    print(response_content)



