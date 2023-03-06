---
layout: page
title: Service Deployment
---

# Building
If you want to deploy the OCD Service to a server, the service must first be build. There are two basic alternatives. Either you directly build the project on the machine where it should be deployed, or you build it on some other machine and then move the files to their designated location. You will have to execute the _jar_ target in order to create the service jar. Please refer to [Build Process](/REST-OCD-Services/pages/wiki/build-process) for more information. Make sure you also generate the user agents for any required user accounts through the target by altering the gradle build file accordingly and running the _jar_ target, if needed.

# Additional Aspects
There are a few additional aspects that should be taken under consideration. First of all, there are several project folders which do not need to be deployed. After completing the build, only the following resources are relevant for the service execution, any others may be removed.

+ _bin_ directory
+ _etc_ directory (without _ant_configuration_ and _ivy_ in the old ant build)
+ _lib_ directory
+ _log_ directory
+ _ocd_ directory (without _ivy_ in the old ant build, _test_ and _yGuard_)
+ _service_ directory (in the case of a public deployment, the standard service jar must be replaced by the obfuscated one
+ LICENSE
+ NOTICE
+ README.md
+ (ocd.conf in the old and build)


Also, when deploying on a Linux system, make sure with `chmod +x <filename>` that the execution of all relevant scripts and programs is allowed. Please refer to [Project Structure](/REST-OCD-Services/pages/wiki/project-structure/) for more information.

Remember also that the Security Policy Files of the JRE being used on the server will in some cases have to be changed, please refer to the LAS2peer Template Project for more information.