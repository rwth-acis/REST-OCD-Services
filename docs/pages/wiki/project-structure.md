---
layout: page
title: Project Structure
---

# Introduction
This page gives you an overview over the file structure of the OCD Service Project. The focus will be set on the _ocd_ directory where most service specific files are located (aside from the source code). Some other files will be discussed as well. For more information on the remaining files you may refer to the LAS2peer Template Project or LAS2peer documentation.

# Source Code
The actual source code of the service is located under _rest\_ocd\_services/src/main/java_. The most important class here is probably the _i5/las2peer/services/ocd/ServiceClass.java_ which contains the entire Service API. 

The source code for the jUnit tests is in _rest\_ocd\_services/src/test_.

ArangoDB database configuration file *config.properties* is located in *ocd/arangoDB* directory. This file can be modified to adjust database used for WebOCD Service as well as tests. For more information please refer to [Build Process](/REST-OCD-Services/pages/wiki/database-configuration).

# OCD Directory
The _ocd_ directory contains most files apart from the source code which are specific to the OCD Service. You will find the following subdirectories.

+ _arangoDB_: Contains the database configuration file.
+ _eclipselink_: Any log files for database creation or dropping created by the EclipseLink JPA persitence provider will be stored here.
+ _test_: Contains a folder with data files for jUnit test input. An additional folder for test output files may be created during the build.

# Other Important Resources
+ _bin_: Comprises scripts for launching the service and generating Agents.
+ _rest\_ocd\_services/export_: This directory contains a copy of the service jar, the obfuscated service jar and jUnit test reports. Please refer to [Build Process](/REST-OCD-Services/pages/wiki/build-process) for more information.
+ _service_: This directory contains the service jar. Please refer to [Build Process](/REST-OCD-Services/pages/wiki/build-process) for more information.
+ _bin/start_network.bat_ and _bin/start_network.sh_: Skripts for launching the OCD Service. Please refer to [Running the OCD Service](/REST-OCD-Services/pages/wiki/running) for more information.
+ _etc/startup/passphrases.txt_: Password storage for preinstalled users. Please refer to [User Management and Authentication](/REST-OCD-Services/pages/wiki/user-management-and-authentication) for more information.
