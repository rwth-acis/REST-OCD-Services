---
layout: page
title: Project Structure
---

# Introduction
This page gives you an overview over the file structure of the OCD Service Project. The focus will be set on the _ocd_ directory where most service specific files are located (aside from the source code). Some other files will be discussed as well. For more information on the remaining files you may refer to the LAS2peer Template Project or LAS2peer documentation.

# Source Code
The actual source code of the service is located under _rest\_ocd\_services/src/main/java_. The most important class here is probably the _i5/las2peer/services/ocd/ServiceClass.java_ which contains the entire Service API. You will also find the _META-INF_ directory with the persistence.xml files. For more information on this aspect take a look at [Database Configuration](https://github.com/rwth-acis/REST-OCD-Services/wiki/Database-Configuration).

The source code for the jUnit tests is in _rest\_ocd\_services/src/test_. This directory includes also the _src/test/i5/las2peer/services/ocd/DatabaseInitializer.java_ which can be used for database initialisation. For more information please refer to [Build Process](https://github.com/rwth-acis/REST-OCD-Services/wiki/Build-Process).

# OCD Directory
The _ocd_ directory contains most files apart from the source code which are specific to the OCD Service. You will find the following subdirectories.

+ _db_: Contains the database of the JPA provider.
+ _eclipselink_: Any log files for database creation or dropping created by the EclipseLink JPA persitence provider will be stored here.
+ _lfr_: Provides an application from Lancichinetti for calculating the LFR benchmarks he introduced (for directed and weighted graphs). It is compiled once for each Linux and Windows.
+ _test_: Contains a folder with data files for jUnit test input. An additional folder for test output files may be created during the build.
+ _yGuard_: Any log files of the obfuscation process will be stored here. Please refer to [Deploying the OCD Service](Deploying the OCD Service) for more information.
+ (_ivy_ if using the old ant build: Contains an _ocd_ivy.xml_ file to load the service's dependencies via Ivy. Note that there is an additional _ivy.xml_ and _ivy.settings_ file for dependencies required by LAS2peer under _etc/ivy_ from the root directory.)

# Other Important Resources
+ _bin_: Comprises scripts for launching the service and generating Agents.
+ _rest\_ocd\_services/export_: This directory contains a copy of the service jar, the obfuscated service jar and jUnit test reports. Please refer to [Build Process](https://github.com/rwth-acis/REST-OCD-Services/wiki/Build-Process) for more information.
+ _service_: This directory contains the service jar. Please refer to [Build Process](https://github.com/rwth-acis/REST-OCD-Services/wiki/Build-Process) for more information.
+ _bin/start_network.bat_ and _bin/start_network.sh_: Skripts for launching the OCD Service. Please refer to [Running the OCD Service](https://github.com/rwth-acis/REST-OCD-Services/wiki/Running-the-OCD-Service) for more information.
+ _etc/startup/passphrases.txt_: Password storage for preinstalled users. Please refer to [User Management and Authentication](https://github.com/rwth-acis/REST-OCD-Services/wiki/User-Management-and-Authentication) for more information.
+ (_ocd_build.xml_ if using the old ant build: The build file for the OCD Service. Please do only make use of this build file. Please refer to [Build Process](https://github.com/rwth-acis/REST-OCD-Services/wiki/Build-Process) for more information.)
+ (_ocd.conf_ if using the old ant build: Upstart script for the OCD Service. Please refer to [Deploying the OCD Service](https://github.com/rwth-acis/REST-OCD-Services/wiki/Deploying-the-OCD-Service) for more information.)
+ (_etc/ant_configuration/user.properties_ if using the old ant build: Ant property file for user agent generation. Please refer to [User Management and Authentication](https://github.com/rwth-acis/REST-OCD-Services/wiki/User-Management-and-Authentication) for more information.)