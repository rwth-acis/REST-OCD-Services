# OCD Service - WebOCD

This is a RESTful web service offering various overlapping community detection algorithms(OCDA) and centrality measures/simulations. The service is developed with the IntelliJ IDE, so we recommend you to use this tool to work on the project.<br/>
**WebOCD works best with its corresponding [web client](https://github.com/rwth-acis/OCD-Web-Client)**.

A running instance of WebOCD can be found at http://webocd.dbis.rwth-aachen.de/OCDWebClient/.

For any information on the service itself please refer to the [project wiki](https://github.com/rwth-acis/REST-OCD-Services/wiki) or the [website](https://rwth-acis.github.io/REST-OCD-Services/).

### Additional Notes
This service is based on the LAS2peer Template Project and its underlying LAS2peer framework. For any information on these please refer to https://github.com/rwth-acis/LAS2peer-Template-Project and https://github.com/rwth-acis/LAS2peer

# Building & Running  WebOCD Service

WebOCD Service requires a running arangodb instance, which has to be [installed separately](https://www.arangodb.com/docs/stable/installation.html). In order for WebOCD to access the database, the configuration files, located under [ocd/arangoDB](/ocd/arangoDB) should be adjusted accordingly.

The service can be built using the gradle build task. Afterwards, it can be run using start_network.sh/start_network.bat scripts located in the [bin](/bin) directory (Consider changing the java heap size in the file with the ```-Xmx``` parameter to avoid storage allocation issues).

Alternatively, Docker can be used, as explained below, to run the WebOCD service and database in one line.

# Quickstart Docker
To build and run WebOCD Service using Docker, you can navigate to the REST-OCD-Service directory and execute
```
docker compose up
```
This will build and run containers for WebOCD service and arangodb, which is required for the service to run properly.

You can adjust the database container default password by modifying docker-compose.yml. Keep in mind to also change the config file [config.properties](ocd/arangoDB/config.properties), so that WebOCD tests as well as the service can use the database.