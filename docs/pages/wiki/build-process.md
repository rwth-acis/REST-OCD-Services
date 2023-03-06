---
layout: page
title: Build Process
---

# Introduction
The OCD Service is built via Gradle. We recommend a version similar to 7.3, yet newer versions may also work. The project has mostly been developed with the IntelliJ IDEA. You can import the project into IntelliJ as a Gradle project. Finally note that you will likely need Java version 17 for the build to work without problems.

# Executing the Build Process

## First Build
When you are going to run the build process the very first time, you just have to import the project as a gradle project IntelliJ and execute the gradle *build* task. Make sure to adapt the database configuration _ocd/arangoDB/config.properties_ to fit your running ArangoDB instance.

After the build, you can then either start the _start_network.bat_ or _start_network.sh_ files in the bin folder to get the service running, see [Running the OCD Service](/REST-OCD-Services/pages/wiki/running) for more information.

## General Build
There are numerous gradle tasks which can be executed. This summarizes the most important ones:
+ _jar_: Generates the service jar in the _service_ and a copy in the _rest\_ocd\_services/export/jars_ directory. This archive is required for running the service.
+ _test_: Runs all jUnit tests . The test reports are stored in _rest\_ocd\_services/export/test_reports_.
+ _build_: The target you'll likely be running most of the time. This executes the build process and generates a jar as well as the executables.
+ _startscripts:_ Generates startscripts into the bin folder that are used to start the service.
+ _generateAgents_: Generates the user and service agents as specified in _build.gradle_. The users will be loaded when the service (or more precisely the underlying LAS2peer node) is launched.
+ _clean_: Clean up files generated from the build. This should be executed before rebuilding.


# Potential Issues
Currently, the build might fail if a version of Java not equal to 17 is used.

Running WebOCD Service might cause *AgentAccessDeniedException* if the *build* task was performed multiple times without having performed the *clean* task inbetween. If this occurs, simply do the *clean* task followed by the *build* task and the issue should be solved.

