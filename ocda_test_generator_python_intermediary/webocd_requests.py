import requests

# Define the URL with the desired GraphCreationType value
# url = 'http://localhost:8080/ocd/benchmarks/LFR/parameters/default'
# url1 = "https://webocd.dbis.rwth-aachen.de/OCDWebClient/graphs.html"
# url2 = "https://webocd.dbis.rwth-aachen.de/OCDWebClient/benchmarks.html"
# url3 = "http://137.226.232.68/OCDWebClient/login.html"

baseUrl = "localhost:8080"  #  "webocd.s-heppner.com"


url2 = f'https://{baseUrl}/ocd/prompts?ocdaName=SskAlgorithm'  # partial tests, algorithm params and setParam method
url3 = f'https://{baseUrl}/ocd/imports?ocdaName=SskAlgorithm' # import statements in test class of OCDA
url4 = f'https://{baseUrl}/ocd/ocdaCode?ocdaName=SskAlgorithm'
url5 = 'http://164.92.238.0:8080/ocd/prompts/SskAlgorithm'

url6 = "https://en.wikipedia.org/wiki/Main_Page"

# Define the basic authentication credentials
username = 'alice'
password = 'pwalice'


if __name__ == "__main__":

    ocdaName = "SskAlgorithm"
    file_path = "files/gpt_responses/unprocessed_graph_type_tests/" + ocdaName + "_graph_type_tests.txt";

    # Read the content from the file
    with open(file_path, "rb") as file:
        file_content = file.read()

    url_process_input = f'http://localhost:8080/ocd/process-input?ocdaName={ocdaName}'


    # Send the POST request with basic authentication and include the file content as the request body
    response = requests.post(url_process_input, data=file_content, headers={'Content-Type': 'text/plain'})

    # Check the response status code
    if response.status_code == 200:
        print("Success", response.headers['Content-Type'])
        # You can access the response content as XML here if needed
        print("------- response content ----------")
        response_content = response.text
        print(response_content)
    elif response.status_code == 401:
        print("Unauthorized")
    else:
        print(f"Request failed with status code: {response.status_code}")
        #print(response.text)
