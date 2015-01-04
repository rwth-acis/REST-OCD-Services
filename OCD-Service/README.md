OCD Service

This is a RESTful web service for overlapping community detection.

For Linux:
The system was mainly tested on Windows so far. For Linux there is still a small issue with the Ant Build. In case you receive an exception that the database ("db") could not be created, please do manually remove the directory "db" from the ocd/derby directory before the build.

File Structure:
Any service specific files (aside from the source and test code and the src/META-INF/persistence.xml for testing purposes) are located in the ocd directory, which is subdivided as follows:
- bin:
	Includes scripts for launching / shutting down the database server of the derby database and to launch the ij tool for derby database monitoring.
- derby:
	Contains the derby database in the "db" folder as well as the database log and properties file (anything aside the derby.properties file might actually be created at a later stage).
- eclipselink:
	Contains the actual persistence.xml used by the service (other than for jUnit testing). Also contains log files created by the eclipse link JPA persitence provider containing information about the commands executed for database creation / dropping.
- ivy:
	Contains an ocd_ivy.xml to load the service's dependencies via ivy.
- lfr:
	Provides an application from Lancichinetti for calculating the LFR benchmarks he introduced (for directed and weighted graphs). Compiled once for each Linux and Windows.
- test:
	Includes two folders input and output containing data files required for testing purposes.

First set up:
- Use only the ocd_build.xml for building purposes, not the build.xml. Note that Eclipse might illustrate errors for the build file because aparrently it is not able to resolve the external target dependencies correctly. This should not cause an issue.
- Run the target "get_deps" in order to obtain all dependencies.
- Open the MANIFEST.MF in the META-INF folder of the las2peer jar which should be located in the lib folder (e.g. las2peer-0.3-SNAPSHOT.jar). You can get access to the jar file with any zip tool.
- Add now the path of the service jar to the Class-Path attribute of the manifest. The path should be "../service/i5.las2peer.services.ocd-0.1.jar".
- Finally run the target "all".
	
Running the service:
- First the derby database server must be started. Simply execute the script start_derby_server.bat for Windows or start_derby_server.sh for Linux, located in the ocd/bin directory.
- Then start a LAS2peer network using the script start_network.bat for Windows or start_network.sh for Linux, located in the bin directory. This script will start the service as well.
- The service should be running now.

User Configuration:
The service's user management is based on the users of the LAS2peer network. Simply add/remove users to/from the network.

Configuring the database:
- adapt the file ocd/eclipselink/persistence.xml
- also adapt the file src/META-INF/persistence.xml accordingly (for build/testing purposes)
	Note that the property "eclipselink.ddl-generation" should maintain the value "drop-and-create-tables" as opposed to the persistence.xml in the actual service jar which is set to "create-tables".
- make sure to also adapt the according targets in the build file ocd_build.xml if necessary.
	
This service is based on the LAS2peer framework. For any information on LAS2peer please refer to the LAS2peer project wiki.