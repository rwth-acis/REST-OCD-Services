OCD Service

This is a RESTful web service for overlapping community detection.

For Linux:
The system was mainly tested on Windows so far. For Linux there is still a small issue with the Ant Build. In case you receive an exception that the database ("db") could not be created, please do manually remove the directory "db" from the ocd/derby directory before the build.

File Structure:
Any service specific files (aside from the source and test code and the META-INF/persistence.xml) are located in the ocd directory, which is subdivided as follows:
- bin:
	Includes scripts for launching / shutting down the database server of the derby database and to launch the ij tool for derby database monitoring.
- derby:
	Contains the derby database in the "db" folder as well as the database log and properties file (anything aside the derby.properties file might actually be created at a later stage).
- eclipselink:
	Contains log files created by the eclipse link JPA persitence provider containing information about the commands executed for database creation / dropping.
- lfr:
	Provides an application from Lancichinetti for calculating the LFR benchmarks he introduced (for directed and weighted graphs). Compiled once for each Linux and Windows.
- test:
	Includes two folders input and output containing data files required for testing purposes.

Starting the service:
- First the derby database server must be started. Simply execute the script start_derby_server.bat for Windows or start_derby_server.sh for Linux, located in the ocd/bin directory.
- Then start a LAS2peer network using the script start_network.bat for Windows or start_network.sh for Linux, located in the bin directory. This script will start the service as well.
- The service should be running now.

User Configuration:
The service's user management is based on the users of the LAS2peer network. Simply add/remove users to/from the network.
By default there is one user with name "User" and password "user".

Configuring the database:
- adapt the file lib/las2peer-0.0.4.1.jar/META-INF/persistence.xml
	Note that the actual name of the las2peer jar might differ according to the version being used.
	The jar can easily be manipulated like a zip file, e.g. using the 7-zip File Manager.
- also adapt the file src/META-INF/persistence.xml accordingly (for build/testing purposes)
	Note that the property "eclipselink.ddl-generation" should maintain the value "drop-and-create-tables" as opposed to the persistence.xml in the actual service jar which is set to "create-tables".
- make sure to also adapt the according targets in the ant build file.
	
This service is based on the LAS2peer framework. For any information on LAS2peer please refer to the LAS2peer project wiki.