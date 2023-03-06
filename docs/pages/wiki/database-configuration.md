---
layout: page
title: Database Configurations
---

# ArangoDB
The service makes use of ArangoDB database. A database instance should be running before executing the build process. Otherwise, the database related tests will fail.
Database instance details that WebOCD Service uses can be configured by changing the _ocd/arangoDB/config.properties_ file.

If the database is not available on the machine where WebOCD Service should run, it is possible to use Docker, without installing/running the database first. For this refer to [Running the OCD Service](/REST-OCD-Services/pages/wiki/running#docker).
