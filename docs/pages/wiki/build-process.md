---
layout: page
title: Build Process
---

# Introduction
The OCD Service is built via Gradle. We recommend a version similar to 6.8, yet others may also work. The project has mostly been developed with the Eclipse IDE using the gradle buildship plugin (most eclipse IDEs already include this), yet IDEs like IntelliJ are actively used as well. Finally note that you will likely need Java version 14 for the build to work without problems. 

There also exists an older version using Apache Ant which is described at the bottom. Here, there are two build files, _build.xml_, and _ocd_build.xml_. Please do only use the _ocd_build.xml_. Note that Eclipse might illustrate errors for the build file because apparently it is not able to resolve the external target dependencies to the second build file correctly. This should not cause an issue. It is highly recommended that you build the project with Eclipse (To use Java 8 for the older version you may have to go with the Eclipse IDE for Java EE Developers variant) as this is the environment used during development and therefore the best tested one.

# Executing the Build Process

## First Build
When you are going to run the build process the very first time, you will need two manually obtain two libraries:
+ _y.jar_ forms part of the commercial yFiles library by yWorks which is not publicly available. Therefore you will have to add it manually to the project. The Chair i5 of RWTH Aachen University has a license for the library, so please contact somebody to obtain access. This will obviously only be possible if you are somehow affiliated with the Chair.
+ _ysvg.jar_ is an SVG writer for yFiles Graph2D objects that makes it possible to add useful features to generated SVG diagrams. The library is used for the visualization of graphs and covers.
Both libraries then have to be placed into the _yFiles_ folder.

Now, you just have to import the project as a gradle project via eclipse or simply open in via IntelliJ. After the first build, eclipse might still give you errors saying the yFiles jars are missing. To fix this, simply refresh the gradle project via the option you get from right clicking the folder. 

After the build, you can then either start the _start_network.bat_ or _start_network.sh_ files in the bin folder to get the service running, see [Running the OCD Service](https://github.com/rwth-acis/REST-OCD-Services/wiki/Running-the-OCD-Service) for more information.

## General Build
There are numerous targets which can be run. This summarizes the most important ones:
+ _jar_: Generates the service jar in the _service_ and a copy in the _rest\_ocd\_services/export/jars_ directory. This archive is required for running the service.
+ _test_: Runs all jUnit tests . The test reports are stored in _rest\_ocd\_services/export/test_reports_.
+ _yguard_: Generates an obfuscated service jar in the _rest\_ocd\_services/export/obfuscated_ directory. This is required for public service deployment. Please refer to [Deploying the OCD Service](https://github.com/rwth-acis/REST-OCD-Services/wiki/Deploying-the-OCD-Service) for more information. A log file of the obfuscation process is stored under _ocd/yGuard/log.xml_.
+ _build_: The target you'll likely be running most of the time. This executes the build process and generates a jar as well as the executables.

# Potential Issues

Currently, the build might fail if a version of Java not equal to 14 (or 8 for the older version of the service) is used, even with eclipse in compliance mode for that version, and running the service with a version other than this one is also likely to cause errors.

## Linux Users
The system was mainly tested on Windows so far. For Linux, there is still a small issue with the old Ant Build. In case you receive an exception that the database (_db_) could not be created, please do manually remove the directory _db_ from the _ocd_ directory before the build.


# Executing the Old Ant Build Process

## First Build
When you are going to run the build process the very first time, please make sure to run the target _get_deps_ in order to obtain all dependencies.

Due to the yFiles library being commercial, there are three dependencies which will be missing and must be added manually to the _lib_ folder:
+ _y.jar_ forms part of the commercial yFiles library by yWorks which is not publicly available. Therefore you will have to add it manually to the project. The Chair i5 of RWTH Aachen University has a license for the library, so please contact somebody to obtain access. This will obviously only be possible if you are somehow affiliated with the Chair.
+ _yguard.jar_ is a free obfuscation tool by yWorks which is only required for public deployment of the service. Anybody can download it from this [link](https://www.yworks.com/downloads#yGuard). Refer to [Deploying the OCD Service](https://github.com/rwth-acis/REST-OCD-Services/wiki/Deploying-the-OCD-Service) for more information. You can probably do without it.
+ _ysvg.jar_ is an SVG writer for yFiles Graph2D objects that makes it possible to add useful features to generated SVG diagrams. The library is used for the visualization of graphs and covers.

## Linux Users
Please make sure that the program _ocd/lfr/LfrBenchmarkLinux_ is executable (via `chmod +x <filename>`).

## General Build
There are numerous targets which can be run. This summarizes the most important ones:
+ _jar_: Generates the service jar in the _service_ and a copy in the _export/jars_ directory. This archive is required for running the service.
+ _generate_configs_: Generates the user and service agents as specified in _etc/ant_configuration_. The users will be loaded when the service (or more precisely the underlying LAS2peer node) is launched.
+ _junit_: Runs all jUnit tests (except _src/test/i5/las2peer/services/ocd/DatabaseInitializer.java_, take look at the target _setup_database_ for more information). The test reports are stored in _export/test_reports_.
+ _startscripts:_ Generates startscripts into the bin folder that are used to start the service.
+ _obfuscate_: Generates an obfuscated service jar in the _export/obfuscated_ directory. This is required for public service deployment. Please refer to [Deploying the OCD Service](https://github.com/rwth-acis/REST-OCD-Services/wiki/Deploying-the-OCD-Service) for more information. A log file of the obfuscation process is stored under _ocd/yGuard/log.xml_.
+ _setup_database_: This is intended for automatically populating the database for presentation or debugging purposes. It is not required in any way to actually make the database work. The database is set up by running _src/test/i5/las2peer/services/ocd/DatabaseInitializer.java_ as a jUnit test. You can adapt the code in any way that suits your needs.
+ _all_: Runs all of the above except _setup_database_.

## Building with IntelliJ
For IntelliJ, you can follow the above guide as well. For correct indexing in the IDE you however need to add the lib folder as a library via right click. You might also want to double check that build.xml isn't selected as an ant build file instead of ocd_build.xml
