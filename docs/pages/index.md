---
layout: page
title: REST-OCD-Service
permalink: /
---

# REST-OCD-Service

Welcome to the Wiki of the RESTful Overlapping Community Detection (OCD) Service!

![karate-2D](/REST-OCD-Services/assets/img/karate-2D.jpg "Communities in the karate graph"){: width="640"; height="auto"}

The OCD Service provides means for

+ storing and retrieving graphs and covers in different formats
+ calculating covers based on OCD algorithms
+ creating test graphs and covers based on benchmark models
+ executing metrics for measuring cover quality

In combination with the service we also provide a corresponding viewer service that creates illustrations of graphs and covers in svg format and a web frontend to facilitate the access and provide further foce graph visualizations.

Note that this service is based on the [LAS2peer Template Project](https://github.com/rwth-acis/LAS2peer-Template-Project). For general information on LAS2peer services, please refer also to that project and to the documentation of the underlying [LAS2peer framework](https://github.com/rwth-acis/LAS2peer). This includes e.g. more detailed information in particular on user management, running LAS2peer services or Swagger Documentation.

## The OCD Service API and Web Client

The OCD Service is publicly accessible via this [link](http://webocd.dbis.rwth-aachen.de/OCDWebClient/login.html). Please note that you should get a Learning Layers account to use the service. This is to ensure that your data is safe since otherwise anybody can access, alter and delete it anonymously. For information on user authentication please refer to [User Management and Authentication](/REST-OCD-Services/pages/wiki/user-management-and-authentication).

Along with the service we provide a web client which also has its own [project](https://github.com/rwth-acis/OCD-Web-Client). The client can only be used with a valid Learning Layers account. We recommend to use the service along with the web client.


We additionally provide a brief [Integration Tutorial](/integration/) that shows you the usage of the most important requests so that you can get started with the OCD Service more quickly.

## The OCD Service
The service is developed using the Eclipse IDE, so we recommend you to stick to that tool for any work on this project.

Please refer to the following pages in order to obtain more information about the OCD Service.

+ [Project Structure](/REST-OCD-Services/pages/wiki/project-structure)
+ [Build Process](/REST-OCD-Services/pages/wiki/build-process)
+ [Running the OCD Service](/REST-OCD-Services/pages/wiki/running)
+ [User Management and Authentication](/REST-OCD-Services/pages/wiki/user-management-and-authentication)
+ [Deploying the OCD Service](/REST-OCD-Services/pages/wiki/service-deployment)
+ [Database Configuration](/REST-OCD-Services/pages/wiki/database-configuration)

### Important
The OCD Service is largely based on the commercial **yFiles** library. You will only be able to run it yourself if you have access to the archive _y.jar_. The Chair i5 of RWTH Aachen University has a license for that library. If you are affiliated with that institute, please contact somebody to obtain the library. Also, keep in mind that the archive mentioned above must under no circumstances be publicly deployed or otherwise made publicly available. Please refer to [Deploying the OCD Service](/REST-OCD-Services/pages/wiki/service-deployment) for more information.

The service also currently requires **Java JDK 14** to be built and most likely also to run it. However, there also exists an older version for JDK 8 (tag [1.0.0](https://github.com/rwth-acis/REST-OCD-Services/tree/1.0.0)) which is still functional in case you need it.

