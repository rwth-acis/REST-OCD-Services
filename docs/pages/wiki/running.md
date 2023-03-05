---
layout: page
title: Running the Service
---

# Making Preparations
## General
Before you start the service you might want to configure the _start_network.bat_ or _start_network.sh_ files first for things such as RAM usage, but especially for making your agent accessible after recompilation with the _`--node-id-seed`_ and other las2peer node launcher parameters (see https://github.com/rwth-acis/las2peer-template-project/wiki/L2pNodeLauncher-Commands).

## Linux Users
Use `chmod +x <filename>` to allow the execution of the scripts.

# Running the Service
Once the arangoDB instance is accessible, you can launch the service on a single node LAS2peer network with the scripts _start_network.bat_ or _start_network.sh_ which are located in the _bin_ directory. The service should be running within a few seconds and be available as a REST service under the basepath _`http://127.0.0.1:8080/ocd/`_ (Note that your browser will display a 404 if you type in this address, to see if the service is running check the address without the _`/ocd`_ part instead). You can terminate the process by either typing _exit_ in the interactive console or of course also by any other common means.

It is recommended that you use the OCD Service in combination with the web client at https://github.com/rwth-acis/OCD-Web-Client, which will take care of formulating the right requests and especially the login process for you as well as provide JSON visualization.

# Docker
To build and run WebOCD Service using Docker, you can navigate to the REST-OCD-Service directory and execute
```
docker compose up
```
This will build and run containers for WebOCD service and ArangoDB database, which is required for the service to run properly.

You can adjust the database container default password by modifying *docker-compose.yml*. Keep in mind to also change the config file *ocd/arangoDB/config.properties*, so that WebOCD tests as well as the service can use the database.

# Potential Issues
Your service might run into the _java.lang.outOfMemoryError_, this indicates that the heap size of the program is most likely to small. You can increase the maximum size by adding the -Xmx<SIZE> parameter in the _start_network_ files. For information on how to use it, please refer to the official Documentation (https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/jrdocs/refman/optionX.html)

If you are getting a warning about encryption problems then you most likely have to change the Security Policy Files of your JRE. This can also happen after a JRE update. Please refer to the Las2Peer Template Project for more information.