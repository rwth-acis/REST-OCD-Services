# OCD Service - WebOCD

This is a RESTful web service offering various overlapping community detection algorithms(OCDA) and centrality measures/simulations. The service is developed with the Eclipse IDE and IntelliJ, so we recommend you to use one of these tools to work on the project.<br/>
**WebOCD works best with its corresponding [web client](https://github.com/rwth-acis/OCD-Web-Client)**.

A running instance of WebOCD can be found at http://webocd.dbis.rwth-aachen.de/OCDWebClient/.

For any information on the service itself please refer to the [project wiki](https://github.com/rwth-acis/REST-OCD-Services/wiki) or the [website](https://rwth-acis.github.io/REST-OCD-Services/).

### Additional Notes
This service is based on the LAS2peer Template Project and its underlying LAS2peer framework. For any information on these please refer to https://github.com/rwth-acis/LAS2peer-Template-Project and https://github.com/rwth-acis/LAS2peer


# Quickstart with a Docker Container
To build and run WebOCD Service using Docker, you can navigate to the REST-OCD-Service directory and execute
```
docker compose up
```
This will build containers for WebOCD service and arango db, which is required for the service to run properly. 

You can adjust the database container default password by modifying docker-compose.yml. Keep in mind to also change the config files [config_test.properties](ocd/arangoDB/config_test.properties) and [standard_config.properties](ocd/arangoDB/standard_config.properties) so that WebOCD tests as well as the service can use the database.