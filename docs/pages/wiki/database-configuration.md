---
layout: page
title: Database Configurations
---

# JPA and persistence.xml
The service makes use of the Java Persistence API (JPA) specification to simplify database access. The JPA implementation that is being used is EclipseLink. If you want to use the service with another database or change some aspects of the database configuration you must adapt the following files accordingly.
+ _rest\_ocd\_services/src/main/java/META-INF/persistence.xml_: This database configuration is used for the actual execution of the service through the service jar (e.g. when running _bin/start_network.bat_)
+ _rest\_ocd\_services/src/main/java/META-INF/testing/persistence.xml_: This database configuration is used during jUnit testing.

The file locations are different in the old ant build:
+ _ocd/eclipselink/persistence.xml_ for the execution
+ _src/META-INF/persistence.xml_ for testing