---
layout: page
title: User Management and Authentication
---

# Introduction
The OCD Service's authentication and user management is based on the user agents of the underlying LAS2peer network. This page will just give a brief overview over some of the most important aspects, however this information might become outdated at any point. For more and up-to-date information on authentication and user management in LAS2peer networks and services please refer to the [LAS2peer project documentation](https://github.com/rwth-acis/las2peer-template-project/wiki/WebConnector%3A-Request-Authentication).
This part deals with manually adding users which should not be necessary if you use the [OCD Web Client](https://github.com/rwth-acis/OCD-Web-Client) or OIDC login for the service. In this case, the underlying las2peer webconnector will create and log in Users according to their Accounts from the OIDC provider.

# User Management

You can preinstall up to three users via the gradle task _generateAgents_. For that purpose the users must be defined in the *gradle.properties* file.

It is also possible to manually add LAS2peer user agents to an existing network. Please refer to the LAS2peer Template Project and LAS2peer for more information.

Currently the OCD Service implements no method that allows to add, change or erase users via the service interface.

# Authentication

Although possible, it is not recommended to use the service without user authentication. Through authentication a user can manage its own graphs, covers, etc. Without authentication all data is shared with anybody else using the service anonymously and thus may be altered or deleted by other users.

Information on how to create a new user is given below.

Once you have a valid user account you can authenticate through [Basic Authentication](http://en.wikipedia.org/wiki/Basic_access_authentication). The authentication header has to be sent with every single request.
